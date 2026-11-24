#!/usr/bin/env python3
"""Classify a diff into the merge tiers of docs/merge_policy.md.

The file rules live in rules.json next to this script. docs/merge_policy.md
renders the same rules between its merge-tier-rules markers, so the document
and this check cannot drift apart (see --check-doc).

Usage:
  classify.py [--base REV] [--head REV] [--body-file FILE] [--summary FILE]
  classify.py --check-doc | --write-doc

Exit codes: 0 when the declared tier is at least the computed one (or nothing
is declared), 1 when the declared tier is lower or the document is stale,
2 on a usage or Git error.
"""

import argparse
import json
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

TIERS = "ABCD"
RULES_PATH = Path(__file__).with_name("rules.json")
REPO_ROOT = Path(__file__).resolve().parents[2]
DOC_PATH = REPO_ROOT / "docs" / "merge_policy.md"
DOC_BEGIN = "<!-- merge-tier-rules:begin (generated from scripts/merge_tier/rules.json) -->"
DOC_END = "<!-- merge-tier-rules:end -->"
# Repository to diff. Defaults to the checkout this script is in, and --repo
# overrides it when CI runs the base branch's copy from outside the checkout.
GIT_ROOT = None


def glob_to_regex(pattern):
    """Translate a path glob. `**/` and `/**` span directories, `*` and `?` do not."""
    out = []
    i = 0
    while i < len(pattern):
        if pattern.startswith("**/", i):
            out.append("(?:.*/)?")
            i += 3
        elif pattern.startswith("/**", i) and i + 3 == len(pattern):
            out.append("/.*")
            i += 3
        elif pattern.startswith("**", i):
            out.append(".*")
            i += 2
        elif pattern[i] == "*":
            out.append("[^/]*")
            i += 1
        elif pattern[i] == "?":
            out.append("[^/]")
            i += 1
        else:
            out.append(re.escape(pattern[i]))
            i += 1
    return re.compile("".join(out) + r"\Z")


@dataclass
class Rule:
    tier: str
    note: str
    patterns: list
    check: str = ""
    deleted_tier: str = ""

    def __post_init__(self):
        for tier in (self.tier, self.deleted_tier or self.tier):
            if tier not in TIERS:
                raise ValueError(f"unknown tier {tier!r} in rules.json")
        self._regexes = [glob_to_regex(p) for p in self.patterns]

    def matches(self, path):
        return any(r.match(path) for r in self._regexes)


def load_rules(path=RULES_PATH):
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    rules = [
        Rule(r["tier"], r["note"], r["patterns"], r.get("check", ""), r.get("deleted_tier", ""))
        for r in data["rules"]
    ]
    default = Rule(data["default"]["tier"], data["default"]["note"], [])
    return rules, default


@dataclass
class Change:
    status: str  # A, M or D, renames are split into D and A
    path: str


@dataclass
class Result:
    path: str
    status: str
    tier: str
    reason: str


def lang_keys(text):
    try:
        data = json.loads(text)
    except ValueError:
        return None
    return set(data) if isinstance(data, dict) else None


def classify_lang(change, rule, read_base, read_head):
    if change.status == "D":
        return "C", "lang file deleted"
    if change.status == "A":
        return rule.tier, "lang file added"
    base = lang_keys(read_base(change.path))
    head = lang_keys(read_head(change.path))
    if base is None or head is None:
        return "C", "lang file is not a JSON object"
    removed = sorted(base - head)
    if removed:
        shown = ", ".join(removed[:3]) + (" ..." if len(removed) > 3 else "")
        return "C", f"lang keys removed or renamed: {shown}"
    return rule.tier, "lang values changed, no key removed"


# The classifier and its rules are tier D whatever rules.json says, so a pull
# request cannot lower its own tier by editing them. CI also runs the base
# branch's copy of this script, see .github/workflows/merge-tier.yml.
SELF_PATTERN = glob_to_regex("scripts/merge_tier/**")


def classify(changes, rules, default, read_base=None, read_head=None):
    results = []
    for change in changes:
        rule = next((r for r in rules if r.matches(change.path)), None)
        if SELF_PATTERN.match(change.path):
            results.append(Result(change.path, change.status, "D", "the tier classifier itself"))
        elif rule is None:
            results.append(Result(change.path, change.status, default.tier, default.note))
        elif rule.check == "lang":
            tier, reason = classify_lang(change, rule, read_base, read_head)
            results.append(Result(change.path, change.status, tier, reason))
        elif change.status == "D" and rule.deleted_tier:
            results.append(Result(change.path, change.status, rule.deleted_tier, f"deleted: {rule.note}"))
        else:
            results.append(Result(change.path, change.status, rule.tier, rule.note))
    return results


