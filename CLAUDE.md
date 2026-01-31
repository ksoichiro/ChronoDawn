# Chrono Dawn Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-10-19

---

## Documentation Maintenance

**CRITICAL**: When updating versions, dependencies, or content, documentation must be kept consistent across all files.

**Key Files**:
- `README.md` - Project overview and installation
- `docs/player_guide.md` - Player guide
- `docs/developer_guide.md` - Developer guide
- `docs/curseforge_description.md` - CurseForge page
- `docs/modrinth_description.md` - Modrinth page
- `gradle.properties` - Version definitions
- `fabric.mod.json` / `neoforge.mods.toml` - Mod metadata

**Current Versions** (as of 2026-01-31):
- Minecraft: 1.21.1, 1.21.2, 1.21.3, 1.21.4, or 1.21.5
- For 1.21.1: Fabric Loader 0.17.3+ | Fabric API 0.116.7+ | NeoForge 21.1.209+ | Architectury API 13.0.8+
- For 1.21.2/1.21.3: Fabric Loader 0.17.3+ | Fabric API | NeoForge 21.2.0-beta+ / 21.3.0-beta+ | Architectury API 14.0.4+
- For 1.21.4: Fabric Loader 0.17.3+ | Fabric API 0.110.5+ | NeoForge 21.4.0-beta+ | Architectury API 15.0.1+
- For 1.21.5: Fabric Loader 0.17.3+ | Fabric API 0.121.0+ | NeoForge 21.5.96+ | Architectury API 16.1.4+
- **Note**: 1.21.3 is a hotfix release that shares modules with 1.21.2

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
- Java 21 (Minecraft Java Edition 1.21.1 / 1.21.2 / 1.21.3 / 1.21.4 / 1.21.5) + NeoForge 21.1.x / 21.2.x / 21.3.x / 21.4.x / 21.5.x, Fabric Loader, mcjunitlib

## Project Structure
```
common-shared/        (shared version-agnostic sources, NOT a Gradle subproject)
common-gametest/      (shared gametest sources, NOT a Gradle subproject)
common-1.20.1/        (version-specific common module)
common-1.21.1/        (version-specific common module)
common-1.21.2/        (version-specific common module)
common-1.21.4/        (version-specific common module)
common-1.21.5/        (version-specific common module)
fabric-base/          (shared Fabric sources, NOT a Gradle subproject)
fabric-1.20.1/        (version-specific Fabric subproject)
fabric-1.21.1/        (version-specific Fabric subproject)
fabric-1.21.2/        (version-specific Fabric subproject)
fabric-1.21.4/        (version-specific Fabric subproject)
fabric-1.21.5/        (version-specific Fabric subproject)
neoforge-base/        (shared NeoForge sources, NOT a Gradle subproject)
neoforge-1.21.1/      (version-specific NeoForge subproject)
neoforge-1.21.2/      (version-specific NeoForge subproject)
neoforge-1.21.4/      (version-specific NeoForge subproject)
neoforge-1.21.5/      (version-specific NeoForge subproject)
gradle/               (build scripts)
props/                (version-specific properties)
```

## Commands
# Add commands for Java 21 (Minecraft Java Edition 1.21.1 / 1.21.2 / 1.21.3)

## Code Style
Java 21 (Minecraft Java Edition 1.21.1 / 1.21.2 / 1.21.3 / 1.21.4 / 1.21.5): Follow standard conventions

## Build Configuration
- **Build DSL**: Groovy DSL (not Kotlin DSL) - for compatibility with Architectury Loom 1.11-SNAPSHOT
- **Mappings**: Mojang mappings (not Yarn) - code uses official Minecraft class names (e.g., `net.minecraft.core.Registry`)
- **Shadow Plugin**: com.gradleup.shadow 8.3.6 - for bundling common module into platform-specific JARs

## Multi-Version Support

**Supported Versions**: Minecraft 1.20.1 + 1.21.1 + 1.21.2 + 1.21.3 + 1.21.4 + 1.21.5 (single codebase)

**Note**: 1.21.3 is a hotfix release that reuses 1.21.2 modules (no separate common-1.21.3, fabric-1.21.3, neoforge-1.21.3 directories needed).

**Clean Commands**:
- `./gradlew clean1_20_1` - Clean for 1.20.1
- `./gradlew clean1_21_1` - Clean for 1.21.1
- `./gradlew clean1_21_2` - Clean for 1.21.2
- `./gradlew clean1_21_3` - Clean for 1.21.3 (uses 1.21.2 modules)
- `./gradlew clean1_21_4` - Clean for 1.21.4
- `./gradlew cleanAll` - Clean all versions (excludes 1.21.3 - shares 1.21.2 modules)

