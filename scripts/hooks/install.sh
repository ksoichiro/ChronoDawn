#!/usr/bin/env bash
# Registers scripts/hooks/pre-commit as a config-based Git hook
# (Git 2.54+, `git help hook`). Unlike core.hooksPath it leaves other hooks
# in place, and because the config lives in the shared .git/config, every
# worktree of this clone runs its own checkout's copy of the script.
set -euo pipefail

name=chronodawn-pre-commit
# Worktrees on branches without the script commit as before.
command='if [ -x scripts/hooks/pre-commit ]; then exec scripts/hooks/pre-commit; fi'

version=$(git --version | sed -E 's/^git version ([0-9]+)\.([0-9]+).*/\1 \2/')
read -r major minor <<<"$version"
if [ "$major" -lt 2 ] || { [ "$major" -eq 2 ] && [ "$minor" -lt 54 ]; }; then
    echo "Git 2.54 or newer is required for config-based hooks (found $(git --version))." >&2
    echo "With an older Git, run the checks by hand before committing:" >&2
    echo "  scripts/hooks/pre-commit" >&2
    exit 1
fi

if [ "${1:-}" = "--uninstall" ]; then
    git config unset --all "hook.$name.command" || true
    git config unset --all "hook.$name.event" || true
    echo "Removed hook $name."
    exit 0
fi

git config set "hook.$name.command" "$command"
git config unset --all "hook.$name.event" >/dev/null 2>&1 || true
git config set --append "hook.$name.event" pre-commit
echo "Installed hook $name. Check with: git hook list pre-commit"
