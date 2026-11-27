# Bug Prevention Checks

This document records which classes of past bugs Chrono Dawn can catch with
deterministic checks, which checks exist for them, and which candidate checks
were rejected and why. Manual in-game verification is the slowest part of
development, so a bug class that a file check can catch should not depend on
someone remembering a note in a skill or in CLAUDE.md.

Read it before adding a new check, and update the tables when a new bug class
shows up or a check is added.

## Sources and method

The analysis (November 2026, up to 0.17.1 and Unreleased) used two inputs:

- All 151 bullets under `### Fixed` in `CHANGELOG.md`, from 0.2.0 to
  Unreleased.
- The 552 commits whose subject starts with `fix` (2025-10 to 2026-11-22).
  These include development-time bugs that never reached a release, such as
  build breaks in versions that were not compiled before the commit.

Each bug was classified by root cause. Counts are approximate, since a bug
with several causes is counted once under its main cause, and commit counts
come from subject grouping plus sampled diffs. Recent releases (0.13.0 and
later) weigh more when choosing what to automate.

## Bug classes

| Class | Changelog | Fix commits | Latest | Status |
|---|---|---|---|---|
| Gameplay logic, persistence, boss AI, portals | ~45 | ~150 | Unreleased | Not statically checkable. Unit tests, GameTest, scenarioCheck. Portal progression rules are model-checked by `verifyFormalModels` ([formal_verification.md](formal_verification.md)) |
| Worldgen and biome wiring (spawns, features, roots) | ~25 | ~72 | Unreleased | Partly covered by `validateData` and `validateEraTagParity`. Slope and foundation behavior needs scenarioCheck |
| Fix applied to some per-version copies only | ~15 | ~15 explicit ports, ~137 multi-version fixes | 2026-11 | **New: `validateVersionSync`** plus the Claude Code edit hint |
| Java era API split (renamed symbols, changed APIs) | ~18 | ~12 since March 2026 (plus ~50 from the 1.21.2 bring-up) | 0.17.1 | **New: `validateEraSymbols`** for renames. Other API changes need the per-version build |
| Loader-specific registration missing | ~9 | ~25 | 2026-10 | **New: `validateLoaderEntityRegistration`** for entities. Other registrations are not covered |
| Build and packaging metadata (pack.mcmeta, version ranges, refmap) | ~12 | ~10 | 0.17.0 | **New: `validateMixinConfigs`** for refmap and listed classes. pack.mcmeta is covered by prodSmokeTest only |
| Client items, models, textures, render layers | ~12 | ~25 | 0.13.0 | Covered by `validateResources`, `validateData` and `validateMobEffectIcons`. Render layers are not covered |
| Missing lang keys | ~9 | ~19 | 0.14.0 | Covered by `validateLangParity`, `validateRegistryLangCoverage`, `validateSoundSubtitles`, `validateConventionalTags` |
| Era JSON format (loot, recipe, worldgen) | ~6 | ~35 | 0.8.0 | Covered by `validateEraJsonFormat`. 26.3 keys that the game silently ignores are not covered |
| Missing tag membership | ~6 | ~24 | 0.12.0 | Covered by `validateBlockTagMembership` and `validateEraTagParity` for the cases that recurred |
| Missing block loot tables | 2 | ~5 | 0.12.0 | **New: `validateBlockLootTables`** |
| Structure, NBT and placement | ~8 | (within worldgen) | 0.17.x | StructureTests GameTest guard. `StructureTemplateTest` also fails when a template block that has no block entity carries `nbt`. A "DUMMY block entity" server log WARN near Forgotten Library was seen only intermittently and not reproduced, so its cause is unknown |
| Entity model and animation | ~6 | ~25 | 0.17.0 | Not covered, see rejected checks |
| Mixin targeting | (within metadata) | ~19 | 2026-11 | prodSmokeTest. `validateMixinConfigs` catches missing classes only |
| Config and upgrade compatibility | ~4 | few | 0.14.0 | upgradeTest |
| Thread safety and performance | ~7 | ~8 | 0.4.0-beta | Not covered, no recent cases |
| Changelog house style (em dash, semicolon) | 0 | 1 | 2026-09 | **New: pre-commit hook** |

