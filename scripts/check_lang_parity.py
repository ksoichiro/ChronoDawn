#!/usr/bin/env python3
"""Verify non-English lang files share keys with en_us.json.

Scans every source lang directory under common/*/src/main/resources/assets/chronodawn/lang/.
For each directory, en_us.json is the reference and missing/extra keys are reported for
every other <locale>.json. Exits 1 if any locale has key drift; 0 otherwise.

Usage:
    python3 scripts/check_lang_parity.py
"""

import json
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
LANG_GLOB = "common/*/src/main/resources/assets/chronodawn/lang"
REFERENCE_FILE = "en_us.json"


def find_lang_dirs():
    return sorted(p for p in REPO_ROOT.glob(LANG_GLOB) if p.is_dir())


def load_keys(json_path):
    with json_path.open(encoding="utf-8") as f:
        data = json.load(f)
    return set(data.keys())


def check_dir(lang_dir):
    """Return (problem_locale_count, lines_to_print)."""
    ref = lang_dir / REFERENCE_FILE
    if not ref.exists():
        return 0, [f"  (no {REFERENCE_FILE}, skipping)"]

    ref_keys = load_keys(ref)
    locales = sorted(p for p in lang_dir.glob("*.json") if p.name != REFERENCE_FILE)
    if not locales:
        return 0, [f"  (only {REFERENCE_FILE} present)"]

    out = []
    bad = 0
    for locale_path in locales:
        locale_keys = load_keys(locale_path)
        missing = sorted(ref_keys - locale_keys)
        extra = sorted(locale_keys - ref_keys)
        if not missing and not extra:
            out.append(f"  {locale_path.name}: OK ({len(locale_keys)} keys)")
            continue
        bad += 1
        out.append(f"  {locale_path.name}: drift "
                   f"(missing {len(missing)}, extra {len(extra)})")
        for k in missing:
            out.append(f"    - missing: {k}")
        for k in extra:
            out.append(f"    + extra:   {k}")
    return bad, out


def main():
    dirs = find_lang_dirs()
    if not dirs:
        print("No lang directories found under "
              f"{LANG_GLOB} (relative to {REPO_ROOT}).", file=sys.stderr)
        return 2

    total_bad = 0
    for d in dirs:
        rel = d.relative_to(REPO_ROOT)
        print(f"=== {rel} ===")
        n, lines = check_dir(d)
        for line in lines:
            print(line)
        total_bad += n
        print()

    if total_bad > 0:
        print(f"FAIL: {total_bad} locale file(s) have key drift vs {REFERENCE_FILE}.",
              file=sys.stderr)
        return 1
    print(f"OK: all locale files have parity with {REFERENCE_FILE}.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
