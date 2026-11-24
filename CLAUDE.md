# Chrono Dawn Development Guidelines

Hand-maintained. Keep it short: it is loaded into every session. Put
task-specific procedures in `.claude/skills/` and link them from here.
Keep `AGENTS.md` consistent when changing shared project instructions.

---

## Documentation Maintenance

**CRITICAL**: When updating versions, dependencies, or content, documentation must be kept consistent across all files. The `doc-maintenance` skill lists the files and the procedure.

**Key Files**: `README.md`, `docs/player_guide.md`, `docs/developer_guide.md`, `docs/release_process.md` (versioning, changelog, release), `docs/curseforge_description.md`, `docs/modrinth_description.md`, `gradle.properties`, and the versioned Fabric, Forge and NeoForge metadata.

**Current version sources**:
- `gradle.properties` defines the supported versions, hotfix mappings, default target, and mod version.
- `props/<version>.properties` defines the Java, loader, and dependency versions for each target. Read these files instead of maintaining a duplicate table here.
- Minecraft 1.20.1 uses Java 17 and supports Fabric and Forge. 1.21.x uses Java 21 and supports Fabric and NeoForge. 26.x uses Java 25 and supports Fabric and NeoForge.
- Minecraft 1.21.3 is a hotfix runtime target that shares modules with 1.21.2 (no `common/1.21.3`, `fabric/1.21.3`, `neoforge/1.21.3`).

---

## License Compliance

**Project License**: LGPL-3.0. All contributions must comply. Details in the `license-compliance` skill.

- Compatible licenses: MIT, Apache 2.0, BSD. Incompatible: CC-BY-NC, proprietary
- Document all third-party licenses in `THIRD_PARTY_LICENSES.md`
- Dynamic linking (normal mod usage): any license OK. Static linking/modification: must be LGPL-3.0

---

## Technologies

Java 17 (1.20.1), Java 21 (1.21.x), Java 25 (26.x), Fabric Loader, Forge 47.x, NeoForge, Architectury, mcjunitlib. Read exact versions from `props/<version>.properties`.

## Project Structure

```
common/
  shared/, shared-1.21.1+/, shared-1.21.2+/, shared-1.21.5+/, gametest/
                      (shared sources/resources, NOT Gradle subprojects)
  <version>/          (version-specific common module)
fabric/    base/ (shared sources, NOT a subproject), <version>/ (subproject, every version)
forge/     base/, 1.20.1/
neoforge/  base/, <version>/ (1.21.1 and newer)
gradle/    wrapper/, shared/ (git submodule → minecraft-mod-gradle-scripts)
props/     version-specific properties
scripts/   project-specific utility scripts
```

The version list is `supported_mc_versions` in `gradle.properties`. List `common/`, `fabric/` and `neoforge/` for the directories that exist.

## Build Configuration

