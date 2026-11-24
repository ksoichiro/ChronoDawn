#!/usr/bin/env python3
"""Claude Code PreToolUse hook for Bash: the gate before `git push` and `gh pr create`.

AGENTS.md asks for a test or a scenario with every visible or behavioral
change, and for the independent verification of docs/merge_policy.md before a
pull request. Written instructions alone were skipped, so this hook checks the
branch mechanically and denies the command with the reason when:

1. Files that change something visible or a behavior are in the diff, but no
   unit test, GameTest or scenario is, and no commit carries a
   `Test-Exemption:` trailer.
2. The independent verification has not run on the current diff: the
   build/verification/diff.patch it reviewed differs from the branch diff, or
   the reviewer's report (build/verification/review.md) is missing or older.
3. `gh pr create` gets a body without a `## Verification` section and a
   `Merge tier:` line.

The reviewer itself is a subagent that only the main agent can start (Agent
tool), so the hook cannot launch it. Denying the command with an instruction
to run the independent-verification skill is how the review gets started.

A command that sets CHRONODAWN_PR_GATE_SKIP="<reason>" passes untouched. The
reason stays in the transcript for the owner to read.

The procedure for agents is in .claude/skills/pr-gate/SKILL.md. Internal
errors fail open (exit 0 without a decision), so a broken gate never blocks
work, it only stops guarding.
"""
import json
import os
import re
import shlex
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.merge_tier.classify import declared_tiers, glob_to_regex  # noqa: E402

SKIP_VAR = "CHRONODAWN_PR_GATE_SKIP"
EXEMPTION_TRAILER = "Test-Exemption"
MIN_REASON_LENGTH = 20
VERIFICATION_DIR = Path("build") / "verification"
PROTECTED_BRANCHES = {"main", "master"}


def _compile(patterns):
    return [glob_to_regex(p) for p in patterns]


# A changed file in one of these counts as a test or scenario for check 1.
TEST_PATTERNS = _compile([
    "**/src/test/**",
    "**/src/gametest/**",
    "common/gametest/**",
    "**/java/**/gametest/**",
    "scripts/scenario_check/scenarios/**",
])
# Files whose change is visible in game or alters a behavior.
BEHAVIOR_PATTERNS = _compile([
    "**/*.java",
    "**/resources/assets/**",
    "**/resources/data/**",
    "**/*.bbmodel",
    "**/*mixins*.json",
    "**/*.accesswidener",
])
# Never behavioral: translations change text only, which the lang validators
# cover, and package-info.java holds annotations for tools. Loader metadata
# (fabric.mod.json, META-INF, pack.mcmeta) is outside BEHAVIOR_PATTERNS because
# prodSmokeTest, not a unit test or scenario, is what checks it.
NON_BEHAVIOR_PATTERNS = _compile([
    "**/assets/*/lang/**",
    "**/package-info.java",
])

# A Java comment, or a string or char literal, which must be kept whole so that
# `"//"` inside a string is not taken for a comment.
_JAVA_COMMENT_OR_LITERAL = re.compile(
    r'//[^\n]*|/\*.*?\*/|"""(?:\\.|[^\\])*?"""|"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\\n])*\'',
    re.DOTALL,
)
_BODY_FROM_CAT = re.compile(r"^\$\(\s*cat\s+(['\"]?)([^'\"\s()]+)\1\s*\)$")


def _matches(regexes, path):
    return any(r.match(path) for r in regexes)


# --- Command parsing ---------------------------------------------------------

_ENV_ASSIGN = re.compile(r"^([A-Za-z_][A-Za-z0-9_]*)=(.*)\Z", re.DOTALL)
_SHELLS = ("bash", "sh", "zsh")
# Global options of git and gh that take a separate value before the subcommand.
_GIT_OPTIONS_WITH_VALUE = ("-C", "-c", "--git-dir", "--work-tree", "--namespace", "--config-env")
_GH_OPTIONS_WITH_VALUE = ("-R", "--repo")
# `<<` or `<<-` with an optionally quoted delimiter. A here-string (`<<<`) is not one.
_HEREDOC = re.compile(r"(?<!<)<<(?!<)-?\s*(['\"]?)([A-Za-z_][A-Za-z0-9_]*)\1")
_OPERATOR_CHARS = "();<>|&\n"
# git push options that take a separate value, so the value is not a remote or refspec.
_PUSH_OPTIONS_WITH_VALUE = ("-o", "--push-option", "--repo", "--receive-pack", "--exec")


