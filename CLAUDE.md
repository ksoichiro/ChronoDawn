# Chrono Dawn Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-09-22

---

## Documentation Maintenance

**CRITICAL**: When updating versions, dependencies, or content, documentation must be kept consistent across all files.

**Key Files**:
- `README.md` - Project overview and installation
- `docs/player_guide.md` - Player guide
- `docs/developer_guide.md` - Developer guide
- `docs/release_process.md` - Versioning, changelog, verification, and release procedure
- `docs/curseforge_description.md` - CurseForge page
- `docs/modrinth_description.md` - Modrinth page
- `gradle.properties` - Version definitions
- Versioned Fabric, Forge, and NeoForge metadata - Mod and dependency metadata

**Current version sources**:
- `gradle.properties` defines the supported versions, hotfix mappings, default
  target, and mod version.
- `props/<version>.properties` defines the Java, loader, and dependency versions
  for each target. Read these files instead of maintaining a duplicate table
  here.
- Minecraft 1.20.1 uses Java 17 and supports Fabric and Forge. Minecraft 1.21.x
  uses Java 21 and supports Fabric and NeoForge. Minecraft 26.x uses Java 25 and
  supports Fabric and NeoForge.
- Minecraft 1.21.3 is a hotfix runtime target that shares modules with 1.21.2.

---

## License Compliance

**Project License**: LGPL-3.0 (GNU Lesser General Public License v3.0)

**CRITICAL**: All contributions and modifications must comply with LGPL-3.0 terms.

**Quick Reference**:
- Compatible licenses: MIT, Apache 2.0, BSD
- Incompatible licenses: CC-BY-NC, proprietary
- Document all third-party licenses in `THIRD_PARTY_LICENSES.md`
- Dynamic linking (normal mod usage): Any license OK
- Static linking/modification: Must be LGPL-3.0

---

## Active Technologies
- Java 17 (Minecraft 1.20.1), Java 21 (Minecraft 1.21.x), Java 25
  (Minecraft 26.x), Fabric Loader, Forge 47.x, NeoForge, Architectury, and
  mcjunitlib. Read exact versions from `props/<version>.properties`.

## Project Structure
```
common/
  shared/             (shared version-agnostic sources, NOT a Gradle subproject)
  shared-1.21.1+/     (shared resources for 1.21.1~26.2, NOT a Gradle subproject)
  shared-1.21.2+/     (shared resources for 1.21.2~26.2, NOT a Gradle subproject)
  shared-1.21.5+/     (shared resources for 1.21.5~26.2, NOT a Gradle subproject)
  gametest/           (shared gametest sources, NOT a Gradle subproject)
  1.20.1/             (version-specific common module)
  1.21.1/             (version-specific common module)
  1.21.2/             (version-specific common module)
  1.21.4/             (version-specific common module)
  1.21.5/             (version-specific common module)
  1.21.6/             (version-specific common module)
  1.21.7/             (version-specific common module)
  1.21.8/             (version-specific common module)
  1.21.9/             (version-specific common module)
  1.21.10/            (version-specific common module)
  1.21.11/            (version-specific common module)
  26.1.2/             (version-specific common module)
  26.2/               (version-specific common module)
fabric/
  base/               (shared Fabric sources, NOT a Gradle subproject)
  1.20.1/             (version-specific Fabric subproject)
  1.21.1/             (version-specific Fabric subproject)
  1.21.2/             (version-specific Fabric subproject)
  1.21.4/             (version-specific Fabric subproject)
  1.21.5/             (version-specific Fabric subproject)
  1.21.6/             (version-specific Fabric subproject)
  1.21.7/             (version-specific Fabric subproject)
  1.21.8/             (version-specific Fabric subproject)
  1.21.9/             (version-specific Fabric subproject)
  1.21.10/            (version-specific Fabric subproject)
  1.21.11/            (version-specific Fabric subproject)
  26.1.2/             (version-specific Fabric subproject)
  26.2/               (version-specific Fabric subproject)
forge/
  base/               (shared Forge sources, NOT a Gradle subproject)
  1.20.1/             (version-specific Forge subproject)
neoforge/
  base/               (shared NeoForge sources, NOT a Gradle subproject)
  1.21.1/             (version-specific NeoForge subproject)
  1.21.2/             (version-specific NeoForge subproject)
  1.21.4/             (version-specific NeoForge subproject)
  1.21.5/             (version-specific NeoForge subproject)
  1.21.6/             (version-specific NeoForge subproject)
  1.21.7/             (version-specific NeoForge subproject)
  1.21.8/             (version-specific NeoForge subproject)
  1.21.9/             (version-specific NeoForge subproject)
  1.21.10/            (version-specific NeoForge subproject)
  1.21.11/            (version-specific NeoForge subproject)
  26.1.2/             (version-specific NeoForge subproject)
  26.2/               (version-specific NeoForge subproject)
gradle/
  wrapper/            (Gradle Wrapper)
  shared/             (git submodule → minecraft-mod-gradle-scripts)
props/                (version-specific properties)
scripts/              (project-specific utility scripts)
```

