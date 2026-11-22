#!/usr/bin/env python3
"""Claude Code PostToolUse hook for Edit/Write/MultiEdit.

When the edited file sits in a version module (common/<v>, fabric/<v>,
neoforge/<v>, forge/<v>) and gradle/version-sync.lock records other versions
whose copy was identical, tell the agent which of those copies now differ.
validateVersionSync fails on the same condition later. This hint arrives at
edit time, when applying the change to every copy is still cheap.

Prints nothing (and exits 0) in every other case, so it never blocks an edit.
"""
import json
import os
import re
import sys

TOPS = ("common", "fabric", "neoforge", "forge")


def main() -> int:
    try:
        payload = json.load(sys.stdin)
    except ValueError:
        return 0
    path = (payload.get("tool_input") or {}).get("file_path")
    root = os.environ.get("CLAUDE_PROJECT_DIR") or payload.get("cwd") or os.getcwd()
    if not path:
        return 0
    rel = os.path.relpath(os.path.abspath(path), os.path.abspath(root)).replace(os.sep, "/")
    m = re.match(r"^(%s)/(\d[^/]*)/src/(.+)$" % "|".join(TOPS), rel)
    lock = os.path.join(root, "gradle", "version-sync.lock")
    if not m or not os.path.exists(lock):
        return 0
    top, version, inner = m.groups()
    key = f"{top}/*/src/{inner}"
    groups = None
    with open(lock, encoding="utf-8") as f:
        for line in f:
            if line.startswith(key + " = "):
                groups = [g.split(",") for g in line[len(key) + 3:].strip().split(" | ")]
                break
    if not groups:
        return 0
    siblings = next((g for g in groups if version in g), [])
    siblings = [v for v in siblings if v != version]
    if not siblings:
        return 0
    try:
        with open(path, "rb") as f:
            current = f.read()
    except OSError:
        return 0
    stale = []
    for v in siblings:
        other = os.path.join(root, top, v, "src", inner)
        try:
            with open(other, "rb") as f:
                if f.read() != current:
                    stale.append(v)
        except OSError:
            stale.append(v)
    if not stale:
        return 0
    message = (
        f"{rel} was byte-identical in {top} {', '.join(siblings)} (gradle/version-sync.lock). "
        f"These copies now differ: {', '.join(stale)}. Apply the same change to "
        f"{top}/<version>/src/{inner} for each of them, unless {version} needs to differ "
        "for an era-specific reason. In that case run ./gradlew updateVersionSyncLock "
        "and commit the lock. validateVersionSync fails until one of the two is done."
    )
    print(json.dumps({"hookSpecificOutput": {"hookEventName": "PostToolUse", "additionalContext": message}}))
    return 0


if __name__ == "__main__":
    sys.exit(main())