def strip_heredocs(command):
    """Drop heredoc bodies. They are data (a PR body, a file), not commands to gate."""
    out, pending = [], []
    for line in command.split("\n"):
        if pending:
            if line.strip() == pending[0]:
                pending.pop(0)
            continue
        out.append(line)
        pending.extend(m.group(2) for m in _HEREDOC.finditer(line))
    return "\n".join(out)


def _shell_tokens(text):
    """Split like a shell: quotes group words, and operators such as `;`, `&&` or `2>&1` are their own tokens."""
    lexer = shlex.shlex(text, posix=True, punctuation_chars=_OPERATOR_CHARS)
    lexer.whitespace = " \t\r"
    lexer.whitespace_split = True
    try:
        return list(lexer)
    except ValueError:
        return text.split()


def _is_operator(token):
    return bool(token) and all(c in _OPERATOR_CHARS for c in token)


def _simple_commands(tokens):
    """Group tokens into simple commands, dropping redirections and their targets."""
    commands, current, i = [], [], 0
    while i < len(tokens):
        token = tokens[i]
        if _is_operator(token) and ("<" in token or ">" in token):
            # `2>&1`: drop the file descriptor number before the operator and the target after it.
            if current and current[-1].isdigit():
                current.pop()
            i += 2
            continue
        if _is_operator(token):
            if current:
                commands.append(current)
            current = []
        else:
            current.append(token)
        i += 1
    if current:
        commands.append(current)
    return commands


def _join(base, path):
    return path if base is None else os.path.join(base, path)


def _match_git_push(tokens, j):
    """(args, git_dir) when tokens[j] starts `git [options] push`, else None."""
    i, git_dir = j + 1, None
    while i < len(tokens) and tokens[i].startswith("-"):
        if tokens[i] in _GIT_OPTIONS_WITH_VALUE and i + 1 < len(tokens):
            if tokens[i] == "-C":
                git_dir = _join(git_dir, tokens[i + 1])
            i += 2
        else:
            i += 1
    if i < len(tokens) and tokens[i] == "push":
        return tokens[i + 1:], git_dir
    return None


def _match_gh_pr_create(tokens, j):
    """The args when tokens[j] starts `gh [options] pr create`, else None."""
    i = j + 1
    while i < len(tokens) and tokens[i].startswith("-"):
        i += 2 if tokens[i] in _GH_OPTIONS_WITH_VALUE else 1
    if tokens[i:i + 2] == ["pr", "create"]:
        return tokens[i + 2:]
    return None


def find_commands(command, base_dir=None):
    """Return [(kind, args, git_dir, skip)] for each `git push` ("push") and `gh pr create` ("pr").

    git and gh are looked for anywhere in a simple command, not only as its
    first word, so wrappers (`timeout 600 git push`), keywords (`then git push`)
    and braces do not hide them. A false match only means the gate checks a
    branch that would pass anyway. git_dir is relative to the hook's cwd and
    follows a preceding `cd`. skip is the CHRONODAWN_PR_GATE_SKIP value
    assigned in front of that command, or None.
    """
    found = []
    cd_dir = base_dir
    for tokens in _simple_commands(_shell_tokens(strip_heredocs(command))):
        if tokens[0] == "cd" and len(tokens) > 1:
            cd_dir = _join(cd_dir, tokens[1])
            continue
        skip = None
        for j, token in enumerate(tokens):
            m = _ENV_ASSIGN.match(token)
            if m and m.group(1) == SKIP_VAR:
                skip = m.group(2).strip()
                continue
            inner = None
            if token == "eval" and j + 1 < len(tokens):
                inner = " ".join(tokens[j + 1:])
            elif token in _SHELLS:
                flags = [k for k in range(j + 1, len(tokens) - 1) if tokens[k].startswith("-")
                         and not tokens[k].startswith("--") and "c" in tokens[k]]
                if flags:
                    inner = tokens[flags[0] + 1]
            if inner is not None:
                found += [(k, a, d, s if s is not None else skip) for k, a, d, s in find_commands(inner, cd_dir)]
                break
            if token == "git":
                match = _match_git_push(tokens, j)
                if match:
                    args, git_dir = match
                    git_dir = git_dir if git_dir and os.path.isabs(git_dir) else (
                        _join(cd_dir, git_dir) if git_dir else cd_dir)
                    found.append(("push", args, git_dir, skip))
                    break
            if token == "gh":
                args = _match_gh_pr_create(tokens, j)
                if args is not None:
                    found.append(("pr", args, cd_dir, skip))
                    break
    return found