## New checks

Each check below fails on a reproduction of the bug it targets and passes on
the current tree. The reproduction used for each is listed so it can be
repeated after changing the check.

### validateVersionSync

Many files have one copy per Minecraft version under `common/<v>`,
`fabric/<v>` and `neoforge/<v>`. The most common recent regression was a fix
that reached only some of those copies. Examples are the Clockstone ore count
that differed between versions, the 26.3 copies that missed a batch of fixes,
and the attack-pose reset that landed on 1.21.1 but not 1.20.1.

`gradle/version-sync.lock` records, for each path that exists in two or more
version modules, which versions hold byte-identical copies. The task fails
when that grouping changes:

- copies that were identical now differ,
- a new file exists in several versions but is not in the lock,
- a recorded file now exists in fewer than two versions.

If the difference is intended, for example an era-specific API change, run
`./gradlew updateVersionSyncLock` and commit the lock. The lock diff then
shows the split in review. When two branches both change the lock, resolve
the conflict by running `updateVersionSyncLock` again after the merge.

The check cannot see a fix that went to every copy in one group but missed a
group whose copies already differed. Those copies have to be compared by
hand, and the Claude Code hint below lists them at edit time only when they
were identical.

Reproduction: append a line to
`common/1.21.5/src/main/java/com/chronodawn/blocks/AncientTimeWoodButton.java`,
which is identical across 1.21.2 to 1.21.8.

### validateEraSymbols

CI compiles only 1.21.1 and 26.3, so a symbol that Minecraft renamed between
eras compiles there but breaks `build1_21_9` or `build1_21_11`. The rules live
in `scripts/era_symbol_rules.json`. Each one gives a regex and the version
range (`since`, `before`) where it must not appear in a version module's Java
sources. The seed rules cover `Identifier` and `ResourceLocation`,
`noCollision()` and `noCollission()`, and the `WaterAnimal` package move.

Add a rule whenever an era rename breaks a version that CI does not build. It
is cheaper than finding the break at the next `buildAll`.

Reproduction: import `net.minecraft.resources.Identifier` in
`common/1.21.5/.../TemporalPointedDripstoneBlock.java` (the bug fixed in
983f6ce12), or use `noCollission()` in a 1.21.9 module (36fe8b368).

### validateBlockLootTables

A block with no loot table drops nothing, and Minecraft logs nothing about it.
Every `ModBlockId` must resolve `blocks/<id>.json` in each version's loot
table directory (`loot_tables` on 1.20.1, `loot_table` on 1.21.1 and later),
from the version module or a `common/shared*` root that applies to it. Blocks
that drop nothing on purpose are listed in `blocksWithoutLootTable` in
`gradle/chronodawn-validation.gradle` with the reason. The task also fails
when an exempt block gains a loot table in every version, so the list does not
go stale.

The check found that the seven potted plants other than the potted Chrono
Bloom, Reversing Time Sandstone and Unstable Fungus have no loot table. These
are exempt and marked as pending review until someone decides whether they
are intended.

Reproduction: delete
`common/shared-1.21.1+/src/main/resources/data/chronodawn/loot_table/blocks/clockstone_ore.json`.

### validateLoaderEntityRegistration

Each loader module registers renderers, attributes and spawn placements in its
own entrypoints. A mob without a renderer crashes the client as soon as it
comes into view. For every Fabric, NeoForge and Forge module, falling back to
`<loader>/base` when the version module has no copy of the entrypoint:

- the client entrypoint must reference every `ModEntities` field of
  `common/<v>`,
- the main entrypoint must reference the same `ModEntities` fields as every
  other module.

The check found that NeoForge 1.21.4 registered no renderer or model layer for
Chrono Bovine and Temporal Caprid. This change fixes that.

Reproduction: restore the NeoForge 1.21.4 client entrypoint from before the
fix.

### validateMixinConfigs

For every Fabric, NeoForge and Forge mixin config (the common
`chronodawn.mixins.json` is reference only), the config must not contain
`refmap` (c233758bb), and every listed mixin must resolve to a Java source in
the module's compile roots. A mixin removed from the sources but still listed
otherwise fails only when the game starts.

