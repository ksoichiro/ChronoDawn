---
name: pr-gate
description: Use when `git push` or `gh pr create` in Chrono Dawn was denied by the PR gate hook, or before pushing a branch for a pull request. Explains what scripts/hooks/claude_pr_gate.py checks (a test or scenario with every behavior change, the independent verification on the current diff, the Verification section in the PR body), how to satisfy each check, and the two ways to pass it with a written reason.
---

# PR gate

`.claude/settings.json` runs `scripts/hooks/claude_pr_gate.py` before every
Bash command. It acts only on `git push` and `gh pr create` in this
repository, and denies the command with the reason when a check fails. The
reason arrives as the tool result. Fix what it names and run the command
again. Do not work around the hook with another command that pushes.

## What it checks

The diff is `git diff $(git merge-base origin/main HEAD) HEAD`. `gh pr create
--base <branch>` uses that branch instead, and `git push` uses the branch
named in `build/verification/base` when the file exists.

1. **A test or scenario with every behavior change.** A changed `*.java`
   file, a file under `src/main/resources/assets/` or `src/main/resources/data/`,
   a `*.bbmodel`, a Mixin config (`*mixins*.json`) or an `*.accesswidener`
   needs a changed or added file under `src/test/`, `src/gametest/`,
   `common/gametest/` or `scripts/scenario_check/scenarios/` in the same
   branch. Not counted as behavior: lang files, `package-info.java`, Java
   edits that change only comments or whitespace, and loader metadata
   (`fabric.mod.json`, `META-INF/`, `pack.mcmeta`), which `prodSmokeTest`
   checks instead. Commented-out code and changed imports count as code.
   Deleting a test does not count as a test.
2. **The independent verification ran on this diff.**
   `build/verification/diff.patch` must equal the branch diff, and
   `build/verification/review.md` must hold the reviewer's report (with its
   `Commands run:` list) and be newer than the patch. Any commit after the
   review makes the patch stale, so review last.
3. **The PR body (`gh pr create` only).** The body from `--body`,
   `--body "$(cat <file>)"` or `--body-file` has a `## Verification` heading and a `Merge tier:` line.
   `--fill` and `--web` bodies do not.

`gh pr create --head <branch>` checks that branch instead of `HEAD`. The
alias `gh pr new` is checked the same way, and `--help` is never gated.

Pushes that send no branch for review pass: `main`, tags, `--delete`,
`--dry-run`, a branch with no commits ahead of the base, and other
repositories such as `gradle/shared` (reached with `git -C`, `--work-tree`
or a preceding `cd`/`pushd`). `--all`, `--branches` and `--mirror` check
every local branch except `main`.

The gate finds `git push` and `gh pr create` anywhere in a command, also
behind wrappers (`timeout`, `env`), keywords, `bash -c` and `eval`. It exists
to catch a forgotten step, so it parses commands the way an agent writes them
and does not try to be a sandbox. Never route a push around it.

Known limits: a `<<` inside a quoted string is read as a heredoc and hides
the lines after it, and a body passed through stdin (`--body-file -`) or a
shell variable (`--body "$BODY"`) is not read, so such a `gh pr create` is
denied. Pass the body with `--body-file <path>`.

## Satisfying the checks

- Check 1: add a unit test that evaluates the output values, or a scenario
  (`scenario-check` skill, `docs/scenario_check.md`) that runs the change.
  When an existing test or scenario already covers the change, or the change
  cannot alter what a player sees or does (a rename, a refactor proven by
  existing tests), add a trailer to the last commit that changes those files.
  It covers only the files of that commit, and a later commit that changes
  them again needs its own trailer:

  ```text
  Test-Exemption: covered by the existing time-compass-needle scenario
  ```

  The line may sit in any paragraph of the message, also before the
  `Co-Authored-By:` paragraph. The reason needs at least 20 characters and must name the covering test or
  scenario, or say why nothing a player sees can change. "Could not check in
  game" is never a reason: AGENTS.md asks for a scenario instead. The owner
  reads the trailer in the commit history.
- Check 2: run the `independent-verification` skill. It writes the patch,
  dispatches the fresh reviewer and saves its report to
  `build/verification/review.md`. The reviewer prompt asks it to compare
  vanilla-derived logic with vanilla and to run the change, so this is also
  how the separate review gets started. A hook cannot start a subagent.
- Check 3: write the body as step 4 of the `independent-verification` skill
  shows. Put it in a git-ignored file in the checkout (such as
  `build/pr-body.md`) and pass `--body-file`, since `$TMPDIR` differs inside
  and outside the sandbox.

## Skipping the gate

Prefix the command with a reason of at least 10 characters:

```bash
CHRONODAWN_PR_GATE_SKIP="back up work in progress, no PR yet" git push -u origin <branch>
```

Use it only for a push that is not on its way to a pull request, or when the
owner asked for it. The reason stays in the transcript. Never use it to open a
pull request that the checks would stop.

## Role of CI

The gate is local and for agents. It needs `build/verification/`, which only
exists in the checkout that ran the review. The `Merge tier` CI check stays
the shared floor for every pull request, including ones from contributors
without the hook: it compares the declared tier with the diff's tier
(`docs/merge_policy.md`).

## Changing the gate

The script and its tests are `scripts/hooks/claude_pr_gate.py` and
`scripts/hooks/tests/test_claude_pr_gate.py`. Run the tests with:

```bash
python3 -m unittest discover -s scripts/hooks/tests -t .
```

CI runs them in the `Merge tier` workflow. Keep the path lists in this skill
in sync with the script.