# --- Git ---------------------------------------------------------------------

def git(root, *args, check=True):
    proc = subprocess.run(["git", *args], cwd=root, capture_output=True)
    if check and proc.returncode != 0:
        raise RuntimeError(f"git {' '.join(args)}: {proc.stderr.decode(errors='replace').strip()}")
    return proc


def git_text(root, *args):
    return git(root, *args).stdout.decode("utf-8", errors="replace").strip()


def resolve_base(root, base_branch):
    for candidate in (f"origin/{base_branch}", base_branch):
        if git(root, "rev-parse", "--verify", "--quiet", candidate + "^{commit}", check=False).returncode == 0:
            return candidate
    raise RuntimeError(f"base branch {base_branch} not found")


def changed_files(root, merge_base, head):
    out = git(root, "diff", "--name-status", "--no-renames", "-z", merge_base, head).stdout.decode()
    fields = [f for f in out.split("\0") if f]
    return [(status[0], path) for status, path in zip(fields[0::2], fields[1::2])]


def java_code(text):
    """Java source with comments removed and whitespace outside literals collapsed, so only code is compared."""
    parts, last = [], 0
    for m in _JAVA_COMMENT_OR_LITERAL.finditer(text):
        parts.append(" ".join(text[last:m.start()].split()))
        # String and char literals stay byte for byte: their text can reach the player.
        parts.append(m.group(0) if m.group(0)[0] in "\"'" else "")
        last = m.end()
    parts.append(" ".join(text[last:].split()))
    return " ".join(p for p in parts if p)


def java_change_is_trivial(root, merge_base, head, path):
    """True when only comments and whitespace differ. Imports and commented-out code count as code."""
    def read(rev):
        proc = git(root, "show", f"{rev}:{path}", check=False)
        return proc.stdout.decode("utf-8", errors="replace") if proc.returncode == 0 else None
    base, new = read(merge_base), read(head)
    return base is not None and new is not None and java_code(base) == java_code(new)


# --- Checks ------------------------------------------------------------------

def exempted(root, merge_base, head, path):
    """True when the last commit in the range that touched path carries a Test-Exemption trailer."""
    values = git_text(
        root, "log", "-1", f"--format=%(trailers:key={EXEMPTION_TRAILER},valueonly)",
        f"{merge_base}..{head}", "--", path,
    )
    return any(len(v.strip()) >= MIN_REASON_LENGTH for v in values.splitlines())


def check_tests(root, merge_base, head):
    """Check 1. Returns a problem message or None."""
    changes = changed_files(root, merge_base, head)
    if any(status != "D" and _matches(TEST_PATTERNS, path) for status, path in changes):
        return None
    behavioral = []
    for status, path in changes:
        if _matches(TEST_PATTERNS, path) or _matches(NON_BEHAVIOR_PATTERNS, path):
            continue
        if not _matches(BEHAVIOR_PATTERNS, path):
            continue
        if path.endswith(".java") and status == "M" and java_change_is_trivial(root, merge_base, head, path):
            continue
        behavioral.append(path)
    if not behavioral:
        return None
    # An exemption covers the files of its own commit only, and only while no
    # later commit changes them again, so it cannot excuse a later untested edit.
    behavioral = [p for p in behavioral if not exempted(root, merge_base, head, p)]
    if not behavioral:
        return None
    shown = "\n".join(f"  - {p}" for p in behavioral[:10])
    more = f"\n  ... and {len(behavioral) - 10} more" if len(behavioral) > 10 else ""
    return (
        "These files change something visible or a behavior, but the branch adds or changes no unit test, "
        "GameTest or scenario (scripts/scenario_check/scenarios/*.json):\n"
        f"{shown}{more}\n"
        "Add a test or scenario that runs the change (AGENTS.md, Verification Reporting). When an existing "
        "test or scenario already covers it, or the change cannot alter what a player sees or does, add a "
        f"trailer `{EXEMPTION_TRAILER}: <reason>` to the last commit that changes these files, naming the "
        "covering test or scenario or explaining why. \"Could not check in game\" is not a reason: write a "
        "scenario instead."
    )