Reproduction: add a `refmap` property, or list a class that does not exist,
in `fabric/1.21.1/src/main/resources/chronodawn-fabric.mixins.json`.

## Where the checks run

| Place | What runs | Notes |
|---|---|---|
| `checkAll` and `validateAll` | Every task in `checkall_extra_tasks`, including the five above | `validateAll` is the same list without the builds |
| CI (`.github/workflows/ci.yml`) | `validateAll` | Picks up new tasks from `checkall_extra_tasks` without a workflow change. Only checks that need no Minecraft runtime run there |
| Nightly (`.github/workflows/nightly.yml`) | `build` (compile and unit tests), `prod-smoke` and `gametest` of the 12 versions that CI skips (Fabric only on 1.20.1) | Runs at 03:00 JST only when code changed on `main` since the last successful scheduled run (docs-only diffs and no new commit skip, and the step summary says which). The morning check reads a failed run and files a task |
| Git pre-commit hook | `scripts/hooks/pre-commit` | Runs `validateAll` when the commit touches modules, build scripts or `scripts/`, and rejects em dashes and semicolons in added `CHANGELOG.md` lines |
| Claude Code PostToolUse hook | `scripts/hooks/claude_version_sync_hint.py` | After an edit to a version module file, lists the versions whose copy was identical and now differs |

### Installing the pre-commit hook

```bash
scripts/hooks/install.sh
git hook list pre-commit
```

The installer needs Git 2.54 or newer. It registers the script as a
config-based hook (`hook.chronodawn-pre-commit.*` in `.git/config`), so it
does not replace `core.hooksPath` or any other hook. The config is shared by
every worktree of the clone, and each worktree runs its own checkout's copy of
the script. A worktree on a branch without the script commits as before.
Remove it with `scripts/hooks/install.sh --uninstall`.

With an older Git, run `scripts/hooks/pre-commit` by hand before committing.

`validateAll` takes one to two seconds with a warm Gradle daemon. It reads the
working tree, not the index, so unstaged edits are checked too.

## Rejected and deferred checks

| Candidate | Reason |
|---|---|
| Build every supported version in every pull request | Would catch every compile break in versions CI skips (~12 commits since March 2026), but it grows the pull request matrix from two jobs to fourteen, each setting up its own Minecraft through Loom. The pull request matrix keeps 1.21.1 and 26.3 on purpose. The nightly workflow (`.github/workflows/nightly.yml`) builds, smoke tests and GameTests the other twelve once on a night when code changed on `main`, which avoids the per-pull-request cost. `validateEraSymbols` covers the renames that recurred, and `checkAll` still builds everything before a release |
| Reverse mixin check (every `@Mixin` class is listed) | Fabric-only mixins live in `common/<v>` and are correctly absent from NeoForge and Forge configs, so the check needs a per-loader allowlist of about 20 entries. Too much maintenance for a bug class seen once |
| Every block in a `mineable/*` tag | 56 of 194 blocks are plants, crops and markers that are correctly instant-break. The allowlist would be larger than the rule |
| `pack.mcmeta` format table per version | The values come from `props/<version>.properties`, and the wrong values were wrong in the table itself. prodSmokeTest boots each version and fails on the resulting log warnings |
| Lint for `setupAnim` `+=` without a pose reset (pre-1.21.2) | Real bug (0.17.0, 0.12.0), but telling a reset from a partial reset needs a Java parser. CLAUDE.md documents the rule. Revisit if it recurs |
| Lint for `SavedData` fields not written in `save()` | Several persistence bugs, but the save code is spread over helper methods and codecs, so a text check would give false results either way. GameTest restart coverage is the better fit |
| Semantic diff of worldgen JSON across versions | Overlaps with `validateVersionSync` for copies that started identical. For copies that differ by era, every normalizer is a new source of false results |
| Render layer registration per loader | Needs a parse of each loader's client registration. Deferred until the class recurs |
| Thread-safety lint | No case since 0.4.0-beta. `audit-performance-thread-safety` stays a review skill |
| Java linters (Checkstyle, Error Prone, SpotBugs) | None of the bug classes above is a style or nullness issue that these tools catch. They would add a build cost to every version without covering a past bug |
| Skill or CLAUDE.md notes | Not enforced. Used only for rules that no check can express |

