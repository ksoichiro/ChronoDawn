#!/usr/bin/env python3
"""Run the mechanical half of the morning check in docs/merge_policy.md.

Lists the pull requests merged since a date whose body declares
`Merge tier: A`, re-classifies each one from its final head, and checks that
its body has a non-empty Verification section. The other half, reading each
diff for what file paths cannot show, stays with the agent or the owner.

Usage:
  morning_check.py [--since YYYY-MM-DD] [--repo DIR] [--remote NAME]

Exit codes: 0 when every tier A pull request passes (or there is none),
1 when one is misclassified or lacks a Verification section, 2 on a gh or
Git error.
"""

import argparse
import datetime
import json
import re
import subprocess
import sys
from dataclasses import dataclass, field

if __package__:
    from . import classify as c
else:  # run as a script: python3 scripts/merge_tier/morning_check.py
    import classify as c

PR_FIELDS = "number,title,url,body,baseRefOid,headRefOid,mergedAt"
_HEADING = re.compile(r"^(#{1,6})\s+(.*?)\s*#*\s*$")


@dataclass
class Finding:
    number: int
    title: str
    url: str
    tier: str = "A"
    problems: list = field(default_factory=list)


def verification_section(body):
    """Return the lines of the first `Verification` section, or None without one.

    The section runs to the next heading of the same or a higher level.
    """
    lines = (body or "").splitlines()
    for i, line in enumerate(lines):
        m = _HEADING.match(line)
        if m and m.group(2).strip().lower() == "verification":
            level = len(m.group(1))
            section = []
            for rest in lines[i + 1:]:
                n = _HEADING.match(rest)
                if n and len(n.group(1)) <= level:
                    break
                section.append(rest)
            return section
    return None


def verification_problem(body):
    """Return a problem message when the Verification section is missing or empty."""
    section = verification_section(body)
    if section is None:
        return "The body has no Verification section."
    # The `Merge tier:` line alone does not record any verification.
    content = [l for l in section if l.strip() and not c.declared_tiers(l)]
    if not content:
        return "The Verification section is empty."
    return None


def declares_tier_a(body):
    return "A" in c.declared_tiers(body)


def assess(pr, computed):
    """Return the Finding for one merged pull request and the tier its diff has now."""
    finding = Finding(pr["number"], pr["title"], pr["url"], computed)
    if computed != "A":
        finding.problems.append(f"Declared tier A, but the diff of its final head is tier {computed}.")
    problem = verification_problem(pr.get("body"))
    if problem:
        finding.problems.append(problem)
    return finding


# --- gh and Git --------------------------------------------------------------

def run(cmd, cwd):
    proc = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True)
    if proc.returncode != 0:
        raise RuntimeError(f"{' '.join(cmd)} failed: {proc.stderr.strip()}")
    return proc.stdout


def merged_prs(since, cwd):
    # The search narrows the list, and declares_tier_a() drops the bodies that
    # only mention the phrase without declaring tier A.
    out = run(["gh", "pr", "list", "--state", "merged", "--limit", "200",
               "--search", f'merged:>={since} "Merge tier: A" in:body',
               "--json", PR_FIELDS], cwd)
    return [pr for pr in json.loads(out) if declares_tier_a(pr.get("body"))]


def fetch_pr(pr, remote, cwd):
    # GitHub keeps the head of a pull request after its branch is deleted.
    run(["git", "fetch", "--quiet", remote, f"pull/{pr['number']}/head", pr["baseRefOid"]], cwd)


def classify_pr(pr):
    rules, default = c.load_rules()
    merge_base, changes = c.git_changes(pr["baseRefOid"], pr["headRefOid"])
    results = c.classify(changes, rules, default, c.git_reader(merge_base), c.git_reader(pr["headRefOid"]))
    return c.overall_tier(results)


# --- CLI ---------------------------------------------------------------------

def format_report(since, findings):
    lines = [f"## Morning check: tier A pull requests merged since {since}", ""]
    if not findings:
        lines.append("No pull request merged since then declares `Merge tier: A`.")
        return "\n".join(lines) + "\n"
    lines += ["| PR | Tier now | Result |", "|---|---|---|"]
    for f in findings:
        result = " ".join(f.problems) if f.problems else "OK"
        lines.append(f"| [#{f.number}]({f.url}) {f.title} | {f.tier} | {result} |")
    bad = [f for f in findings if f.problems]
    lines += ["", f"{len(bad)} of {len(findings)} need attention." if bad else "All passed the mechanical check."]
    lines.append("Still read each diff for what file paths cannot show (docs/merge_policy.md).")
    return "\n".join(lines) + "\n"


def main(argv=None, today=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--since", help="first merge date to check, YYYY-MM-DD (default: yesterday)")
    parser.add_argument("--repo", help="repository to fetch into and diff (default: the checkout this script is in)")
    parser.add_argument("--remote", default="origin", help="remote to fetch pull request heads from (default: origin)")
    args = parser.parse_args(argv)

    today = today or datetime.date.today()
    since = args.since or (today - datetime.timedelta(days=1)).isoformat()
    try:
        datetime.date.fromisoformat(since)
    except ValueError:
        print(f"--since must be YYYY-MM-DD, got {since!r}", file=sys.stderr)
        return 2

    c.GIT_ROOT = args.repo
    cwd = args.repo or c.REPO_ROOT
    findings = []
    try:
        for pr in merged_prs(since, cwd):
            fetch_pr(pr, args.remote, cwd)
            findings.append(assess(pr, classify_pr(pr)))
    except (RuntimeError, json.JSONDecodeError) as e:
        print(e, file=sys.stderr)
        return 2

    print(format_report(since, findings), end="")
    return 1 if any(f.problems for f in findings) else 0


if __name__ == "__main__":
    sys.exit(main())
