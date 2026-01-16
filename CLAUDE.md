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

**Current Versions** (as of 2025-12-27):
- Minecraft: 1.21.1
- Fabric Loader: 0.17.3+ | Fabric API: 0.116.7+
- NeoForge: 21.1.209+
- Architectury API: 13.0.8+

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
- Java 21 (Minecraft Java Edition 1.21.1) + NeoForge 21.1.x, Fabric Loader, mcjunitlib

## Project Structure
```
src/
tests/
```

## Commands
# Add commands for Java 21 (Minecraft Java Edition 1.21.1)

## Code Style
Java 21 (Minecraft Java Edition 1.21.1): Follow standard conventions

## Build Configuration
- **Build DSL**: Groovy DSL (not Kotlin DSL) - for compatibility with Architectury Loom 1.11-SNAPSHOT
- **Mappings**: Mojang mappings (not Yarn) - code uses official Minecraft class names (e.g., `net.minecraft.core.Registry`)
- **Shadow Plugin**: com.gradleup.shadow 8.3.6 - for bundling common module into platform-specific JARs

## Multi-Version Support

**Supported Versions**: Minecraft 1.20.1 + 1.21.1 (single codebase)

**Build Commands**:
- `./gradlew build1_20_1` - Build for 1.20.1
- `./gradlew build1_21_1` - Build for 1.21.1 (default)
- `./gradlew buildAll` - Build all versions

**Run Client**:
- Fabric: `./gradlew fabric:runClient1_20_1` or `./gradlew fabric:runClient1_21_1`
- NeoForge: `./gradlew neoforge:runClient1_20_1` or `./gradlew neoforge:runClient1_21_1`

**Key Strategy**: Custom Gradle scripts + abstraction layer (`compat/` package) for API differences

## Development Notes
- When writing code, use Mojang mapping names (e.g., `net.minecraft.world.level.Level`, not Yarn's `class_XXXX`)
- Build files use Groovy syntax (e.g., `maven { url 'https://...' }`, not `maven { url = "https://..." }`)
- Common module code is bundled into Fabric JAR using Shadow plugin

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

## Critical Game Design Decisions

**Respawn Mechanics**:
- Players respawn at bed/respawn anchor, or world spawn if none set
- Portal Stabilizer does NOT affect respawn location
- Portal Stabilizer only makes portal bidirectional (one-way → two-way)
- Players can always escape by breaking bed and dying

**Rationale**: Maintains tension (one-way portal) without excessive difficulty (can always escape)

## Structure Worldgen Guidelines

**Key Topics**:
- **Underground Structure Waterlogging**: Solutions for Aquifer-related waterlogging in Minecraft 1.18+
- **Advanced Waterlogging Prevention**: Three-component system (Custom Decorative Water, Structure Processor, Mixin)
- **Structure Generation Priority**: Using `step` parameter to control generation order
- **Advanced Jigsaw Techniques**: Hybrid Jigsaw + programmatic approach for guaranteed boss room placement
- **Terrain Adaptation**: Configuring terrain integration for underground + surface structures
- **Boss Spawning System**: Proximity-based spawning patterns

**Guidelines**:
- Avoid waterloggable blocks in deep underground structures (Y < 40)
- Use `terrain_adaptation: "none"` for structures with surface + underground components
- For boss rooms: Use marker-based programmatic placement with rotation-aware calculations
- Reference implementation: Phantom Catacombs (T236m-r), Master Clock (T234-238)

## Texture Creation Guidelines

**Approach**: Extract vanilla textures and apply RGB channel color transformation for rapid, consistent texture creation.

**Quick Steps**:
- Extract from Minecraft JAR: `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft/1.21.1/minecraft-1.21.1.jar`
- Analyze colors: `magick texture.png -format %c histogram:info:-`
- Transform: `magick input.png -channel R -evaluate multiply 0.95 +channel ...`
- Known transformation: **Time Wood** (from Jungle) → R×0.95, G×1.17, B×0.85

## Chronicle Structure Addition Guidelines

**Workflow**: 3-step process for adding structures with images to Chronicle.

**Steps**:
- Screenshot location: `assets/screenshots/chronicle/<structure_name>.png`
- Convert script: `./scripts/convert_chronicle_image.sh <structure_name>.png`
- Output location: `common/src/main/resources/assets/chronodawn/textures/gui/chronicle/<structure_name>.png`
- JSON entry: Add `{"image": "chronodawn:textures/gui/chronicle/<structure_name>.png"}` page
- Image features: Auto-scaling, sepia tone, 15px vignette fade effect

## Custom Noise Settings (Terrain Generation)

**Difficulty Assessment** (based on T299 experiment, 2025-12-13):
- **Simple approach** (BiomeScalingMixin): Easy, effective for biome size adjustment
- **Custom noise settings**: Very difficult, requires 10-15 hours of trial-and-error

**Recommendation**:
- For biome size: Use Mixin-based coordinate scaling
- For terrain customization: Start with vanilla density functions as base, modify incrementally
- Avoid creating noise_settings from scratch unless absolutely necessary

<!-- MANUAL ADDITIONS END -->
