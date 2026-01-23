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

**Current Versions** (as of 2026-01-23):
- Minecraft: 1.21.1 or 1.21.2
- For 1.21.1: Fabric Loader 0.17.3+ | Fabric API 0.116.7+ | NeoForge 21.1.209+ | Architectury API 13.0.8+
- For 1.21.2: Fabric Loader 0.17.3+ | Fabric API | NeoForge 21.2.0-beta+ | Architectury API 14.0.4+

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
- Java 21 (Minecraft Java Edition 1.21.1 / 1.21.2) + NeoForge 21.1.x / 21.2.x, Fabric Loader, mcjunitlib

## Project Structure
```
src/
tests/
```

## Commands
# Add commands for Java 21 (Minecraft Java Edition 1.21.1 / 1.21.2)

## Code Style
Java 21 (Minecraft Java Edition 1.21.1 / 1.21.2): Follow standard conventions

## Build Configuration
- **Build DSL**: Groovy DSL (not Kotlin DSL) - for compatibility with Architectury Loom 1.11-SNAPSHOT
- **Mappings**: Mojang mappings (not Yarn) - code uses official Minecraft class names (e.g., `net.minecraft.core.Registry`)
- **Shadow Plugin**: com.gradleup.shadow 8.3.6 - for bundling common module into platform-specific JARs

## Multi-Version Support

**Supported Versions**: Minecraft 1.20.1 + 1.21.1 + 1.21.2 (single codebase)

**Build Commands**:
- `./gradlew build1_20_1` - Build for 1.20.1
- `./gradlew build1_21_1` - Build for 1.21.1
- `./gradlew build1_21_2` - Build for 1.21.2 (default)
- `./gradlew buildAll` - Build all versions

**Run Client**:
- Fabric: `./gradlew fabric:runClient1_20_1`, `./gradlew fabric:runClient1_21_1`, or `./gradlew fabric:runClient1_21_2`
- NeoForge: `./gradlew neoforge:runClient1_20_1`, `./gradlew neoforge:runClient1_21_1`, or `./gradlew neoforge:runClient1_21_2`

**GameTest**:
- `./gradlew :fabric:runGameTest -Ptarget_mc_version=1.21.2` - Run GameTests for specific version
- `./gradlew gameTestAll` - Run GameTests for all versions and loaders

**Resource Validation**:
- `./gradlew validateResources` - Check JSON syntax and cross-references (blockstate→model, model→texture)

**Key Strategy**: Custom Gradle scripts + abstraction layer (`compat/` package) for API differences

## Development Notes
- When writing code, use Mojang mapping names (e.g., `net.minecraft.world.level.Level`, not Yarn's `class_XXXX`)
- Build files use Groovy syntax (e.g., `maven { url 'https://...' }`, not `maven { url = "https://..." }`)
- Common module code is bundled into Fabric JAR using Shadow plugin
- **Parallel build constraint**: Architectury Plugin hardcodes `.gradle/architectury/` paths (private Kotlin lazy val, no setter/API), preventing parallel builds within the same platform (fabric×fabric or neoforge×neoforge). `gameTestAll` only parallelizes fabric‖neoforge pairs. See `docs/developer_guide.md` "gameTestAll Architecture" for details.

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
  1. Run ./gradlew :fabric:build
  2. Confirm that the build succeeds
  3. Confirm that JAR files are generated in fabric/build/libs/
  Expected result: Build completes without errors and JAR files are generated
  ```
- **Wait for User Decision**: Allow user to decide whether to proceed with verification before committing

<!-- MANUAL ADDITIONS END -->
