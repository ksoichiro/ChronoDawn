# Merge Policy and Independent Verification

This document defines how a pull request is verified before it is merged and
which pull requests the owner must merge by hand. The goal is that a change
that is cheap to undo can merge without waiting for the owner, while a change
that cannot be undone always gets a human decision.

Which check catches which bug class is recorded in
[bug_prevention.md](bug_prevention.md). This document decides which of those
checks a given kind of change must pass.

## Change tiers

Classify a pull request by its riskiest file. A pull request that mixes tiers
takes the highest one.

| Tier | Kind of change | Merge |
|---|---|---|
| A: two-way | Docs, comments, `CHANGELOG.md`, changed text values in lang files, tests and scripts that do not change what a gate checks | Auto-merge after the gates below |
| B: two-way, needs judgment | Balance and feel: boss strength, rewards, mob stats, drop rates, recipes, loot tables, tags, visual assets such as textures, models and sounds | Owner merges |
| C: one-way | Anything an existing world or a pack keeps (see below) | Owner merges, with the extra verification below |
| D: gate files | `CLAUDE.md`, `.claude/**`, skills, hooks, `.github/**`, `scripts/hooks/**`, validators, the tier classifier and this document | Owner merges. The fuda agent guard already stops agents from merging these |

### What makes a change one-way (tier C)

The test is the same as for the upgrade test: would an existing world, save
or modpack config break, lose data or change behavior after updating to this
build, and could a later release not simply undo it?

- Registry IDs: blocks, items, entities, block entities, effects, sounds,
  dimensions, and any rename or removal of one.
- Persisted data: `SavedData`, block entity and entity NBT, player data,
  structure NBT that is placed into worlds.
- Worldgen and biome JSON: structures, structure sets, template pools,
  processor lists, configured and placed features, noise settings, density
  functions, biome modifiers, dimension types, surface rules. These are data
  files, but chunks that are already generated keep the old result, so a
  later fix cannot repair them. This is the boundary case: **any JSON that
  decides how a chunk is generated is tier C even though it is only data**.
- Config files and config defaults, including their file names and keys.
- Portals, advancements, equipment and progression gating (tool tiers, unlock
  items, boss and structure gating).
- Mixin configs, `pack.mcmeta`, loader metadata and dependency ranges.
- Lang keys. Changing a value is tier A. Removing or renaming a key can break
  pack translations and is tier C.

A recipe or loot table that only adds an option is tier B. One that removes a
way to obtain a progression item is tier C.

### Classifying a diff by its files

`scripts/merge_tier/classify.py` applies the tiers above to the files in a
diff. Its path rules live in `scripts/merge_tier/rules.json`, and the table
below is generated from that file, so the document and the check read the
same list. To change a rule, edit `rules.json` and run
`python3 scripts/merge_tier/classify.py --write-doc`. The unit tests fail when
this table is stale.

```bash
python3 scripts/merge_tier/classify.py                       # diff of HEAD against origin/main
python3 scripts/merge_tier/classify.py --body-file body.md   # also check its `Merge tier:` line
```

The first rule whose pattern matches a path decides its tier, and the diff
takes the highest tier of its files. Deleting a test, a GameTest, a scenario
or an upgrade-test layer is tier D, because it weakens a gate. Lang files are
the one content check:
a lang file is tier A when the head keeps every key of the base, and tier C
when a key is removed or renamed or the file is deleted.

Paths alone cannot show every case. A Java change that only edits comments
still counts as tier B, a recipe that removes a way to obtain a progression
item still counts as tier B, and a Java change that alters NBT outside the
listed packages is not detected as tier C. The classifier sets a lower bound.
The author still raises the tier by hand when the content calls for it.

