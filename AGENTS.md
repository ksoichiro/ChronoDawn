# AGENTS.md

This file gives AI coding agents the project-specific context needed to work on
Chrono Dawn. It is the single source for shared agent instructions: `CLAUDE.md`
imports it with `@AGENTS.md` and adds only Claude Code specifics. Change shared
rules here, not in `CLAUDE.md`. Keep it short, because Claude Code loads it in
every session.

## Project Overview

Chrono Dawn is a multi-loader Minecraft mod built with Architectury. It supports
Fabric and NeoForge, plus Forge on Minecraft 1.20.1, from one codebase. Most
gameplay logic lives in common modules and loader-specific code stays small.

- Language: Java 17 for Minecraft 1.20.1, Java 21 for 1.21.x, Java 25 for 26.x
- Build: Gradle Groovy DSL (Architectury Loom, Gradle wrapper), Shadow plugin
  bundles the common module into each loader JAR
- Mappings: Mojang mappings, not Yarn
- License: LGPL-3.0
- Supported versions, hotfix mappings and the default target: `gradle.properties`
  (`supported_mc_versions`, `hotfix_mc_versions`). The default development
  target is Minecraft 26.3
- Java, loader and dependency versions per target: `props/<version>.properties`.
  Read them instead of keeping a copy of the versions here
- Modpack/LTS target: Minecraft 1.21.1 on both Fabric and NeoForge

Reference docs:

- `README.md`: user-facing overview and build examples
- `docs/developer_guide.md`: architecture and development details
- `docs/release_process.md`: versioning, changelog, verification, and release
  procedure
- `CONTRIBUTING.md`: coding standards and contribution process

## Repository Layout

```text
common/
  shared/             Shared source included by versioned modules
  shared-1.21.1+/     Shared resources for 1.21.1+
  shared-1.21.2+/     Shared resources for 1.21.2+
  shared-1.21.5+/     Shared resources for 1.21.5+
  gametest/           Shared GameTest sources
  <version>/          Version-specific common module
fabric/
  base/               Shared Fabric code
  <version>/          Version-specific Fabric module
forge/
  base/               Shared Forge code
  1.20.1/             Forge 1.20.1 module
neoforge/
  base/               Shared NeoForge code
  <version>/          Version-specific NeoForge modules (1.21.1 and newer)
gradle/shared/        Git submodule with shared Gradle scripts
props/                Version-specific dependency properties
scripts/              Project utility scripts
docs/                 Player/developer/release documentation
```

`shared*`, `gametest` and every `base/` are source directories, not Gradle
subprojects. Minecraft 1.21.3 is a hotfix release that reuses the 1.21.2
modules; do not add separate `common/1.21.3`, `fabric/1.21.3`, or
`neoforge/1.21.3` modules unless the build model changes.

## Working Rules

- Preserve existing user changes. Check `git status --short` before editing and
  avoid reverting unrelated files.
- Prefer existing patterns in nearby version modules before introducing new
  abstractions.
- Do not use zsh's reserved `path` variable as a loop or script variable; it
  controls command lookup for the rest of that shell invocation.
- When applying a command to `rg` results in zsh, prefer NUL-delimited output
  with `rg -0` and `xargs -0`; command substitution can collapse multiple file
  names into one argument.
- Put common gameplay behavior in `common/` where possible. Use `fabric/`,
  `forge/`, or `neoforge/` only for loader APIs, entry points, events, or
  platform bridges.
- Use Mojang class names such as `Level`, `BlockPos`, and `Registry`; do not use
  Yarn names.
- Build files are Groovy DSL. Assign properties with `=`, as in `maven { url = 'https://...' }` and `{ transitive = false }`. Gradle 9 deprecates the `url 'https://...'` form, and Gradle 10 removes it.
- Keep documentation consistent when changing versions, dependencies, features,
  commands, metadata, or release behavior. Update related files such as
  `README.md`, `docs/player_guide.md`, `docs/developer_guide.md`,
  `docs/curseforge_description.md`, `docs/modrinth_description.md`,
  `gradle.properties`, and loader metadata as needed.
- Add player-visible fixes to `CHANGELOG.md` under `Unreleased` before reporting
  completion, even when the release version stays unchanged.
- Research that affects future implementation should be written to an
  appropriate file under `docs/`, and linked from follow-up tasks when
  applicable.
