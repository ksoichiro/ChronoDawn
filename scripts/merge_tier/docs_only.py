#!/usr/bin/env python3
"""Tell CI whether a diff changes only documentation.

A file counts as documentation when it matches a pattern of a rule marked
`"ci_docs_only": true` in rules.json, the same patterns that make docs tier A.
CI then skips the steps of its build and test jobs but still runs every job,
so the required checks on `main` report success instead of going missing
(docs/merge_policy.md).

Usage:
  docs_only.py --base REV --head REV [--repo DIR] [--output FILE] [--summary FILE]

Prints `docs_only=true` or `docs_only=false`, and appends the same line to
--output (e.g. $GITHUB_OUTPUT). An empty diff counts as not docs-only, so an
unexpected state runs the full CI. Exit codes: 0 on success, 2 on a usage or
Git error.
"""

import argparse
import json
import sys
from pathlib import Path

if __package__:
    from . import classify as c
else:  # run as a script: python3 scripts/merge_tier/docs_only.py
    import classify as c


def docs_patterns(path=c.RULES_PATH):
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    return [p for r in data["rules"] if r.get("ci_docs_only") for p in r["patterns"]]


def non_docs_paths(paths, patterns):
    regexes = [c.glob_to_regex(p) for p in patterns]
    return [p for p in paths if not any(r.match(p) for r in regexes)]


def is_docs_only(paths, patterns):
    return bool(paths) and not non_docs_paths(paths, patterns)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base", required=True, help="base revision, diffed from its merge base with --head")
    parser.add_argument("--head", required=True, help="head revision")
    parser.add_argument("--repo", help="repository to diff (default: the checkout this script is in)")
    parser.add_argument("--output", help="append `docs_only=...` to this file (e.g. $GITHUB_OUTPUT)")
    parser.add_argument("--summary", help="append a Markdown note to this file (e.g. $GITHUB_STEP_SUMMARY)")
    args = parser.parse_args(argv)

    c.GIT_ROOT = args.repo
    try:
        _, changes = c.git_changes(args.base, args.head)
    except RuntimeError as e:
        print(e, file=sys.stderr)
        return 2
    paths = [ch.path for ch in changes]
    patterns = docs_patterns()
    docs_only = is_docs_only(paths, patterns)

    line = f"docs_only={'true' if docs_only else 'false'}"
    print(line)
    if args.output:
        with open(args.output, "a", encoding="utf-8") as f:
            f.write(line + "\n")
    if args.summary:
        if docs_only:
            note = f"Only documentation changed ({len(paths)} files), so the build and test steps are skipped.\n"
        elif not paths:
            note = "The diff is empty, so every job runs.\n"
        else:
            others = non_docs_paths(paths, patterns)
            note = "Files other than documentation changed, so every job runs. First ones:\n\n"
            note += "".join(f"- `{p}`\n" for p in others[:10])
        with open(args.summary, "a", encoding="utf-8") as f:
            f.write(note)
    return 0


if __name__ == "__main__":
    sys.exit(main())