<!-- merge-tier-rules:begin (generated from scripts/merge_tier/rules.json) -->
| # | Tier | Paths | What |
|---|---|---|---|
| 1 | D | `CLAUDE.md`, `**/CLAUDE.md`, `AGENTS.md`, `**/AGENTS.md`, `.claude/**`, `.agents/**`, `.codex/**`, `**/SKILL.md` | Agent instructions and skills |
| 2 | D | `.github/**`, `scripts/hooks/**`, `scripts/merge_tier/**`, `docs/merge_policy.md` | CI, Git hooks and this classifier |
| 3 | D | `gradle/shared`, `gradle/shared/**`, `gradle/chronodawn-validation.gradle`, `gradle/version-sync.lock`, `gradle/disk-space-check.gradle`, `gradle/upgrade-test.gradle`, `gradle.properties`, `scripts/*_rules.json`, `prod-smoke-log-ignore.txt`, `upgrade-test-log-ignore.txt` | Validators, their rule files and settings. gradle.properties lists the validators that checkAll and validateAll run |
| 4 | A (D when deleted) | `scripts/upgrade_test/tests/**`, `scripts/upgrade_test/layers/**`, `scripts/scenario_check/tests/**`, `scripts/scenario_check/scenarios/**` | Tests and test content for the verification drivers. Deleting one weakens a gate |
| 5 | D | `scripts/upgrade_test/**`, `scripts/scenario_check/**` | Verification drivers (upgradeTest, scenarioCheck) |
| 6 | C | `build.gradle`, `**/build.gradle`, `settings.gradle`, `props/**`, `gradle/**`, `buildSrc/**`, `gradlew`, `gradlew.bat`, `.gitmodules` | Build, dependency ranges and supported versions |
| 7 | C | `**/*mixins*.json`, `**/pack.mcmeta`, `**/fabric.mod.json`, `**/META-INF/**` | Mixin configs, pack.mcmeta and loader metadata |
| 8 | C | `**/data/*/worldgen/**`, `**/data/*/dimension/**`, `**/data/*/dimension_type/**`, `**/data/*/structure/**`, `**/data/*/structures/**`, `**/data/*/tags/worldgen/**`, `**/data/*/neoforge/biome_modifier/**`, `**/data/*/forge/biome_modifier/**` | Worldgen, dimensions, structures and biome modifiers |
| 9 | C | `**/data/*/advancement/**`, `**/data/*/advancements/**`, `**/tags/block/needs_*`, `**/tags/blocks/needs_*`, `**/tags/block/incorrect_for_*`, `**/tags/blocks/incorrect_for_*` | Advancements and progression gating tags (tool tiers) |
| 10 | C | `**/java/**/registry/**`, `**/java/**/worldgen/**`, `**/java/**/config/**`, `**/java/com/chronodawn/data/**`, `**/java/**/core/portal/**`, `**/java/**/core/dimension/**`, `**/java/**/mixin/**` | Registries, worldgen, configs, persisted data, portals, dimensions and Mixins in Java |
| 11 | A | `**/assets/*/lang/*.json` | Lang files: A when every key of the base is kept, C when a key is removed or renamed or the file is deleted |
| 12 | A (D when deleted) | `**/src/test/**`, `**/src/gametest/**`, `common/gametest/**`, `**/java/**/gametest/**` | Unit tests and GameTests. Deleting one weakens a gate |
| 13 | B | `**/data/*/recipe/**`, `**/data/*/recipes/**`, `**/data/*/loot_table/**`, `**/data/*/loot_tables/**`, `**/data/*/loot_modifiers/**`, `**/data/*/tags/**` | Recipes, loot tables, loot modifiers and tags |
| 14 | B | `**/assets/**`, `assets/**`, `**/*.bbmodel` | Client assets and their sources: textures, models, sounds, blockstates |
| 15 | B | `**/*.java` | Other Java sources. The classifier cannot tell a comment-only change from a behavior change |
| 16 | A | `docs/**`, `**/*.md`, `LICENSE`, `.gitignore` | Docs, changelog and other Markdown |
| 17 | A | `scripts/**` | Utility scripts that no gate runs |
| - | D | `scripts/merge_tier/**` | Built into the classifier, so a pull request cannot lower its own tier by editing rules.json |
| - | C | anything else | Any path no rule names. When in doubt, use the higher tier |
<!-- merge-tier-rules:end -->