- When investigating seed-dependent portal inconsistencies, compare a fresh
  Minecraft process with consecutive worlds in one process. Check both new-data
  construction and saved-data loading, since static registries can survive an
  integrated-server shutdown. See `docs/portal-floating-investigation-1.21.1.md`,
  which also holds the rules for diagnostic Gradle init scripts and portal
  singleton state.
- GitHub Actions: pin every `uses:` to a full commit SHA with the version as a
  trailing comment, and pin `runs-on` to a fixed image (`ubuntu-24.04`), never
  `@vN` or `ubuntu-latest`.
- Never save Minecraft JARs, mappings or world data as CI artifacts.

## Support Priority

CurseForge data (2026-09-22): 1.21.1 files hold about 95% of cumulative
downloads and NeoForge about 2:1 over Fabric, driven by a few modpacks. Details
are in `.claude/tasks.local.md` when that local file is present.

- Treat 1.21.1 as the modpack/LTS regression target while keeping 26.3 as the
  default development target. Do not drop or deprioritize it because newer
  Minecraft versions exist.
- Verify pack-facing changes on both 1.21.1 loaders: NeoForge covers Tensura and
  MineColonies usage, Fabric covers Fantasy MC usage.
- Prioritize optional compatibility, configuration, quest-author support, and
  safe upgrades over generic content growth when the work serves modpacks.
- Re-derive priorities from current pack manifests and download data. This is
  not a permanent ranking.

## Coding Standards

- Java indentation: 4 spaces.
- JSON indentation: 2 spaces.
- Line length: 120 characters soft limit.
- Encoding: UTF-8.
- Line endings: LF.
- Comments should be in English and explain non-obvious reasons, not obvious
  mechanics.
- Registry IDs and resource names should follow existing lowercase naming
  conventions.

## Common Commands

Setup after clone, and inside every new git worktree before any `./gradlew`
command (a worktree starts with an empty `gradle/shared/`):

```bash
git submodule update --init
```

Patterns (`<version>` is `1.21.1` in `-Ptarget_mc_version`, `1_21_1` in task
names; the Claude Code skill `verification-commands` lists every option):

```bash
./gradlew build<version>                  # e.g. build26_3, build1_21_1, build1_20_1
./gradlew buildAll                        # release build of every version, excludes 1.21.3
./gradlew buildAll -Pbuildall_run_tests=true   # also runs unit tests of all versions
./gradlew :common-<version>:test -Ptarget_mc_version=<version>
./gradlew runClientFabric<version>        # also runClientNeoForge<version>, runClientForge1_20_1
./gradlew :fabric:runGameTest -Ptarget_mc_version=<version>
./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=<version>
./gradlew gameTestAll
./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=<version>   # and :neoforge:; not for Forge 1.20.1
./gradlew validateAll                     # file-only validations, also the pre-commit hook
./gradlew checkAll                        # full verification
./gradlew collectJars                     # then release
```

Each Gradle invocation selects one target version. Do not combine tasks from
different versioned projects in one invocation. Run a separate command with
the matching `-Ptarget_mc_version` for each version.
Do not run Gradle invocations for the same version concurrently: their resource
processing tasks share output directories and can interfere with each other.

`checkAll` is expensive. For small changes, run the narrowest build, test, or
validation command that covers the affected version and loader. Run it for all
versions before a PR unless the impact is clearly confined to specific
versions, and say so in the PR body. Redirect its output to
`<checkout>/build/checkall.log` with the absolute checkout path, not to a fixed
shared temp name. Run at most 2 verifications at once (`checkAll`,
`scenarioCheck`, `upgradeTest`). They fail early when less than
`min_free_disk_gb` (20 GB) is free: free the space instead of passing
`-Pmin_free_disk_gb=0`.

GameTest servers may omit custom dimensions even when the normal game loads
them. Do not count a dimension-dependent test that skips on a missing dimension
as runtime verification. Use a normal client or production server for the final
cross-dimension behavior check.

When changing a NeoForge version, run the affected project's `clean` task
before `prodSmokeTest`. The installed server cache is keyed by Minecraft
version and can otherwise reuse the previous NeoForge runtime.

Upgrade test (`upgradeTest`) and scenario check (`scenarioCheck`) open a game
window, run outside the sandbox, and are not part of `checkAll`.

