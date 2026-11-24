# Chrono Dawn Development Guidelines

@AGENTS.md

The shared project instructions are in `AGENTS.md`, imported above. Change shared
rules there. This file holds only Claude Code specifics, and it is loaded into
every session, so keep it short. Put task-specific procedures in
`.claude/skills/` and link them from here.

## Skills

Read the matching skill before the work:

- `verification-commands`: build, clean, run, test, validate and release commands, TLA+ model checking, `prodSmokeTest`, `upgradeTest`, `scenarioCheck`, `checkAll`, parallel verification limits, CI notes
- `multiversion-support`: era-specific API differences between Minecraft versions
- `mixin-guide`: adding and verifying Mixins
- `doc-maintenance`: which docs to update for versions, content, dependencies and releases
- `license-compliance`: LGPL-3.0 rules for dependencies and assets
- `scenario-check`: adding or editing `scripts/scenario_check/scenarios/*.json`
- `independent-verification`: required before a PR (`docs/merge_policy.md`)
- `pr-gate`: when the hook denies `git push` or `gh pr create`, and before pushing a branch for a PR

## Before a PR

- Run the `independent-verification` skill and put its Verification section with a `Merge tier:` line in the PR body.
- Follow the test, vanilla-comparison and scenario rules under "Verification Reporting" in `AGENTS.md`.
- A new bug class goes into `docs/bug_prevention.md` first. When a Minecraft rename breaks a version that CI does not build, add a rule to `scripts/era_symbol_rules.json` in the same change.

## Workflow

- Before committing, decide whether the change is testable. If so, present the verification method, the steps, and the expected result, and let the user decide whether to run it first.
- Save research results under `docs/` and track follow-up work in fuda with a link to that document.
- To try out git behavior on other commits, do not `git switch` or `git checkout` in this worktree. The sandbox keeps `.claude/settings.json`, `.claude/skills` and `.claude/hooks` read-only, so a checkout that changes them stops halfway and leaves a mix of two commits in the worktree. Clone into `$TMPDIR` with `git clone --template=` and run every step there with `git -C <clone>`.