### Checking the declared tier in CI

The `Merge tier` workflow (`.github/workflows/merge-tier.yml`) runs on every
pull request, including edits to its body. It classifies the diff against the
merge base and fails when the body declares a tier lower than the diff's.
A higher declared tier passes. A body without a `Merge tier:` line passes with
a hint, because only tier A auto-merge needs the line. The job summary lists
every file with its tier and reason.

The same check requires an added or modified unit test, GameTest or scenario
when gameplay Java, assets, data, model sources or Mixin configuration change.
Deleted tests do not count. Translations and `package-info.java` are excluded.
Paths cannot establish whether a change actually affects behavior or whether a
test covers it. When an existing test covers the change or no behavior changes,
write `Test-Exemption: <reason>` in the PR body with at least 20 characters
explaining the coverage or why no new test is needed. The check cannot judge
the reason. The local Claude Code hook also accepts its existing commit trailer.

Each statement containing `not verified in game` or `実機未確認` must be on a
`Manual checks: <statement and reason>` line. The reason after removing the
statement must have at least 20 characters and explain why a scenario cannot
cover the check. Run the scenario where possible and attach its evidence as
required by the PR rules. These mechanical checks do not establish that a test
ran or that an exception is justified.

The workflow runs the classifier and `rules.json` from the base branch, not
from the pull request, so a pull request cannot lower its own tier by editing
them. The classifier also counts its own files as tier D regardless of the
rules.

## Verification per tier

| Gate | A | B | C |
|---|---|---|---|
| CI green (`validate`, `build`, `prod-smoke`, `gametest` on 1.21.1 and 26.3) | required | required | required |
| Independent verification (next section) | required | required | required |
| `checkAll` for the affected versions | not needed | when versions other than 1.21.1 and 26.3 are affected | required for all versions, as in the PR rule in `CLAUDE.md` |
| `upgradeTest` on 1.21.1 and 26.3, both loaders | not needed | not needed | required when the change touches registries, persisted data, worldgen, configs, portals, advancements, equipment or progression |
| `scenarioCheck` | not needed | when RCON commands and screenshots can show the behavior | when RCON commands and screenshots can show the behavior |
| Owner approval | not needed | required | required |

A diff that changes only paths of the docs rule above (`docs/**`, `**/*.md`,
`LICENSE`, `.gitignore`, marked `ci_docs_only` in `rules.json`) still runs
every CI job, but the jobs skip their steps and pass within seconds. This
holds for pull requests and for pushes to `main`. The first CI job,
`Detect docs-only changes`, decides this with
`scripts/merge_tier/docs_only.py` from the base branch, and its job summary
lists the files that made it run everything. A green CI on such a diff
therefore says nothing about the build, which is fine because nothing that
the build reads changed. Manual runs (`workflow_dispatch`) always run every
step.

`prodSmokeTest` and GameTest already run in CI for 1.21.1 and 26.3, so a
change in any tier that touches Mixin configs or `pack.mcmeta` also gets a
local `prodSmokeTest` for the other versions it affects.

Things a machine cannot judge stay with the owner: whether a boss is too
strong or too weak, whether a reward feels right, and whether a screenshot
looks good. These are tier B at the lowest, and the owner answers OK or NG
per scenario shot as before.

## Independent verification

The agent that wrote the change is biased toward believing it works. Before
the pull request is opened, a second reviewer tries to break the diff.

1. **Who.** A fresh subagent that has none of the author's conversation. It
   uses the same model as the main agent, so the independence comes from the
   clean context and from the evidence rule below, not from a different
   model. It gets only the diff, the task text and the tier of the change.
   When a Codex `auto-review` session cannot start such a subagent, the owner
   starts the review as an Ask chat from the Agent tab of the fuda task, and
   the agent saves the answer as `review.md`. The requirement stays
   "required": it is not waived for Codex.
2. **What it does.** It tries to break the change: edge cases, the other
   loader, the other Minecraft versions, existing worlds, and the bug classes
   in `bug_prevention.md` that match the touched files. It may run builds,
   tests, validators and scenarios.