- **Build DSL**: Groovy DSL (not Kotlin DSL), for compatibility with Architectury Loom 1.17.491 (Gradle 9.5.1)
- **Mappings**: Mojang mappings (not Yarn). Code uses official Minecraft class names (e.g., `net.minecraft.core.Registry`)
- **Shadow Plugin**: com.gradleup.shadow 8.3.6 bundles the common module into platform-specific JARs
- **Shared Scripts**: `gradle/shared/` is a Git submodule ([minecraft-mod-gradle-scripts](https://github.com/ksoichiro/minecraft-mod-gradle-scripts)). Clone with `--recursive` or run `git submodule update --init`. A new git worktree starts with an empty `gradle/shared/`, so run `git submodule update --init gradle/shared` inside the worktree before any `./gradlew` command. After that, every Gradle task, including `gameTestAll`, runs isolated within the worktree.
- **Multi-version configuration**: supported versions and hotfix mappings are in `gradle.properties` (`supported_mc_versions`, `hotfix_mc_versions`)

## Multi-Version Support

Single codebase for every version in `supported_mc_versions` (plus the 1.21.3 hotfix). Era-specific API differences and the `compat/` abstraction layer are in the `multiversion-support` skill.

For 1.21.9+ `pack.mcmeta`, do not combine the resource-pack and data-pack versions into one `min_format`/`max_format` range. Minecraft validates the same mod metadata with different legacy cutoffs for client resources and server data. Follow the loader's bundled mod-pack metadata, preserve minor versions such as `[94, 1]`, and verify both resource reload and server data loading.

## Commands

Full list, flags and caveats are in the `verification-commands` skill. Read it before building, testing, validating or releasing.

- Build: `./gradlew build<version>` (`build1_21_1`, `build26_3`, ...), `./gradlew buildAll`. Default target is 26.3. Clean: `clean<version>`, `cleanAll`
- Run client: `./gradlew runClientFabric<version>`, `./gradlew runClientNeoForge<version>`
- Unit test: `./gradlew :common-<version>:test -Ptarget_mc_version=<version>`
- GameTest: `./gradlew :fabric:runGameTest -Ptarget_mc_version=<version>`, `:neoforge:runGameTestServer`, `gameTestAll`
- Production smoke: `./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=<version>` (and `:neoforge:`). Not for Forge 1.20.1
- Validation: `./gradlew validateAll` (also the pre-commit hook). Also `validateResources`, `validateData`, `validateTranslations`
- `./gradlew checkAll` is the full verification (clean, validate, buildAll with unit tests, gameTestAll)
- Redirect long Gradle output (`checkAll`) to `<checkout>/build/checkall.log` with the absolute checkout path, never to a fixed name under `$TMPDIR`
- Upgrade test and scenario check open a game window, run outside the sandbox, and are not part of `checkAll`
- Run at most 2 verifications at once (`checkAll`, `scenarioCheck`, `upgradeTest`). They fail early when less than `min_free_disk_gb` (20 GB) is free: free space instead of passing `-Pmin_free_disk_gb=0`

**Before a PR**:
- Classify the diff with `python3 scripts/merge_tier/classify.py`, run the `independent-verification` skill, and put its Verification section with a `Merge tier:` line in the PR body (`docs/merge_policy.md`). The `Merge tier` CI check fails when the declared tier is lower than the diff's
- Run `checkAll` for all versions. Limit it to the changed versions only when the impact is clearly confined to them, and say so in the PR body
- When a change needs an in-game check, do not wait for the owner. Add or extend a scenario (`scenario-check` skill, `docs/scenario_check.md`), run it on both 1.21.1 loaders (and both 26.3 loaders when 26.3 is affected), and attach `sheet.png` to the PR or task. List only what a scenario cannot cover as manual checks and continue
- A new bug class goes into `docs/bug_prevention.md` first. When a Minecraft rename breaks a version that CI does not build, add a rule to `scripts/era_symbol_rules.json` in the same change
- `validateVersionSync` fails when per-version copies recorded in `gradle/version-sync.lock` differ. Apply the change to every listed version. Run `./gradlew updateVersionSyncLock` only when a version must differ for an era-specific reason

**Key Strategy**: custom Gradle scripts + abstraction layer (`compat/` package) for API differences.

## Development Notes

- Use Mojang mapping names (e.g., `net.minecraft.world.level.Level`, not Yarn's `class_XXXX`)
- Build files use Groovy DSL. Assign properties with `=` (e.g., `maven { url = 'https://...' }`, `{ transitive = false }`). The `url 'https://...'` form is deprecated in Gradle 9 and removed in Gradle 10
- Common module code is bundled into the Fabric JAR using Shadow plugin
- GitHub Actions: pin every `uses:` to a full commit SHA with the version as a trailing comment, and pin `runs-on` to a fixed image (`ubuntu-24.04`). Update with pinact (command in `verification-commands`)
- CI never saves Minecraft JARs, mappings or world data as artifacts. Fabric and NeoForge run in separate Gradle invocations in CI (shared common module transform race, see `verification-commands`)
- Minecraft 1.20.1 encodes a uniform `IntProvider` inside worldgen feature configurations with a nested `value` object. Newer versions put `min_inclusive` and `max_inclusive` beside `type`, so keep a 1.20.1 resource override for configured features that use one.
- In pre-1.21.2 entity models, reset model-part poses at the start of `setupAnim` before applying additive rotations. The legacy `EntityModel` setup does not reset poses automatically, so `+=` animations otherwise accumulate across rendered frames.
- GameTest servers may omit custom dimensions. A dimension-dependent test that skips is not runtime verification: use a normal client or production server for the final cross-dimension check

## Support Priority

CurseForge data (2026-09-22): 1.21.1 files hold about 95% of cumulative downloads, NeoForge about 2:1 over Fabric, driven by a few modpacks. Details are in `.claude/tasks.local.md`.

- Treat 1.21.1 as the modpack/LTS regression target while keeping 26.3 as the default development target
- Verify pack-facing changes on both 1.21.1 loaders: NeoForge covers Tensura and MineColonies usage, Fabric covers Fantasy MC usage
- Prioritize optional compatibility, configuration, quest-author support, and safe upgrades over generic content growth when the work serves modpacks
- Do not drop or deprioritize 1.21.1 solely because newer Minecraft versions exist
- Re-derive priorities from current pack manifests and download data. This is not a permanent ranking

## Mixin Configuration

Fabric, Forge, and NeoForge use separate loader-specific Mixin configurations. Step-by-step guide: `mixin-guide` skill.

- **All loaders**: do not include a `refmap` property. Loom remaps Mixin targets directly in production JARs, and the legacy Mixin annotation processor is disabled
- **Common Gradle modules**: do not configure `loom.mixin.defaultRefmapName`
- **Common**: `chronodawn.mixins.json` is excluded from builds (reference only)
- When adding Mixins, update every affected loader-specific config
- After changing Mixin config, run `:fabric:prodSmokeTest` and `:neoforge:prodSmokeTest` for the version. `runGameTest`'s dev/mapped Loom environment can mask remapping or injection failures that only appear in a production server

## Workflow Guidelines

- **Research**: save findings for future work in an appropriate document under `docs/`, not only in the session. Track follow-up work in fuda with links to that document
- **Pre-commit verification**: before committing, decide whether the change is testable. If so, present the verification method (build, run, test command), the steps, and the expected result, and let the user decide whether to run it before committing. Example: run `./gradlew :fabric:build -Ptarget_mc_version=1.21.2`, confirm the build succeeds and JARs appear in `fabric/1.21.2/build/libs/`
