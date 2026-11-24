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
])
# Never behavioral: translations change text only, which the lang validators
# cover, and package-info.java holds annotations for tools.
NON_BEHAVIOR_PATTERNS = _compile([
    "**/assets/*/lang/**",
    "**/package-info.java",
])

_JAVA_TRIVIAL_LINE = re.compile(r"^\s*(?:$|//|/\*|\*|import\s|package\s)")


def _matches(regexes, path):
    return any(r.match(path) for r in regexes)


# --- Command parsing ---------------------------------------------------------

_SEGMENT_SPLIT = re.compile(r"&&|\|\||[;|&\n()]")
_ENV_ASSIGN = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*=")
_SKIP_ASSIGN = re.compile(SKIP_VAR + r"""=(?:"([^"]*)"|'([^']*)'|(\S+))""")


def _tokens(segment):
    try:
        return shlex.split(segment)
    except ValueError:
        return segment.split()


def _strip_prefix(tokens):
    """Drop leading VAR=value assignments and wrappers such as `env` or `command`."""
    while tokens and (_ENV_ASSIGN.match(tokens[0]) or tokens[0] in ("env", "command", "exec", "time")):
        tokens = tokens[1:]
    return tokens


def find_commands(command):
    """Return [("push", git_args, git_dir) | ("pr", gh_args, None)] found in a shell command."""
    found = []
    for segment in _SEGMENT_SPLIT.split(command):
        tokens = _strip_prefix(_tokens(segment.strip()))
        if len(tokens) >= 3 and tokens[0] == "gh" and tokens[1] == "pr" and tokens[2] == "create":
            found.append(("pr", tokens[3:], None))
            continue
        if not tokens or tokens[0] != "git":
            continue
        i, git_dir = 1, None
        while i < len(tokens) and tokens[i].startswith("-"):
            if tokens[i] in ("-C", "-c") and i + 1 < len(tokens):
                if tokens[i] == "-C":
                    git_dir = tokens[i + 1] if git_dir is None else os.path.join(git_dir, tokens[i + 1])
                i += 2
            else:
                i += 1
        if i < len(tokens) and tokens[i] == "push":
            found.append(("push", tokens[i + 1:], git_dir))
    return found


def skip_reason(command):
    m = _SKIP_ASSIGN.search(command)
    if not m:
        return None
    return next(g for g in m.groups() if g is not None).strip()


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


def java_change_is_trivial(root, merge_base, head, path):
    """True when every added or removed line is blank, a comment, an import or the package line."""
    diff = git(root, "diff", "-U0", "--no-color", merge_base, head, "--", path).stdout.decode(errors="replace")
    for line in diff.splitlines():
        if line.startswith(("+++", "---")) or not line.startswith(("+", "-")):
            continue
        if not _JAVA_TRIVIAL_LINE.match(line[1:]):
            return False
    return True


# --- Checks ------------------------------------------------------------------

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
    exemptions = [
        v.strip() for v in git_text(
            root, "log", f"--format=%(trailers:key={EXEMPTION_TRAILER},valueonly)", f"{merge_base}..{head}"
        ).splitlines()
        if len(v.strip()) >= MIN_REASON_LENGTH
    ]
    if exemptions:
        return None
    shown = "\n".join(f"  - {p}" for p in behavioral[:10])
    more = f"\n  ... and {len(behavioral) - 10} more" if len(behavioral) > 10 else ""
    return (
        "These files change something visible or a behavior, but the branch adds or changes no unit test, "
        "GameTest or scenario (scripts/scenario_check/scenarios/*.json):\n"
        f"{shown}{more}\n"
        "Add a test or scenario that runs the change (AGENTS.md, Verification Reporting). When an existing "
        "test or scenario already covers it, or the change cannot alter what a player sees or does, add a "
        f"commit trailer `{EXEMPTION_TRAILER}: <reason>` that names the covering test or scenario or explains "
        "why. \"Could not check in game\" is not a reason: write a scenario instead."
    )


def check_verification(root, merge_base, head):
    """Check 2. Returns a problem message or None."""
    vdir = Path(root) / VERIFICATION_DIR
    patch = vdir / "diff.patch"
    review = vdir / "review.md"
    current = git(root, "diff", merge_base, head).stdout
    how = (
        "Run the independent-verification skill (.claude/skills/independent-verification/SKILL.md): write "
        f"{VERIFICATION_DIR}/diff.patch, dispatch the fresh reviewer subagent, and save its report verbatim "
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


def push_head(root, args):
    """The local revision a push sends, or None when the push sends no branch to review."""
    if any(a in ("--delete", "-d", "--tags", "--dry-run", "-n") for a in args):
        return None
    positional = [a for a in args if not a.startswith("-")]
    current = git(root, "symbolic-ref", "--quiet", "--short", "HEAD", check=False).stdout.decode().strip()
    refspecs = positional[1:]
    if not refspecs:
        return None if current in PROTECTED_BRANCHES or not current else "HEAD"
    if len(refspecs) > 1:
        return None
    src, _, dst = refspecs[0].lstrip("+").partition(":")
    if not src or src.startswith("refs/tags/"):
        return None
    target = (dst or src).removeprefix("refs/heads/")
    if target in PROTECTED_BRANCHES or src in PROTECTED_BRANCHES:
        return None
    if src == "HEAD" and current in PROTECTED_BRANCHES:
        return None
    if git(root, "rev-parse", "--verify", "--quiet", src + "^{commit}", check=False).returncode != 0:
        return None
    return src


def evaluate(command, cwd):
    """Return the deny message for a Bash command, or None to let it run."""
    found = find_commands(command)
    if not found:
        return None
    reason = skip_reason(command)
    if reason is not None:
        if len(reason) < 10:
            return f"{SKIP_VAR} needs a reason of at least 10 characters that tells the owner why the gate is skipped."
        return None
    problems = []
    for kind, args, git_dir in found:
        where = os.path.join(cwd, git_dir) if git_dir else cwd
        top = git(where, "rev-parse", "--show-toplevel", check=False)
        if top.returncode != 0:
            continue
        root = top.stdout.decode().strip()
        # Only this repository is gated, not gradle/shared or any other clone.
        if not (Path(root) / "scripts" / "merge_tier" / "classify.py").exists():
            continue
        if kind == "push":
            head = push_head(root, args)
            base_branch = "main"
        else:
            head = "HEAD"
            base_branch = option_value(args, "--base", "-B") or "main"
        if head is None:
            continue
        base = resolve_base(root, base_branch)
        merge_base = git_text(root, "merge-base", base, head)
        if merge_base == git_text(root, "rev-parse", head):
            continue
        for problem in (check_tests(root, merge_base, head), check_verification(root, merge_base, head)):
            if problem:
                problems.append(problem)
        if kind == "pr":
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
