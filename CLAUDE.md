# Chronosphere Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-10-19

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

### Underground Structure Waterlogging (2025-11-11)

**Problem**: In Minecraft 1.18+, underground structures face waterlogging issues due to the Aquifer system:
- Aquifers generate water sources throughout underground areas
- When structures generate where water exists, waterloggable blocks (stairs, lanterns, slabs, fences, etc.) automatically become `waterlogged=true`
- Water flows out from these blocks inside the structure

**Standard Solution**: **Avoid waterloggable blocks in underground structures**
- Use full blocks instead of stairs for elevation changes
- Use torches, glowstone, or sea lanterns instead of hanging lanterns
- Avoid slabs, fences, and other non-full blocks in deep underground structures

**Alternative Approaches** (less reliable):
- Processor to remove water (`remove_water.json`) - only removes water blocks, not waterlogged state
- Generate at shallower depths (Y > 40) - reduces aquifer exposure
- 2-block thick walls - does NOT solve waterlogging issue

**Vanilla Examples**:
- Ancient City: Uses waterloggable blocks but generates at Y < -30 (below most aquifers)
- Mineshaft: Allows water flooding (intentional design)
- Stronghold: Generates at Y 40-60 (minimal aquifer exposure)

**Best Practice**: For structures with significant underground portions (Y < 40), use only full blocks for decoration and avoid waterloggable blocks entirely.

**Reference**: See `specs/001-chronosphere-mod/research.md` → "Structure Waterlogging Research" for detailed analysis

### Structure Generation Priority (2025-11-11)

**Generation Order**: Structures generate in phases determined by the `step` parameter:
1. `raw_generation` - Immediately after terrain generation
2. `lakes` - Lake generation
3. `local_modifications` - Local terrain modifications
4. `underground_structures` - Underground structures (early)
5. `surface_structures` - Surface structures (late)
6. `strongholds` - Strongholds
7. `underground_ores` - Ore generation
8. ... (vegetation, decoration, etc.)

**Priority Rule**: Earlier steps have priority over later steps. Later structures avoid overwriting earlier structures.

**Use Case**: For unique/important structures (like Master Clock), use `underground_structures` step even if they have surface components. This prevents common structures (using `surface_structures`) from overwriting them.

**Limitation**: Structures in different `structure_set` definitions have weak collision detection. Overlaps can still occur but are less likely when using earlier generation steps.

<!-- MANUAL ADDITIONS END -->