def recorded_base(root):
    """The base branch the reviewer's diff was taken against (build/verification/base), or main."""
    try:
        return (Path(root) / VERIFICATION_DIR / "base").read_text(encoding="utf-8").strip() or "main"
    except OSError:
        return "main"


def check_verification(root, merge_base, head, base_branch="main"):
    """Check 2. Returns a problem message or None."""
    vdir = Path(root) / VERIFICATION_DIR
    patch = vdir / "diff.patch"
    review = vdir / "review.md"
    current = git(root, "diff", merge_base, head).stdout
    how = (
        "Run the independent-verification skill (.claude/skills/independent-verification/SKILL.md): write "
        f"{VERIFICATION_DIR}/diff.patch against {base_branch} (and the base name to {VERIFICATION_DIR}/base "
        "when it is not main), dispatch the fresh reviewer subagent, and save its report verbatim "
        f"to {VERIFICATION_DIR}/review.md. The reviewer compares vanilla-derived logic with vanilla and runs "
        "the change."
    )
    if not patch.exists():
        return f"The independent verification has not run on this checkout ({patch.relative_to(root)} is missing). {how}"
    if patch.read_bytes() != current:
        return (
            f"{patch.relative_to(root)} differs from the branch diff, so the reviewer saw an older change. "
            f"Run the reviewer again on the new diff. {how}"
        )
    if not review.exists():
        return f"The reviewer's report is missing ({review.relative_to(root)}). {how}"
    if review.stat().st_mtime < patch.stat().st_mtime:
        return f"{review.relative_to(root)} is older than diff.patch, so it reviewed an earlier diff. {how}"
    text = review.read_text(encoding="utf-8", errors="replace")
    if "Commands run:" not in text:
        return (
            f"{review.relative_to(root)} does not look like the reviewer's report: it has no \"Commands run:\" "
            f"list. Save the reviewer's answer verbatim. {how}"
        )
    return None


_VERIFICATION_HEADING = re.compile(r"^##\s+Verification\s*$", re.MULTILINE)


def pr_body(args, cwd, command):
    """The body gh pr create will send, or the whole command when it is inline or unknown."""
    for i, arg in enumerate(args):
        value = None
        if arg in ("--body-file", "-F") and i + 1 < len(args):
            value = args[i + 1]
        elif arg.startswith("--body-file="):
            value = arg.split("=", 1)[1]
        elif arg in ("--body", "-b") and i + 1 < len(args):
            # --body "$(cat file)": the command text holds only the file name.
            m = _BODY_FROM_CAT.match(args[i + 1].strip())
            value = m.group(2) if m else None
        if value and value != "-":
            path = Path(value) if os.path.isabs(value) else Path(cwd) / value
            try:
                return path.read_text(encoding="utf-8")
            except OSError:
                return ""
    return command


def check_pr_body(body):
    """Check 3. Returns a problem message or None."""
    problems = []
    if not _VERIFICATION_HEADING.search(body):
        problems.append("a `## Verification` section")
    if not declared_tiers(body):
        problems.append("a `Merge tier:` line")
    if not problems:
        return None
    return (
        f"The pull request body lacks {' and '.join(problems)}. Pass the body with --body or --body-file and "
        "fill it as step 4 of the independent-verification skill describes."
    )


# --- Entry -------------------------------------------------------------------

def option_value(args, *names):
    for i, arg in enumerate(args):
        if arg in names and i + 1 < len(args):
            return args[i + 1]
        for name in names:
            if name.startswith("--") and arg.startswith(name + "="):
                return arg.split("=", 1)[1]
    return None