## Weekly gardening

Most fixes are one-off. A few come back with the same root cause, and those
are worth turning into a check. A weekly scheduled task looks for them so the
tables above do not depend on someone remembering to update them. It follows
the same mechanism as the morning check in
[merge_policy.md](merge_policy.md#running-the-morning-check-on-a-schedule).

### Schedule setup (owner)

A fuda schedule on the Chrono Dawn project creates the gardening task, and the
fuda agent auto start runs an agent on it. Schedules have no MCP tool or CLI
command, so the owner creates it on the project's Schedules page:

- Cron `0 8 * * 1` (Monday 08:00, server local time). It does not overlap the
  morning check at `0 7 * * *`.
- Status Todo, so that the auto start picks the task.
- "Skip if open" off. The agent cannot close its own task, so with the option
  on, a week whose previous task the owner has not closed yet would create
  nothing, and the routine would never run again.
- Title: `Weekly gardening`. The agent finds the previous run by this title.
- Template body: `Run the "Weekly gardening" section of docs/bug_prevention.md
  and nothing else.`

The auto start runs the task only when the project is opted in and the time is
inside its window. Otherwise the task waits in Todo for the owner to start it.

### Agent procedure

The agent on the gardening task:

1. Finds the date of the previous task titled `Weekly gardening`, or uses the
   last seven days for the first run. Reads the commits on `main` since then
   whose subject starts with `fix`, and the lines added under `### Fixed` in
   `CHANGELOG.md` (`git log --no-merges --since=<date> main`). Do not use
   `--first-parent`: `main` takes pull requests as merge commits, so it would
   list only their subjects.
2. Groups them by root cause, not by symptom. Sets each group against the
   "Bug classes" table: a recurrence of an existing class, or a new class.
3. Leaves alone every cause that appears once. Acts only on a cause that
   shows up in several fixes, or where several agents or sessions reached for
   the same shortcut or workaround.
4. For each remaining group, creates one fuda task, related to the gardening
   task. It proposes either a row for the "Bug classes" table or a new check.
   It does not propose a check that is already in "Rejected and deferred
   checks" or in "Memory conversion candidates" below, unless the new fixes
   are evidence for reconsidering it. Then it says so in the task.
5. Comments a short report on the gardening task and stops: the range read,
   the groups found, the tasks created, and the causes left alone. fuda does
   not let an agent close its own task, so the owner finishes it.

The agent changes no files and opens no pull request in the gardening task
itself. Each proposal is implemented in its own task.

### Turning a cause into a check

When an agent implements one of these proposals, including a fix it made
itself, it follows the same rules as any other change:

- Classify the diff with `python3 scripts/merge_tier/classify.py` and follow
  [merge_policy.md](merge_policy.md). Adding a validator, a rule file, a
  `scripts/hooks/**` script, a skill, or an entry in `checkall_extra_tasks` is
  tier D, so the owner merges it. A script under `scripts/**` that no gate
  runs is tier A.
- Run the independent verification (`independent-verification` skill) on the
  diff before the pull request. A check written by the author of the fix is
  the case where a fresh reviewer matters most.
- Show that the check fails on a reproduction of the bug and passes on the
  current tree, and write the reproduction in "New checks" like the entries
  there.
- Add the new class or check to the tables in this document in the same
  change.

## Memory conversion candidates

Claude's feedback memory held about 210 entries when this list was made
(2026-11-27). Entries are local to the developer's machine, so the list names
the topic and not the file. Entries were left out of the list when:

- A check already covers them (the validators in `checkall_extra_tasks`,
  `StructureTemplateTest`, the Merge tier gate, or the pre-commit hook). This
  includes loot table, recipe and advancement era formats, `table_bonus`
  `chances`, recipe unlock advancements, loader entity registration, mixin
  refmaps, `noCollission`, `Identifier`, `WaterAnimal`, the lang and subtitle
  keys, block loot tables and the NBT block-entity rule.
- They are in "Rejected and deferred checks" (pose reset, `SavedData`
  `save()`, render layer registration).
- They are workflow, sandbox, Gradle, subagent or machine notes. Those are
  not file properties. They stay as memory, `CLAUDE.md` or a skill.
- They are one-off facts, such as a library version pin or a vanilla quirk
  that needs a reviewer to know it. Examples are the Architectury minimum for
  26.3 and the `SeagrassBlock` fluid state.

What remains, ordered by how often the class bit, then by how cheap the check
is. "Form" is the cheapest place to enforce it. Each row becomes its own task
when someone picks it up, following the rules above.

| Candidate | Form | Why it is mechanical |
|---|---|---|
| Era-specific Java symbol rules: `setId()` before 1.21.2, `saturationMod` vs `saturationModifier`, `net.minecraft.Util` move in 1.21.11, `MapCodec` on 1.20.1 | New entries in `scripts/era_symbol_rules.json` | Same mechanism as `validateEraSymbols`. Each is a regex with a version range. CI builds only 1.21.1 and 26.3, so these break at the next `buildAll` otherwise. Tier D |
| Era JSON schema rules: `IntProvider` uniform nesting on 1.20.1, biome music schema, `dimension_type` `timelines` on 1.21.11, `pack.mcmeta` `min_format` on 26.x, explicit `type` in disk `state_provider` on 26.1.2, `matching_fluids` on 1.20.1, 26.2-only advancement predicates | A rule file read by `validateEraJsonFormat`, one entry per JSON path and version range | These are silent or world-creation crashes that only a per-version run shows. The paths and keys are fixed per era |
| Feature order cycle across biomes | Validator | Minecraft sorts every biome's `features` array into one topological order and crashes on entering the dimension if two biomes disagree. The check builds the same graph from the biome JSON and reports a cycle |
| Every dimension biome lists the ore features, and each loader has its biome modifier | Validator | Three biomes once missed two ores with no error. Forge 1.20.1 needs its own `forge/biome_modifier` and NeoForge needs `data/<ns>/neoforge/biome_modifier/`. The check compares the ore placed features against each biome and each loader's modifier files |
| Biome music and ambient sound events exist in `ModSounds` | Validator | A biome JSON that names an unregistered sound event crashes worldgen. `sounds.json` alone does not register it |
| Ore `configured_feature` targets also the deepslate variant, and solid blocks are in the 26.3 heightmap tag | Validator (JSON) | The deepslate surface rule replaces the base stone below Y 0, so an ore that targets only the base stone never generates there. On 26.3 an untagged solid block reads as air in the heightmaps |
| Feature configs that take `Holder<PlacedFeature>` | Validator (JSON) | A bare configured feature where a placed feature is required crashes world creation. The accepted shapes are a string id or an inline object |
| Structure generation code uses the dimension's custom blocks | Validator (Java text) with an allowlist | `Blocks.DIRT` hardcoded in a structure placer leaves vanilla blocks in the dimension. A grep over the placer classes finds them |
| Boss `Monster` subclasses override both `isPersistenceRequired()` and `removeWhenFarAway()` | Validator (Java text) or GameTest | A boss that overrides only one still despawns. The boss list comes from the entity registry |
| Blocks that need the right tool are in `needs_stone_tool` or `needs_iron_tool`, and custom dirt-type blocks are in `minecraft:dirt` | Validator (tag membership) | Same shape as `validateBlockTagMembership`. The rule needs the Java factory that sets `requiresCorrectToolForDrops()` |
| Block model lint: inline `elements` have a `block/block` parent, a model that uses `tintindex` descends from `tinted_cross`, no `template_kelp` or `template_seagrass` parents, double slabs with distinct sides have their own model | Validator (JSON) | Each is a parent or key rule on model files. The failures are a flat dark icon, a tint that never applies, or a missing parent |
| `ComposterBlock.COMPOSTABLES` covers every custom subclass of a vanilla plant block | GameTest | The registration is code only, so it cannot be read from data. JUnit tests avoid the registry, so this needs a GameTest |
| `@ExpectPlatform` implementations exist at `<package>.<loader>.<Class>Impl` | Validator (Java text) | A wrong package compiles and fails when the method is first called |
| A change to `props/*.properties` that raises a minimum version carries a `CHANGELOG.md` Unreleased line | `scripts/hooks/pre-commit` | The bump is user-facing because an old pin crashes at startup. The hook already inspects `CHANGELOG.md` |
