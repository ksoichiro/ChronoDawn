# Chronosphere Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-10-19

---

## Documentation Maintenance

**CRITICAL**: When updating versions, dependencies, or content, documentation must be kept consistent across all files.

**Documentation Maintenance Skill**: Detailed checklists and guidance are available in `.claude/skills/doc-maintenance.md`. Claude will automatically reference this skill when you:
- Update Minecraft or dependency versions
- Add new bosses, structures, or items
- Prepare for a release
- Add new mod dependencies

Simply describe what you want to do (e.g., "I want to update Minecraft to 1.21.2" or "I added a new boss"), and Claude will automatically use the appropriate checklist.

**Quick Reference - Key Files**:
- `README.md` - Project overview and installation
- `docs/player_guide.md` - Player guide
- `docs/developer_guide.md` - Developer guide
- `docs/curseforge_description.md` - CurseForge page
- `docs/modrinth_description.md` - Modrinth page
- `gradle.properties` - Version definitions
- `fabric.mod.json` / `neoforge.mods.toml` - Mod metadata

**Current Versions** (as of 2025-12-08):
- Minecraft: 1.21.1
- Fabric Loader: 0.17.3+ | Fabric API: 0.116.7+
- NeoForge: 21.1.209+
- Architectury API: 13.0.8+ | Custom Portal API: 0.0.1-beta66-1.21+ | Patchouli: 1.21.1-92+

---

## Active Technologies
- Java 21 (Minecraft Java Edition 1.21.1) + NeoForge 21.1.x, Fabric Loader, mcjunitlib (001-chronosphere-mod)
- Custom Portal API 0.0.1-beta66-1.21 (Fabric) - for custom portal implementation

## Project Structure
```
src/
tests/
```

## Commands
# Add commands for Java 21 (Minecraft Java Edition 1.21.1)

## Code Style
Java 21 (Minecraft Java Edition 1.21.1): Follow standard conventions

## Recent Changes
- 001-chronosphere-mod: Added Java 21 (Minecraft Java Edition 1.21.1) + NeoForge 21.1.x, mcjunitlib
- 2025-10-23: Migrated to Groovy DSL and Mojang mappings for Minecraft 1.21.1 compatibility
- 2025-10-24: Implemented Time Distortion Effect (Slowness IV for hostile mobs in Chronosphere)
- 2025-10-24: Added Custom Portal API 0.0.1-beta66-1.21 dependency for future portal implementation
- 2025-10-26: **CRITICAL DESIGN DECISION**: Respawn mechanics follow Minecraft standard (End-like), not custom logic (see spec.md "Game Design Philosophy")
- 2025-10-27: Implemented Time Wood tree worldgen with custom blocks (Log, Leaves, Planks, Sapling)
- 2025-10-27: Fixed leaves decay logic using minecraft:logs tag and distance tracking (T079-T080)

## Build Configuration
- **Build DSL**: Groovy DSL (not Kotlin DSL) - for compatibility with Architectury Loom 1.11-SNAPSHOT
- **Mappings**: Mojang mappings (not Yarn) - code uses official Minecraft class names (e.g., `net.minecraft.core.Registry`)
- **Shadow Plugin**: com.gradleup.shadow 8.3.6 - for bundling common module into platform-specific JARs

## Development Notes
- When writing code, use Mojang mapping names (e.g., `net.minecraft.world.level.Level`, not Yarn's `class_XXXX`)
- Build files use Groovy syntax (e.g., `maven { url 'https://...' }`, not `maven { url = "https://..." }`)
- Common module code is bundled into Fabric JAR using Shadow plugin

## Mixin Configuration

**CRITICAL**: Fabric and NeoForge require **different** Mixin configurations due to mapping differences.

**Mixin Configuration Skill**: Detailed guidance for configuring Mixins in Architectury multi-loader projects is available in `.claude/skills/mixin-guide.md`. Claude will automatically reference this skill when you:
- Add new Mixin classes
- Configure Mixin files for Fabric or NeoForge
- Debug Mixin-related errors (InvalidInjectionException, refMap issues)

**Quick Reference**:
- **Fabric**: Must include `"refmap": "common-common-refmap.json"` in `chronosphere-fabric.mixins.json`
- **NeoForge**: Must NOT include refMap property in `chronosphere-neoforge.mixins.json`
- **Common**: `chronosphere.mixins.json` excluded from builds (reference only)
- When adding Mixins: Update BOTH loader-specific configs

<!-- MANUAL ADDITIONS START -->

## Workflow Guidelines

### Research and Investigation
- **Document Research Results**: When conducting research or investigation for future tasks, always save findings to appropriate files (e.g., `specs/001-chronosphere-mod/research.md`) rather than keeping them only in session conversation
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

### Respawn Mechanics (2025-10-26)
**Decision**: Chronosphere follows Minecraft's standard respawn behavior (like End dimension)
- Players respawn at bed/respawn anchor, or world spawn if none set
- Portal Stabilizer does NOT affect respawn location
- Portal Stabilizer only makes portal bidirectional (one-way → two-way)
- Players can always escape by breaking bed and dying

**Rationale**: Maintains tension (one-way portal) without excessive difficulty (can always escape)

**See**: `specs/001-chronosphere-mod/spec.md` → "Game Design Philosophy" section for full details

## Structure Worldgen Guidelines

**Structure Worldgen Skill**: Comprehensive guidance for implementing complex structure generation is available in `.claude/skills/structure-worldgen.md`. Claude will automatically reference this skill when you:
- Create underground or multi-chunk structures
- Implement waterlogging prevention systems
- Use Jigsaw structures with programmatic boss room placement
- Configure terrain adaptation for structures
- Implement boss spawning systems

**Key Topics Covered**:
- **Underground Structure Waterlogging**: Solutions for Aquifer-related waterlogging in Minecraft 1.18+
- **Advanced Waterlogging Prevention**: Three-component system (Custom Decorative Water, Structure Processor, Mixin)
- **Structure Generation Priority**: Using `step` parameter to control generation order
- **Advanced Jigsaw Techniques**: Hybrid Jigsaw + programmatic approach for guaranteed boss room placement
- **Terrain Adaptation**: Configuring terrain integration for underground + surface structures
- **Boss Spawning System**: Proximity-based spawning patterns

**Quick Reference**:
- Avoid waterloggable blocks in deep underground structures (Y < 40)
- Use `terrain_adaptation: "none"` for structures with surface + underground components
- For boss rooms: Use marker-based programmatic placement with rotation-aware calculations
- Reference implementation: Phantom Catacombs (T236m-r), Master Clock (T234-238)

## Texture Creation Guidelines

**Texture Creation Skill**: Detailed guidance for creating mod textures using vanilla texture reuse and color transformation is available in `.claude/skills/texture-creation.md`. Claude will automatically reference this skill when you:
- Create textures for custom blocks or items
- Reuse vanilla textures with color modifications
- Use ImageMagick for texture processing
- Create wood variant textures (doors, trapdoors, etc.)

**Approach**: Extract vanilla textures and apply RGB channel color transformation for rapid, consistent texture creation.

**Quick Reference**:
- Extract from Minecraft JAR: `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft/1.21.1/minecraft-1.21.1.jar`
- Analyze colors: `magick texture.png -format %c histogram:info:-`
- Transform: `magick input.png -channel R -evaluate multiply 0.95 +channel ...`
- Known transformation: **Time Wood** (from Jungle) → R×0.95, G×1.17, B×0.85

<!-- MANUAL ADDITIONS END -->