def overall_tier(results):
    """An empty diff is tier A: nothing in it can be one-way."""
    return max((r.tier for r in results), key=TIERS.index, default="A")


_DECLARATION = re.compile(r"^merge tier\s*:\s*([abcd])\b", re.IGNORECASE)


def declared_tiers(body):
    """Return the tiers declared with `Merge tier: X` lines, ignoring Markdown emphasis."""
    found = []
    for line in (body or "").splitlines():
        plain = re.sub(r"[*_`>]", "", line).strip()
        plain = re.sub(r"^[-+]\s+", "", plain)
        m = _DECLARATION.match(plain)
        if m:
            found.append(m.group(1).upper())
    return found


def check_declaration(body, computed):
    """Return (ok, message) for the tier declared in a pull request body."""
    declared = sorted(set(declared_tiers(body)), key=TIERS.index)
    if not declared:
        return True, (
            f"No `Merge tier:` line in the pull request body. The diff is tier {computed}. "
            "Add `Merge tier: " + computed + "` to the body (required before an agent auto-merges tier A)."
        )
    if len(declared) > 1:
        return False, f"The body declares more than one tier ({', '.join(declared)}). Keep one `Merge tier:` line."
    tier = declared[0]
    if TIERS.index(tier) < TIERS.index(computed):
        return False, (
            f"The body declares `Merge tier: {tier}`, but the diff is tier {computed}. "
            f"Declare tier {computed} or higher, or move the higher-tier files to another pull request."
        )
    return True, f"Declared tier {tier}, the diff is tier {computed}."


# Verification paths are separate from tier rules: tier C also includes build
# files, while translations and test sources do not need a new gameplay test.
_TEST_PATHS = [glob_to_regex(p) for p in (
    "**/src/test/**", "**/src/gametest/**", "common/gametest/**",
    "**/java/**/gametest/**", "scripts/scenario_check/scenarios/**",
)]
_BEHAVIOR_PATHS = [glob_to_regex(p) for p in (
    "**/*.java", "**/resources/assets/**", "**/resources/data/**",
    "**/*.bbmodel", "**/*mixins*.json", "**/*.accesswidener",
)]
_NON_BEHAVIOR_PATHS = [glob_to_regex(p) for p in (
    "**/assets/*/lang/**", "**/package-info.java",
)]
_UNVERIFIED = re.compile(r"not verified in.game|実機未確認", re.IGNORECASE)
_REASON = re.compile(r"^\s*(?:[-*]\s*)?(?:\*\*)?([\w -]+)(?:\*\*)?:\s*(.*?)\s*$", re.IGNORECASE)


def check_verification(changes, body):
    """Return deterministic problems; reasons are declarations, not proof of testing."""
    matches = lambda patterns, path: any(p.match(path) for p in patterns)
    test = any(c.status != "D" and matches(_TEST_PATHS, c.path) for c in changes)
    behavior = [c.path for c in changes if not matches(_TEST_PATHS, c.path)
                and not matches(_NON_BEHAVIOR_PATHS, c.path) and matches(_BEHAVIOR_PATHS, c.path)]
    lines = (body or "").splitlines()
    reasons = []
    for line in lines:
        match = _REASON.match(line)
        if match:
            reasons.append((match.group(1).lower(), match.group(2)))
    problems = []
    if behavior and not test and not any(key == "test-exemption" and len(value.strip()) >= 20
                                         for key, value in reasons):
        problems.append("Behavior paths changed without a test or scenario: " + ", ".join(behavior[:10])
                        + ". Add one or provide `Test-Exemption: <reason>` (at least 20 characters).")
    for line in lines:
        if _UNVERIFIED.search(line):
            match = _REASON.match(line)
            if not match or match.group(1).lower() != "manual checks" or len(_UNVERIFIED.sub("", match.group(2)).strip(" :-.,")) < 20:
                problems.append("Put each unverified-in-game statement on a `Manual checks: <statement and reason>` line with a reason of at least 20 characters.")
                break
    return problems


# --- Git ---------------------------------------------------------------------

def git(*args, check=True):
    proc = subprocess.run(["git", *args], cwd=GIT_ROOT or REPO_ROOT, capture_output=True, text=True)
    if check and proc.returncode != 0:
        raise RuntimeError(f"git {' '.join(args)} failed: {proc.stderr.strip()}")
    return proc