def push_heads(root, args):
    """The local revisions a push sends for review. Empty when it sends no branch to review."""
    if any(a in ("--delete", "-d", "--tags", "--dry-run", "-n") for a in args):
        return []
    positional, i = [], 0
    while i < len(args):
        if args[i] in _PUSH_OPTIONS_WITH_VALUE:
            i += 2
            continue
        if not args[i].startswith("-"):
            positional.append(args[i])
        i += 1
    current = git(root, "symbolic-ref", "--quiet", "--short", "HEAD", check=False).stdout.decode().strip()
    refspecs = positional[1:]
    if not refspecs:
        return [] if current in PROTECTED_BRANCHES or not current else ["HEAD"]
    heads = []
    for refspec in refspecs:
        if "$" in refspec or "`" in refspec:
            # Expanded only by the shell. Check the current branch, the likely value.
            if current and current not in PROTECTED_BRANCHES:
                heads.append("HEAD")
            continue
        src, _, dst = refspec.lstrip("+").partition(":")
        if not src or src.startswith("refs/tags/"):
            continue
        target = (dst or src).removeprefix("refs/heads/")
        if target in PROTECTED_BRANCHES or src in PROTECTED_BRANCHES:
            continue
        if src == "HEAD" and current in PROTECTED_BRANCHES:
            continue
        if git(root, "rev-parse", "--verify", "--quiet", src + "^{commit}", check=False).returncode != 0:
            continue
        heads.append(src)
    return heads


def pr_head(root, args):
    """The revision `gh pr create` opens the pull request for: --head when given, else HEAD."""
    head = option_value(args, "--head", "-H")
    if not head:
        return "HEAD"
    head = head.split(":", 1)[-1]
    for candidate in (head, f"origin/{head}"):
        if git(root, "rev-parse", "--verify", "--quiet", candidate + "^{commit}", check=False).returncode == 0:
            return candidate
    return "HEAD"


def evaluate(command, cwd):
    """Return the deny message for a Bash command, or None to let it run."""
    found = find_commands(command)
    problems = []
    for kind, args, git_dir, skip in found:
        if skip is not None:
            if len(skip) < 10:
                problems.append(
                    f"{SKIP_VAR} needs a reason of at least 10 characters that tells the owner why the gate is skipped.")
            continue
        where = os.path.join(cwd, git_dir) if git_dir else cwd
        top = git(where, "rev-parse", "--show-toplevel", check=False)
        if top.returncode != 0:
            continue
        root = top.stdout.decode().strip()
        # Only this repository is gated, not gradle/shared or any other clone.
        if not (Path(root) / "scripts" / "merge_tier" / "classify.py").exists():
            continue
        if kind == "push":
            heads = push_heads(root, args)
            base_branch = recorded_base(root)
        else:
            heads = [pr_head(root, args)]
            base_branch = option_value(args, "--base", "-B") or "main"
        base = resolve_base(root, base_branch) if heads else None
        reviewable = False
        for head in heads:
            merge_base = git_text(root, "merge-base", base, head)
            if merge_base == git_text(root, "rev-parse", head):
                continue
            reviewable = True
            for problem in (check_tests(root, merge_base, head),
                            check_verification(root, merge_base, head, base_branch)):
                if problem:
                    problems.append(problem)
        if kind == "pr" and reviewable:
            problem = check_pr_body(pr_body(args, where, command))
            if problem:
                problems.append(problem)
    if not problems:
        return None
    unique = list(dict.fromkeys(problems))
    lines = ["The PR gate (scripts/hooks/claude_pr_gate.py, .claude/skills/pr-gate/SKILL.md) stopped this command:"]
    lines += [f"{i}. {p}" for i, p in enumerate(unique, 1)]
    lines.append(
        f"Only for a push that is not on its way to a pull request, or when the owner asked for it, prefix the "
        f"command with {SKIP_VAR}=\"<reason>\"."
    )
    return "\n".join(lines)


def main():
    try:
        payload = json.load(sys.stdin)
    except ValueError:
        return 0
    if payload.get("tool_name") != "Bash":
        return 0
    command = (payload.get("tool_input") or {}).get("command") or ""
    cwd = payload.get("cwd") or os.getcwd()
    try:
        message = evaluate(command, cwd)
    except (RuntimeError, OSError) as e:
        print(f"claude_pr_gate: skipped after an error: {e}", file=sys.stderr)
        return 0
    if message:
        print(json.dumps({"hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "deny",
            "permissionDecisionReason": message,
        }}))
    return 0


if __name__ == "__main__":
    sys.exit(main())