Minecraft 26.x development clients can outlive the game main method because
launcher threads remain active. Shutdown workarounds that wait for the main
thread must capture it from a runtime client event, not a parallel mod-loading
event, and must be enabled only by the development run configuration.

## Multi-Version Notes

- 1.20.1 supports Fabric and Forge. NeoForge starts at 1.21.1.
- Shared sources/resources are included via source sets.
- When adding a feature, inspect the nearest existing implementation across
  versioned modules and copy only the version-specific differences that are
  actually required.
- For 1.21.9+ `pack.mcmeta`, do not combine the resource-pack and data-pack
  versions into one `min_format`/`max_format` range. Minecraft validates the
  same mod metadata with different legacy cutoffs for client resources and
  server data. Follow the loader's bundled mod-pack metadata, preserve minor
  versions such as `[94, 1]`, and verify both resource reload and server data
  loading.
- Minecraft 1.20.1 encodes a uniform `IntProvider` inside worldgen feature
  configurations as `{"type":"minecraft:uniform","value":{...}}`. Newer
  versions place `min_inclusive` and `max_inclusive` directly beside `type`, so
  keep a 1.20.1 resource override when a configured feature uses one.
- If an API differs between Minecraft versions, prefer the existing compatibility
  layer (`compat/` package) or platform helper pattern over scattering
  conditionals.
- In pre-1.21.2 entity models, reset model-part poses at the start of
  `setupAnim` before applying additive rotations. The legacy `EntityModel`
  setup does not reset poses automatically, so `+=` animations otherwise
  accumulate across rendered frames.
- `validateVersionSync` fails when per-version copies recorded in
  `gradle/version-sync.lock` differ. Apply the change to every listed version.
  Run `./gradlew updateVersionSyncLock` only when a version must differ for an
  era-specific reason.

When tuning biome coverage, adjust neighboring climate ranges as well as the
target biome in the shared dimension JSON. Climate intervals do not directly
measure world area or cap biome size. Verify the result in newly generated chunks
across multiple seeds.

## Mixin Configuration

Fabric, Forge, and NeoForge use separate loader-specific Mixin configs.

- Loader-specific Mixin configs must not include a `refmap` property. Loom
  remaps Mixin targets directly in the production JAR, and its legacy Mixin
  annotation processor is disabled.
- Do not add `loom.mixin.defaultRefmapName`. It creates a reference to a refmap
  that current Loom does not generate unless the legacy annotation processor is
  explicitly re-enabled.
- `chronodawn.mixins.json` is a common/reference config and is excluded from
  builds.
- When adding or changing Mixins, update every affected loader-specific config.
- After changing a Mixin or its config, run the affected loader's
  `prodSmokeTest`. A dev run can hide production remapping failures.

## License and Dependencies

- The project is LGPL-3.0. Contributions are expected to be compatible with that
  license.
- Before adding a dependency, verify license compatibility and update
  `THIRD_PARTY_LICENSES.md`.
- MIT, Apache-2.0, and BSD licenses are generally compatible. Avoid proprietary
  or non-commercial licenses such as CC-BY-NC.
- Dynamic linking (normal mod usage) accepts any license. Static linking or
  modification must be LGPL-3.0.

## Verification Reporting

When finishing a task, report:

- What changed.
- Which commands were run.
- Which commands were skipped and why.
- Any remaining risks or follow-up work.

For changes that are verifiable but not yet verified, provide the exact command
and expected success criteria.

Before a PR:

- A change to something visible or to a behavior (rendering, animation, item
  properties, movement, worldgen, game rules) needs a unit test or a scenario
  (`docs/scenario_check.md`) that runs it. A passing build or matching JSON
  does not show that it works.
- When you keep or copy a formula that mirrors vanilla, compare it with the
  vanilla equivalent of each affected Minecraft version first. Check signs,
  offsets, units and per-tick rates, not only that it compiles.
- Before writing "not verified in game" in a PR, check whether `scenarioCheck`
  can cover it. List as manual checks only what a scenario cannot cover, with
  the reason.

Classify the diff with `python3 scripts/merge_tier/classify.py` and
put a `Merge tier:` line in the PR body (`docs/merge_policy.md`). The `Merge tier`
CI check fails when the declared tier is lower than the diff's.