## Code Style
Java 21 (Minecraft Java Edition 1.21.1 / 1.21.2 / 1.21.3 / 1.21.4 / 1.21.5 / 1.21.6 / 1.21.7 / 1.21.8 / 1.21.9 / 1.21.10 / 1.21.11): Follow standard conventions

## Build Configuration
- **Build DSL**: Groovy DSL (not Kotlin DSL) - for compatibility with Architectury Loom 1.17.491 (Gradle 9.5.1; bumped from Loom 1.13-SNAPSHOT/Gradle 8.14 to support Minecraft 26.x)
- **Mappings**: Mojang mappings (not Yarn) - code uses official Minecraft class names (e.g., `net.minecraft.core.Registry`)
- **Shadow Plugin**: com.gradleup.shadow 8.3.6 - for bundling common module into platform-specific JARs
- **Shared Scripts**: `gradle/shared/` is a Git submodule ([minecraft-mod-gradle-scripts](https://github.com/ksoichiro/minecraft-mod-gradle-scripts)). Clone with `git clone --recursive` or run `git submodule update --init` after clone.
- **Multi-version configuration**: Supported versions and hotfix mappings are defined in `gradle.properties` (`supported_mc_versions`, `hotfix_mc_versions`)

## Multi-Version Support

**Supported Versions**: Minecraft 1.20.1 + 1.21.1 + 1.21.2 + 1.21.3 + 1.21.4 + 1.21.5 + 1.21.6 + 1.21.7 + 1.21.8 + 1.21.9 + 1.21.10 + 1.21.11 + 26.1.2 + 26.2 (single codebase)

**Note**: 1.21.3 is a hotfix release that reuses 1.21.2 modules (no separate common/1.21.3, fabric/1.21.3, neoforge/1.21.3 directories needed).

**Clean Commands**:
- `./gradlew clean1_20_1` - Clean for 1.20.1
- `./gradlew clean1_21_1` - Clean for 1.21.1
- `./gradlew clean1_21_2` - Clean for 1.21.2
- `./gradlew clean1_21_3` - Clean for 1.21.3 (uses 1.21.2 modules)
- `./gradlew clean1_21_4` - Clean for 1.21.4
- `./gradlew clean1_21_6` - Clean for 1.21.6
- `./gradlew clean1_21_7` - Clean for 1.21.7
- `./gradlew clean1_21_8` - Clean for 1.21.8
- `./gradlew clean1_21_9` - Clean for 1.21.9
- `./gradlew clean1_21_10` - Clean for 1.21.10
- `./gradlew clean1_21_11` - Clean for 1.21.11
- `./gradlew clean26_1_2` - Clean for 26.1.2
- `./gradlew clean26_2` - Clean for 26.2
- `./gradlew cleanAll` - Clean all versions (excludes 1.21.3 - shares 1.21.2 modules)

**Build Commands**:
- `./gradlew build1_20_1` - Build for 1.20.1
- `./gradlew build1_21_1` - Build for 1.21.1
- `./gradlew build1_21_2` - Build for 1.21.2
- `./gradlew build1_21_3` - Build for 1.21.3 (uses 1.21.2 modules, for testing only)
- `./gradlew build1_21_4` - Build for 1.21.4
- `./gradlew build1_21_5` - Build for 1.21.5
- `./gradlew build1_21_6` - Build for 1.21.6
- `./gradlew build1_21_7` - Build for 1.21.7
- `./gradlew build1_21_8` - Build for 1.21.8
- `./gradlew build1_21_9` - Build for 1.21.9
- `./gradlew build1_21_10` - Build for 1.21.10
- `./gradlew build1_21_11` - Build for 1.21.11
- `./gradlew build26_1_2` - Build for 26.1.2
- `./gradlew build26_2` - Build for 26.2 (default)
- `./gradlew buildAll` - Build for release (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2 - excludes 1.21.3)

**Run Client**:
- Fabric: `./gradlew runClientFabric1_20_1`, `./gradlew runClientFabric1_21_1`, `./gradlew runClientFabric1_21_2`, `./gradlew runClientFabric1_21_3`, `./gradlew runClientFabric1_21_4`, `./gradlew runClientFabric1_21_5`, `./gradlew runClientFabric1_21_6`, `./gradlew runClientFabric1_21_7`, `./gradlew runClientFabric1_21_8`, `./gradlew runClientFabric1_21_9`, `./gradlew runClientFabric1_21_10`, `./gradlew runClientFabric1_21_11`, `./gradlew runClientFabric26_1_2`, `./gradlew runClientFabric26_2`
- NeoForge: `./gradlew runClientNeoForge1_21_1`, `./gradlew runClientNeoForge1_21_2`, `./gradlew runClientNeoForge1_21_3`, `./gradlew runClientNeoForge1_21_4`, `./gradlew runClientNeoForge1_21_5`, `./gradlew runClientNeoForge1_21_6`, `./gradlew runClientNeoForge1_21_7`, `./gradlew runClientNeoForge1_21_8`, `./gradlew runClientNeoForge1_21_9`, `./gradlew runClientNeoForge1_21_10`, `./gradlew runClientNeoForge1_21_11`, `./gradlew runClientNeoForge26_1_2`, `./gradlew runClientNeoForge26_2`

**Unit Test** (JUnit only, not GameTest):
- `./gradlew :common-1.21.2:test -Ptarget_mc_version=1.21.2` - Run unit tests for specific version
- `./gradlew testAll` - Run unit tests for all versions (excludes 1.21.3 - shares 1.21.2 modules)
- `./gradlew :common-1.21.6:test -Ptarget_mc_version=1.21.6` - Run unit tests for 1.21.6
- `./gradlew :common-1.21.7:test -Ptarget_mc_version=1.21.7` - Run unit tests for 1.21.7
- `./gradlew :common-1.21.8:test -Ptarget_mc_version=1.21.8` - Run unit tests for 1.21.8
- `./gradlew :common-1.21.9:test -Ptarget_mc_version=1.21.9` - Run unit tests for 1.21.9
- `./gradlew :common-1.21.10:test -Ptarget_mc_version=1.21.10` - Run unit tests for 1.21.10
- `./gradlew :common-1.21.11:test -Ptarget_mc_version=1.21.11` - Run unit tests for 1.21.11
- `./gradlew :common-26.1.2:test -Ptarget_mc_version=26.1.2` - Run unit tests for 26.1.2
- `./gradlew :common-26.2:test -Ptarget_mc_version=26.2` - Run unit tests for 26.2

**GameTest**:
- `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.2` - Run GameTests for specific version
- `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.2` - Run NeoForge GameTests
- `./gradlew gameTestAll` - Run GameTests including 1.21.3 runtime verification (1.21.2+1.21.3 sequential)

**Production Smoke Test** (headless, no display required; recommended after Mixin config changes or when adding a new Minecraft version):
- `./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=1.21.2` - Boot a real Fabric dedicated server with the built mod and confirm a fresh world is created without crashing
- `./gradlew :neoforge:prodSmokeTest -Ptarget_mc_version=1.21.2` - Same for NeoForge
- Not supported for Forge (1.20.1) - `runProd`'s underlying installer path is unsupported for Forge < 50, see `gradle/shared/prod-run.gradle`
- Unlike `runGameTest`/`runGameTestServer` (dev/mapped Loom environment), this launches an installer-provisioned production server, so it also catches Mixin refmap issues that only surface outside the dev environment
- Uses `build/prod-smoke/<loader>-<version>/`, separate from the manually-verified `run-prod/` used by `runProd` - cleaned up by `clean<version>`/`cleanAll`

**Resource Validation**:
- `./gradlew validateResources` - Check JSON syntax and cross-references (blockstate→model, model→texture)
- `./gradlew validateData` - Check data-pack cross-references (tag entries → registered IDs, recipe references, 1.21.4+ client items coverage)
- `./gradlew validateTranslations` - Cross-version translation key validation (entities, spawn eggs)

**Release**:
- `./gradlew collectJars` - Collect release JARs from all versions into `build/release/`
- `./gradlew release` - Full release pipeline: cleanAll → buildAll → collectJars
- `./gradlew releaseModrinth` - Release all JARs in `build/release/` to Modrinth (requires `MODRINTH_TOKEN`)
- `./gradlew releaseCurseForge` - Release all JARs in `build/release/` to CurseForge (requires `CURSEFORGE_TOKEN`)
- `./gradlew releaseAll` - Release to both Modrinth and CurseForge

**Full Verification** (recommended before commits/PRs):
- `./gradlew checkAll` - Run all verification tasks in sequence:
  1. cleanAll - Clean all build outputs and IDE directories
  2. validateResources - JSON syntax and cross-reference checks
  3. validateTranslations - Cross-version translation key validation
  4. buildAll - Build for release (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2)
  5. testAll - Run unit tests (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, 26.2)
  6. gameTestAll - Run GameTests including 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11, 26.1.2, and 26.2 runtime verification

**Key Strategy**: Custom Gradle scripts + abstraction layer (`compat/` package) for API differences

## Development Notes
- When writing code, use Mojang mapping names (e.g., `net.minecraft.world.level.Level`, not Yarn's `class_XXXX`)
- Build files use Groovy syntax (e.g., `maven { url 'https://...' }`, not `maven { url = "https://..." }`)
- Common module code is bundled into Fabric JAR using Shadow plugin
- **Parallel GameTest**: `gameTestAll` groups configurations by Minecraft version. 1.20.1 and 1.21.1 run in parallel, while 1.21.2 and 1.21.3 run sequentially in the same thread (they share modules). Within each version, fabric and neoforge run in a single Gradle process to avoid common module build conflicts. Each version uses a separate directory (fabric/1.20.1, fabric/1.21.1, etc.) with its own `.gradle/architectury/` path.

## Support Priority

**CurseForge download data supplied on 2026-09-22** shows that 1.21.1 files account for about 185.6K of 195.3K cumulative downloads, roughly 95%. Across all listed 1.21.1 files, NeoForge has about 124.5K downloads and Fabric about 61.1K, roughly 2:1. The approximately 5.7:1 ratio applies only to the 0.8.0 NeoForge and Fabric files, not to 1.21.1 as a whole. Matching pack manifests and release timing indicate that a few modpacks drive the largest spikes, so these figures should not be read as standalone loader demand.

**Implications for development priority**:
- Treat 1.21.1 as the modpack/LTS regression target while keeping 26.2 as the default development target
- Verify pack-facing changes on both 1.21.1 loaders: NeoForge covers Tensura and MineColonies usage, while Fabric covers Fantasy MC usage
- Prioritize optional compatibility, configuration, quest-author support, and safe upgrades over generic content growth when the work serves modpacks
- Do not drop or deprioritize 1.21.1 solely because newer Minecraft versions exist
- Re-derive priorities from current pack manifests and download data; the detailed 2026-09-22 analysis is in `.claude/tasks.local.md` and is not a permanent ranking

## Mixin Configuration

**CRITICAL**: Fabric and the Mojang-mapped loaders require **different** Mixin configurations due to mapping differences.

**Key Points**:
- **Fabric**: Must include `"refmap": "common-common-refmap.json"` in `chronodawn-fabric.mixins.json`
- **Forge / NeoForge**: Must NOT include a refMap property in their loader-specific mixin configs
- **Common**: `chronodawn.mixins.json` excluded from builds (reference only)
- When adding Mixins: Update every affected loader-specific config
- After changing Mixin config, run `./gradlew :fabric:prodSmokeTest -Ptarget_mc_version=<v>` and `:neoforge:prodSmokeTest` - `runGameTest`'s dev/mapped Loom environment can mask refmap injection failures that only appear in a production server (see "Production Smoke Test" above)

<!-- MANUAL ADDITIONS START -->

## Workflow Guidelines

### Research and Investigation
- **Document Research Results**: When conducting research or investigation for future tasks, always save findings to appropriate files (e.g., `specs/chrono-dawn-mod/research.md`) rather than keeping them only in session conversation
- **Add Related Tasks**: After completing research, add corresponding tasks to `tasks.md` with clear implementation steps
- **Cross-Reference**: Link research decisions to related task IDs for traceability

### Pre-Commit Verification
- **Verification Check**: Before committing changes, determine if the changes are testable/verifiable
- **Present Verification Steps**: When changes are verifiable, present to the user:
  1. Verification method (build, run, test command, etc.)
  2. Step-by-step instructions
  3. Expected results/success criteria
- **Example**:
  ```
  Verification is possible:
  1. Run ./gradlew :fabric:build -Ptarget_mc_version=1.21.2
  2. Confirm that the build succeeds
  3. Confirm that JAR files are generated in fabric/1.21.2/build/libs/
  Expected result: Build completes without errors and JAR files are generated
  ```
- **Wait for User Decision**: Allow user to decide whether to proceed with verification before committing

<!-- MANUAL ADDITIONS END -->
