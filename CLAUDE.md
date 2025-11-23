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

### Advanced Solution: Complete Waterlogging Prevention System (2025-11-24)

**Use Case**: When you need to:
1. Use waterloggable blocks in underground structures (stairs, lanterns, slabs, etc.)
2. Include decorative water features (waterfalls, pools) in the same structures
3. Support intentional waterlogging (waterlogged slabs for decorative pools)

**Problem**: Simple Mixin-based water removal cannot distinguish between:
- Aquifer water (unwanted) vs Decorative water (wanted)
- Aquifer-induced waterlogging (unwanted) vs Intentional waterlogging (wanted)

**Complete Solution: Three-Component System**

**1. Custom Decorative Water Fluid** (`DecorativeWaterFluid.java`, `ModFluids.java`):
- Create `chronosphere:decorative_water` fluid that looks identical to vanilla water
- Use in NBT structures for decorative water features (waterfalls, pools)
- Distinguishable from Aquifer water (`minecraft:water`) during generation
- Preserves flow state (level=0-15) for waterfalls

**2. Structure Processor** (`CopyFluidLevelProcessor.java`):
```java
@Override
public StructureBlockInfo processBlock(...) {
    // Phase 1: During structure placement (per-block processing)

    // A. Convert decorative water → vanilla water (preserve flow)
    if (state.is(ModBlocks.DECORATIVE_WATER.get())) {
        int level = state.getValue(BlockStateProperties.LEVEL);
        return new StructureBlockInfo(pos, Blocks.WATER.defaultBlockState()
            .setValue(BlockStateProperties.LEVEL, level), nbt);
    }

    // B. Record intentional waterlogging positions
    BlockPos worldPos = currentBlockInfo.pos();  // IMPORTANT: use currentBlockInfo.pos()!
    if (originalBlockInfo.state().getValue(WATERLOGGED) == true) {
        INTENTIONAL_WATERLOGGING.add(worldPos.immutable());
    }

    // C. Remove ALL waterlogging temporarily (prevents water spread)
    if (currentBlockInfo.state().getValue(WATERLOGGED) == true) {
        return new StructureBlockInfo(pos, state.setValue(WATERLOGGED, false), nbt);
    }
}
```

**3. Mixin** (`StructureStartMixin.java`):
```java
@Inject(method = "placeInChunk", at = @At("HEAD"))
private void removeWaterBeforePlacement(...) {
    // Phase 2a: Before structure placement
    // Remove ONLY minecraft:water (Aquifer), preserve chronosphere:decorative_water
    for (BlockPos pos : structurePieceBoundingBox) {
        if (state.is(Blocks.WATER) && !state.is(ModBlocks.DECORATIVE_WATER)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}

@Inject(method = "placeInChunk", at = @At("RETURN"))
private void finalizeWaterlogging(...) {
    // Phase 2b: After structure placement
    // Process current chunk + 1-block border (fixes multi-chunk timing issues)
    for (BlockPos pos : chunkBox.expanded(1, 0, 1)) {
        BlockPos immutablePos = pos.immutable();

        if (state.hasProperty(WATERLOGGED)) {
            boolean shouldBeWaterlogged = INTENTIONAL_WATERLOGGING.contains(immutablePos);

            // Restore intentional, remove unintentional
            if (state.getValue(WATERLOGGED) != shouldBeWaterlogged) {
                level.setBlock(pos, state.setValue(WATERLOGGED, shouldBeWaterlogged), 2);
            }

            // Cleanup: remove from set after processing
            if (shouldBeWaterlogged) {
                INTENTIONAL_WATERLOGGING.remove(immutablePos);
            }
        }
    }
}
```

**Processing Flow**:
```
1. Mixin HEAD: Remove Aquifer water (minecraft:water only)
2. Structure Placement:
   - NBT blocks placed
   - Processor runs for each block:
     * Records intentional waterlogging positions
     * Removes all waterlogging (prevents spread during placement)
     * Converts decorative_water → minecraft:water
3. Mixin RETURN:
   - Restores intentional waterlogging (from recorded positions)
   - Removes unintentional waterlogging (Aquifer or water spread)
   - Processes chunk + 1-block border (fixes multi-chunk structures)
```

**Critical Implementation Details**:

1. **Correct World Position** (Most Common Bug):
   ```java
   // WRONG: blockPos parameter is structure origin (same for all blocks!)
   BlockPos worldPos = blockPos;

   // CORRECT: Get actual position from currentBlockInfo
   BlockPos worldPos = currentBlockInfo.pos();
   ```