**Build Commands**:
- `./gradlew build1_20_1` - Build for 1.20.1
- `./gradlew build1_21_1` - Build for 1.21.1
- `./gradlew build1_21_2` - Build for 1.21.2
- `./gradlew build1_21_3` - Build for 1.21.3 (uses 1.21.2 modules, for testing only)
- `./gradlew build1_21_4` - Build for 1.21.4
- `./gradlew build1_21_5` - Build for 1.21.5 (default)
- `./gradlew buildAll` - Build for release (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5 - excludes 1.21.3)

**Run Client**:
- Fabric: `./gradlew runClientFabric1_20_1`, `./gradlew runClientFabric1_21_1`, `./gradlew runClientFabric1_21_2`, `./gradlew runClientFabric1_21_3`, `./gradlew runClientFabric1_21_4`, `./gradlew runClientFabric1_21_5`
- NeoForge: `./gradlew runClientNeoForge1_21_1`, `./gradlew runClientNeoForge1_21_2`, `./gradlew runClientNeoForge1_21_3`, `./gradlew runClientNeoForge1_21_4`, `./gradlew runClientNeoForge1_21_5`

**Unit Test** (JUnit only, not GameTest):
- `./gradlew :common-1.21.2:test -Ptarget_mc_version=1.21.2` - Run unit tests for specific version
- `./gradlew testAll` - Run unit tests for all versions (excludes 1.21.3 - shares 1.21.2 modules)

**GameTest**:
- `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.2` - Run GameTests for specific version
- `./gradlew :neoforge:runGameTestServer -Ptarget_mc_version=1.21.2` - Run NeoForge GameTests
- `./gradlew gameTestAll` - Run GameTests including 1.21.3 runtime verification (1.21.2+1.21.3 sequential)

**Resource Validation**:
- `./gradlew validateResources` - Check JSON syntax and cross-references (blockstate→model, model→texture)
- `./gradlew validateTranslations` - Cross-version translation key validation (entities, spawn eggs)

**Release**:
- `./gradlew collectJars` - Collect release JARs from all versions into `build/release/`
- `./gradlew release` - Full release pipeline: cleanAll → buildAll → collectJars

**Full Verification** (recommended before commits/PRs):
- `./gradlew checkAll` - Run all verification tasks in sequence:
  1. cleanAll - Clean all build outputs and IDE directories
  2. validateResources - JSON syntax and cross-reference checks
  3. validateTranslations - Cross-version translation key validation
  4. buildAll - Build for release (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5)
  5. testAll - Run unit tests (1.20.1, 1.21.1, 1.21.2, 1.21.4, 1.21.5)
  6. gameTestAll - Run GameTests including 1.21.3, 1.21.4, and 1.21.5 runtime verification

**Key Strategy**: Custom Gradle scripts + abstraction layer (`compat/` package) for API differences

## Development Notes
- When writing code, use Mojang mapping names (e.g., `net.minecraft.world.level.Level`, not Yarn's `class_XXXX`)
- Build files use Groovy syntax (e.g., `maven { url 'https://...' }`, not `maven { url = "https://..." }`)
- Common module code is bundled into Fabric JAR using Shadow plugin
- **Parallel GameTest**: `gameTestAll` groups configurations by Minecraft version. 1.20.1 and 1.21.1 run in parallel, while 1.21.2 and 1.21.3 run sequentially in the same thread (they share modules). Within each version, fabric and neoforge run in a single Gradle process to avoid common module build conflicts. Each version uses a separate directory (fabric-1.20.1, fabric-1.21.1, etc.) with its own `.gradle/architectury/` path.

## Mixin Configuration

**CRITICAL**: Fabric and NeoForge require **different** Mixin configurations due to mapping differences.

**Key Points**:
- **Fabric**: Must include `"refmap": "common-common-refmap.json"` in `chronodawn-fabric.mixins.json`
- **NeoForge**: Must NOT include refMap property in `chronodawn-neoforge.mixins.json`
- **Common**: `chronodawn.mixins.json` excluded from builds (reference only)
- When adding Mixins: Update BOTH loader-specific configs

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
  3. Confirm that JAR files are generated in fabric-1.21.2/build/libs/
  Expected result: Build completes without errors and JAR files are generated
  ```
- **Wait for User Decision**: Allow user to decide whether to proceed with verification before committing

<!-- MANUAL ADDITIONS END -->
