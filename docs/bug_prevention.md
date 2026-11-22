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
| Gameplay logic, persistence, boss AI, portals | ~45 | ~150 | Unreleased | Not statically checkable. Unit tests, GameTest, scenarioCheck |
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
| Structure, NBT and placement | ~8 | (within worldgen) | 0.17.x | StructureTests GameTest guard. Not statically checked |
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
| Build every supported version in CI | Would catch every compile break in versions CI skips (~12 commits since March 2026), but it grows the build matrix from two jobs to fourteen, each setting up its own Minecraft through Loom. The CI matrix keeps 1.21.1 and 26.3 on purpose. `validateEraSymbols` covers the renames that recurred, and `checkAll` still builds everything before a release |
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