2. **Immutable BlockPos for Set Storage**:
   ```java
   INTENTIONAL_WATERLOGGING.add(worldPos.immutable());  // Store immutable
   INTENTIONAL_WATERLOGGING.contains(pos.immutable());   // Check immutable
   ```

3. **Multi-Chunk Structures** (Timing Issue):
   - Each chunk processes separately via `placeInChunk()`
   - Later chunks can waterlog blocks in earlier (already processed) chunks
   - Solution: Expand processing area by 1 block: `chunkBox.expanded(1, 0, 1)`
   - Don't clear `INTENTIONAL_WATERLOGGING` set until all chunks processed

4. **Set Cleanup**:
   ```java
   // WRONG: Clear entire set after each chunk
   INTENTIONAL_WATERLOGGING.clear();

   // CORRECT: Remove individual positions after restoration
   if (shouldBeWaterlogged) {
       INTENTIONAL_WATERLOGGING.remove(immutablePos);
   }
   ```

**Capabilities**:
- ✅ Prevents Aquifer water from waterlogging stairs, lanterns, slabs, etc.
- ✅ Supports decorative water features (waterfalls with flow)
- ✅ Supports intentional waterlogging (waterlogged slabs for pools)
- ✅ Works with multi-chunk structures
- ✅ Compatible with Jigsaw structures

**Files**:
- `DecorativeWaterFluid.java` - Custom fluid (Source + Flowing)
- `ModFluids.java` - Fluid registration
- `CopyFluidLevelProcessor.java` - Structure processor
- `ModStructureProcessorTypes.java` - Processor type registration
- `StructureStartMixin.java` - Mixin for water removal and waterlogging finalization
- `convert_decorative_water.json` - Processor list (applied to template pools)

**Reference**: Master Clock (T234-238) - uses stairs, decorative water, intentional waterlogging

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

## Texture Creation Guidelines

### Reusing Vanilla Textures with Color Transformation (2025-11-15)

**Approach**: For custom wood variants, extract vanilla textures and apply color transformation to maintain visual consistency while ensuring rapid texture creation.

**Workflow**:

1. **Extract Vanilla Textures**
   - Locate Minecraft client JAR: `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft/1.21.1/minecraft-1.21.1.jar`
   - Extract textures using `unzip`:
     ```bash
     unzip -j minecraft-1.21.1.jar \
       "assets/minecraft/textures/block/jungle_door_top.png" \
       "assets/minecraft/textures/block/jungle_door_bottom.png" \
       "assets/minecraft/textures/block/jungle_trapdoor.png" \
       "assets/minecraft/textures/item/jungle_door.png"
     ```
   - Use similar wood type as base (e.g., Jungle for yellowish woods, Oak for brown woods)

2. **Determine Color Transformation Parameters**
   - Compare reference planks (e.g., Time Wood Planks vs Jungle Planks)
   - Use ImageMagick histogram to analyze color distributions:
     ```bash
     magick time_wood_planks.png -format %c histogram:info:-
     magick jungle_planks.png -format %c histogram:info:-
     ```
   - Calculate RGB channel multipliers by comparing dominant colors
   - Example for Time Wood (yellowish-olive from brown):
     - Red: 0.95× (slight reduction)
     - Green: 1.17× (significant increase for yellow tone)
     - Blue: 0.85× (reduction for warmth)

3. **Apply Color Transformation with ImageMagick**
   ```bash
   magick input.png \
     -channel R -evaluate multiply 0.95 +channel \
     -channel G -evaluate multiply 1.17 +channel \
     -channel B -evaluate multiply 0.85 +channel \
     output.png
   ```

4. **Iterate Based on Visual Feedback**
   - Test textures in-game
   - Adjust RGB multipliers if colors don't match:
     - Too green → reduce G multiplier, increase R/B
     - Too yellow → reduce G multiplier
     - Too dark/bright → adjust all channels proportionally

**Known Color Transformations**:
- **Time Wood** (from Jungle): R×0.95, G×1.17, B×0.85 - produces yellowish-olive tone
  - Applied to: Door, Trapdoor textures (T080v-T080aa)

**Benefits**:
- Rapid texture creation (batch processing with ImageMagick)
- Visual consistency across all variants
- Easy iteration and adjustment
- Reusable approach for future wood types

**Notes**:
- HSV color space transformations may not work well for fundamental color shifts (e.g., brown → yellow-green)
- RGB channel multiplication provides more control for cross-color transformations
- Always verify textures in-game before finalizing

<!-- MANUAL ADDITIONS END -->
