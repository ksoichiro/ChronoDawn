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
| D: gate files | `CLAUDE.md`, `.claude/**`, skills, hooks, `.github/**`, `scripts/hooks/**`, validators and this document | Owner merges. The fuda agent guard already stops agents from merging these |

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

## Verification per tier

| Gate | A | B | C |
|---|---|---|---|
| CI green (`validate`, `build`, `prod-smoke`, `gametest` on 1.21.1 and 26.3) | required | required | required |
| Independent verification (next section) | required | required | required |
| `checkAll` for the affected versions | not needed | when versions other than 1.21.1 and 26.3 are affected | required for all versions, as in the PR rule in `CLAUDE.md` |
| `upgradeTest` on 1.21.1 and 26.3, both loaders | not needed | not needed | required when the change touches registries, persisted data, worldgen, configs, portals, advancements, equipment or progression |
| `scenarioCheck` | not needed | when RCON commands and screenshots can show the behavior | when RCON commands and screenshots can show the behavior |
| Owner approval | not needed | required | required |

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

The reviewer runs locally before the pull request, not in CI. CI has no model
access and should not receive credentials for one. CI stays the deterministic
evidence, and the reviewer covers what CI cannot: reading intent and trying
cases nobody wrote a test for.

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

- `main` has no branch protection and there are no rulesets. Anyone with write
  access can push to it or merge without a passing check.
- The repository has auto-merge disabled. Merge commit, squash and rebase are
  all allowed, and merged branches are deleted.

So the policy above is a convention and nothing on GitHub enforces it yet.
Making `validate`, `build`, `prod-smoke` and `gametest` required checks on
`main` is an owner-only change in the repository settings. Until then an
agent must wait for green CI itself before it merges.

## After merging: morning check

Auto-merged pull requests are sampled once a day instead of before each merge.
Each morning the owner or the first agent of the day reads the history since
the previous check:

```bash
git log --first-parent --since="yesterday 00:00" --stat main
gh pr list --state merged --search "merged:>=$(date -v-1d +%F) Merge tier: A in:body"
```

For each tier A pull request, skim the diff and confirm it really contains
only tier A files and that the Verification section is not empty. If one was
misclassified or is wrong:

1. Revert it with a new pull request. Do not rewrite `main`.
2. Add the missed case to the tier lists above, or to `bug_prevention.md` when
   a check could have caught it.
3. Tighten the rule that let it through. Two misses in a month mean tier A is
   too wide, so stop auto-merging until the list is narrowed.