3. **Evidence rule.** A finding counts only if the reviewer reproduced it by
   running something: a failing command, a log line, a test, a scenario shot.
   A suspicion without a reproduction is dropped, not forwarded.
4. **Author's response.** The author fixes each reproduced finding and
   re-runs the failing command. The author may not dismiss a reproduced
   finding without a reason written in the pull request.
5. **Record.** The pull request body gets a short "Verification" section with
   the tier, the commands the reviewer ran, and the reproduced findings with
   how each was resolved. "No reproduced findings" is a valid result when it
   lists what was tried.

The `independent-verification` skill (`.claude/skills/independent-verification/SKILL.md`)
is the procedure: how to collect the three inputs, the prompt for the
reviewer, and the form of the Verification section.

The reviewer runs locally before the pull request, not in CI. CI has no model
access and should not receive credentials for one. CI stays the deterministic
evidence, and the reviewer covers what CI cannot: reading intent and trying
cases nobody wrote a test for.

A Claude Code hook (`scripts/hooks/claude_pr_gate.py`, configured in
`.claude/settings.json`) enforces this locally. It denies `git push` and
`gh pr create` while the reviewer has not reviewed the current diff, while a
behavior change comes without a test or scenario, and while the pull request
body lacks the Verification section. The `pr-gate` skill
(`.claude/skills/pr-gate/SKILL.md`) lists the checks and the written
exemptions. The gate covers agents only, since it reads files that exist only
in the checkout that ran the review. The `Merge tier` CI check stays the
floor for every pull request.

## Auto-merge (tier A only)

An agent may merge a tier A pull request itself when all of these hold:

- CI is green on the pull request head.
- The independent verification section is in the body with no open finding.
- The classification is by the files in the diff, checked after the last push,
  and every file is tier A.
- The body states `Merge tier: A`, so the morning check can find it.

Everything else waits for the owner. When in doubt about the tier, use the
higher one.

### GitHub settings (checked 2026-11-23)

- `main` has the active ruleset `main required CI checks`. It requires the
  seven CI checks and `Merge tier` listed below and has an empty bypass list,
  so a pull request with a red or missing check cannot be merged, and a direct
  push to `main` is rejected unless its commits passed the checks. Merge
  through pull requests.
- The repository has auto-merge disabled. Merge commit, squash and rebase are
  all allowed, and merged branches are deleted.

### Setting up required checks (owner only)

Changing a ruleset needs admin rights on the repository, so only the owner
does it. The ruleset on `main` requires every CI job and the tier check. The
check names are the job names, so each matrix entry is its own check:

- `Validate resources and data`
- `Build and unit test (1.21.1)`, `Build and unit test (26.3)`
- `Production smoke test (1.21.1)`, `Production smoke test (26.3)`
- `GameTest (1.21.1)`, `GameTest (26.3)`
- `Merge tier`

In the web UI, open Settings, Rules, Rulesets, `main required CI checks`,
press "Add checks" under "Require status checks to pass", pick the check with
GitHub Actions as the source, and save. From the command line, look up the ruleset ID and
replace its rules with the full list:

```bash
gh api repos/{owner}/{repo}/rulesets --jq '.[] | "\(.id) \(.name)"'
gh api repos/{owner}/{repo}/rulesets/<id> --method PUT --input - <<'JSON'
{
  "rules": [
    {"type": "required_status_checks", "parameters": {
      "do_not_enforce_on_create": false,
      "strict_required_status_checks_policy": false,
      "required_status_checks": [
        {"context": "Validate resources and data", "integration_id": 15368},
        {"context": "Build and unit test (1.21.1)", "integration_id": 15368},
        {"context": "Build and unit test (26.3)", "integration_id": 15368},
        {"context": "Production smoke test (1.21.1)", "integration_id": 15368},
        {"context": "Production smoke test (26.3)", "integration_id": 15368},
        {"context": "GameTest (1.21.1)", "integration_id": 15368},
        {"context": "GameTest (26.3)", "integration_id": 15368},
        {"context": "Merge tier", "integration_id": 15368}
      ]}}
  ]
}
JSON
```