def git_changes(base, head):
    merge_base = git("merge-base", base, head).stdout.strip()
    out = git("diff", "--name-status", "--no-renames", "-z", merge_base, head).stdout
    fields = [f for f in out.split("\0") if f]
    changes = []
    for status, path in zip(fields[0::2], fields[1::2]):
        # T (type change) and the rest behave like a modification here.
        changes.append(Change(status[0] if status[0] in "AD" else "M", path))
    return merge_base, changes


def git_reader(rev):
    def read(path):
        proc = git("show", f"{rev}:{path}", check=False)
        return proc.stdout if proc.returncode == 0 else ""
    return read


# --- Document ----------------------------------------------------------------

def render_doc_table(rules, default):
    lines = [
        DOC_BEGIN,
        "| # | Tier | Paths | What |",
        "|---|---|---|---|",
    ]
    for i, rule in enumerate(rules, 1):
        paths = ", ".join(f"`{p}`" for p in rule.patterns)
        tier = rule.tier + (f" ({rule.deleted_tier} when deleted)" if rule.deleted_tier else "")
        lines.append(f"| {i} | {tier} | {paths} | {rule.note} |")
    lines.append("| - | D | `scripts/merge_tier/**` | Built into the classifier, so a pull request cannot lower its own tier by editing rules.json |")
    lines.append(f"| - | {default.tier} | anything else | {default.note} |")
    lines.append(DOC_END)
    return "\n".join(lines)


def replace_doc_table(text, table):
    start = text.find(DOC_BEGIN)
    end = text.find(DOC_END)
    if start < 0 or end < start:
        raise ValueError(f"{DOC_PATH.name} has no merge-tier-rules markers")
    return text[:start] + table + text[end + len(DOC_END):]


# --- CLI ---------------------------------------------------------------------

def format_report(results, computed, base_label):
    lines = [f"## Merge tier: {computed}", "", f"Files changed since `{base_label}`:", ""]
    if not results:
        lines.append("(no changes)")
    else:
        lines += ["| Tier | Status | Path | Reason |", "|---|---|---|---|"]
        order = sorted(results, key=lambda r: (-TIERS.index(r.tier), r.path))
        lines += [f"| {r.tier} | {r.status} | `{r.path}` | {r.reason} |" for r in order]
    return "\n".join(lines) + "\n"


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base", default="origin/main", help="base revision (default: origin/main)")
    parser.add_argument("--head", default="HEAD", help="head revision (default: HEAD)")
    parser.add_argument("--body-file", help="pull request body to check the `Merge tier:` line against")
    parser.add_argument("--repo", help="repository to diff (default: the checkout this script is in)")
    parser.add_argument("--summary", help="append the Markdown report to this file (e.g. $GITHUB_STEP_SUMMARY)")
    parser.add_argument("--check-doc", action="store_true", help="fail when docs/merge_policy.md is stale")
    parser.add_argument("--write-doc", action="store_true", help="regenerate the rules table in docs/merge_policy.md")
    args = parser.parse_args(argv)

    global GIT_ROOT
    GIT_ROOT = args.repo
    rules, default = load_rules()

    if args.check_doc or args.write_doc:
        text = DOC_PATH.read_text(encoding="utf-8")
        updated = replace_doc_table(text, render_doc_table(rules, default))
        if args.write_doc:
            DOC_PATH.write_text(updated, encoding="utf-8")
            return 0
        if updated != text:
            print(f"{DOC_PATH.relative_to(REPO_ROOT)} is stale. Run: python3 scripts/merge_tier/classify.py --write-doc")
            return 1
        return 0

    try:
        merge_base, changes = git_changes(args.base, args.head)
    except RuntimeError as e:
        print(e, file=sys.stderr)
        return 2
    results = classify(changes, rules, default, git_reader(merge_base), git_reader(args.head))
    computed = overall_tier(results)
    report = format_report(results, computed, args.base)
    print(report)

    ok = True
    if args.body_file:
        body = Path(args.body_file).read_text(encoding="utf-8")
        ok, message = check_declaration(body, computed)
        print(message)
        report += "\n" + message + "\n"
        problems = check_verification(changes, body)
        for problem in problems:
            print(problem)
            report += "\n" + problem + "\n"
        ok = ok and not problems
    if args.summary:
        with open(args.summary, "a", encoding="utf-8") as f:
            f.write(report)
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