`integration_id` 15368 is GitHub Actions. Keep the bypass list empty. Agents
run `gh` with the owner's credentials, so a bypass for the admin role would
also let an agent merge past a red check. A pull request opened before the
`Merge tier` workflow existed has no `Merge tier` result. Edit its body or
push to it to run the check.

To allow agents to use GitHub auto-merge for tier A, also turn on "Allow
auto-merge" under Settings, General. Renaming a CI job renames its check, so
update the ruleset in the same change. Keep the jobs running for docs-only
diffs and skip their steps instead: a skipped matrix job reports one check
with the unexpanded `${{ matrix.mc }}` name, so the required checks would go
missing.

## After merging: morning check

Auto-merged pull requests are sampled once a day instead of before each merge.
Each morning the scheduled morning-check task (next section) reads the history
since the previous check. The mechanical half is one script:

```bash
python3 scripts/merge_tier/morning_check.py              # merged since yesterday
python3 scripts/merge_tier/morning_check.py --since 2026-11-20   # local midnight
```

It lists the merged pull requests whose body declares `Merge tier: A`,
fetches each final head (GitHub keeps it after the branch is deleted),
re-classifies the diff, and checks that the Verification section is not
empty. It exits 1 when one of them fails. It needs `gh` and network access,
so run it outside the sandbox. To look at one pull request by hand:

```bash
git log --first-parent --since="yesterday 00:00" --stat main
gh pr view <number> --json baseRefOid,headRefOid --jq '"\(.baseRefOid) \(.headRefOid)"'
git fetch origin "pull/<number>/head"
python3 scripts/merge_tier/classify.py --base <baseRefOid> --head <headRefOid>
```

Then skim each diff for what the file paths cannot show (see "Classifying a
diff by its files"). If one was misclassified or is wrong:

1. Revert it with a new pull request. Do not rewrite `main`.
2. Add the missed case to the tier lists above, or to `bug_prevention.md` when
   a check could have caught it.
3. Tighten the rule that let it through. Two misses in a month mean tier A is
   too wide, so stop auto-merging until the list is narrowed.

### Running the morning check on a schedule

A fuda schedule on the Chrono Dawn project creates the morning-check task
every morning, and the fuda agent auto start runs an agent on it. A scheduled
GitHub workflow cannot do the judgment half without model credentials in CI,
which this policy rules out. The owner sets the schedule up on the project's
Schedules page in fuda (schedules have no MCP tool or CLI command):

- Cron `0 7 * * *` (server local time), status Todo so that the auto start
  picks the task, and "skip if open" off. The agent cannot close its own
  task, so with the option on, a morning whose previous task the owner has
  not closed yet would create nothing and never run later. Off, an unclosed
  task stays visible as a check nobody has read, and step 3 below keeps the
  overlapping runs from reporting a pull request twice.
- The template body asks the agent to run this section and nothing else.

The agent on the morning-check task:

1. Runs `python3 scripts/merge_tier/morning_check.py --since <date>`, with the
   date of the previous morning-check task, or yesterday for the first one.
   The script reads the date as local midnight and sends GitHub the UTC
   offset, so nothing merged between two runs is skipped. The ranges of two
   runs overlap, so a pull request that already has a task is not reported
   again.
2. Skims the diff of every listed pull request, including the ones the script
   passed, for what the file paths cannot show.
3. Creates one fuda task per misclassified or wrong pull request, related to
   the morning-check task, with the pull request link, the declared and the
   computed tier, and what is wrong. The owner decides about the revert, so
   the agent changes no files, opens no pull request and reverts nothing.
4. Comments the script's report on the morning-check task and stops. fuda
   does not let an agent close its own task, so the owner finishes it.

The auto start runs the task only when the project is opted in and 07:00 is
inside its window. Otherwise the task waits in Todo for the owner to start it.
