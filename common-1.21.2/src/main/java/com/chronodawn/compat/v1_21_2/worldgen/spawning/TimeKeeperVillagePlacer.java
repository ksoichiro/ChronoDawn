package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.TimeKeeperVillageData;
import com.chronodawn.entities.mobs.TimeKeeperEntity;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Time Keeper Village Placer
 *
 * Handles programmatic placement of Time Keeper Village structure in ChronoDawn dimension.
 *
 * Placement Strategy:
 * - Location: 32-64 blocks from ChronoDawn spawn point
 * - Trigger: First player entry to ChronoDawn dimension
 * - Max per world: 1 (tracked via SavedData)
 *
 * Design:
 * - Small trading post with 2 Time Keepers
 * - Uses Time Wood blocks for construction
 * - Placed on flat terrain (heightmap-aware)
 *
 * Reference: research.md "Time Keeper Village Design (T274)"
 * Task: T276 [US2] Implement TimeKeeperVillagePlacer.java
 */
public class TimeKeeperVillagePlacer {
    private static final ResourceLocation VILLAGE_TEMPLATE = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "time_keeper_village"
    );

    private static final ResourceKey<LootTable> VILLAGE_LOOT_TABLE = ResourceKey.create(
        Registries.LOOT_TABLE,
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "chests/time_keeper_village")
    );

    // Placement distance constraints (will expand if no suitable location found)
    private static final int[][] DISTANCE_RANGES = {
        {32, 64},    // Phase 1: 32-64 blocks (ideal)
        {64, 128},   // Phase 2: 64-128 blocks (fallback)
        {128, 256},  // Phase 3: 128-256 blocks (last resort)
    };

    // Structure dimensions (from design)
    private static final int STRUCTURE_WIDTH = 11;  // X dimension
    private static final int STRUCTURE_DEPTH = 11;  // Z dimension

    // Terrain flatness tolerance
    private static final int MAX_HEIGHT_VARIATION = 3;

    // Maximum placement attempts
    private static final int MAX_ATTEMPTS = 100;

    /**
     * Called when a player enters ChronoDawn dimension.
     * Places the Time Keeper Village if not already placed.
     *
     * @param player Player who entered ChronoDawn
     */
    public static void onPlayerEnterChronoDawn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        // Verify we're in ChronoDawn dimension
        if (!level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        // Check if village is already placed
        TimeKeeperVillageData data = TimeKeeperVillageData.get(level);
        if (data.isPlaced()) {
            ChronoDawn.LOGGER.debug("Time Keeper Village already placed at {}", data.getPosition());
            return;
        }

        // Find suitable position near player's entry point (not world spawn)
        // This ensures the village is near where the player actually enters ChronoDawn
        BlockPos playerEntryPos = player.blockPosition();
        BlockPos villagePos = findSuitablePosition(level, playerEntryPos);

        if (villagePos != null) {
            boolean success = placeVillage(level, villagePos);
            if (success) {
                data.setPlaced(villagePos);
                ChronoDawn.LOGGER.info("Successfully placed Time Keeper Village at {} for player {}",
                    villagePos, player.getName().getString());
            } else {
                ChronoDawn.LOGGER.warn("Failed to place Time Keeper Village structure at {}", villagePos);
            }
        } else {
            ChronoDawn.LOGGER.warn("Could not find suitable position for Time Keeper Village near player entry point {}",
                playerEntryPos);
        }
    }

    /**
     * Find a suitable position for the village near the entry point.
     *
     * Criteria:
     * - Starts with 32-64 blocks, expands to 256 blocks if needed
     * - Flat terrain (height variation <= 3 blocks over 11x11 area)
     * - On surface (uses MOTION_BLOCKING heightmap for runtime placement)
     *
     * @param level ServerLevel
     * @param center Center position (player entry point)
     * @return Suitable position, or null if none found
     */
    private static BlockPos findSuitablePosition(ServerLevel level, BlockPos center) {
        // Try each distance range, expanding if no suitable location found
        for (int phase = 0; phase < DISTANCE_RANGES.length; phase++) {
            int minDistance = DISTANCE_RANGES[phase][0];
            int maxDistance = DISTANCE_RANGES[phase][1];

            ChronoDawn.LOGGER.info("Searching for Time Keeper Village location (phase {}: {}-{} blocks)",
                phase + 1, minDistance, maxDistance);

            BlockPos result = searchInRange(level, center, minDistance, maxDistance);
            if (result != null) {
                return result;
            }
        }

        ChronoDawn.LOGGER.warn("No suitable position found after searching all distance ranges");
        return null;
    }

    /**
     * Search for a suitable position within a specific distance range.
     *
     * @param level ServerLevel
     * @param center Center position
     * @param minDistance Minimum distance from center
     * @param maxDistance Maximum distance from center
     * @return Suitable position, or null if none found in this range
     */
    private static BlockPos searchInRange(ServerLevel level, BlockPos center, int minDistance, int maxDistance) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Generate random position within distance range
            int range = maxDistance - minDistance;
            int dx = level.random.nextInt(range + 1) + minDistance;
            int dz = level.random.nextInt(range + 1) + minDistance;

            // Randomize direction
            if (level.random.nextBoolean()) dx = -dx;
            if (level.random.nextBoolean()) dz = -dz;

            int x = center.getX() + dx;
            int z = center.getZ() + dz;

            // Force chunk generation before querying heightmap
            // Without this, heightmap returns -64 for ungenerated chunks
            level.getChunk(x >> 4, z >> 4);

            // Get surface height at center (MOTION_BLOCKING for runtime)
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

            // Sanity check: if Y is still at minimum world height, skip this position
            if (y <= level.getMinY()) {
                ChronoDawn.LOGGER.debug("Skipping position ({}, {}) - heightmap returned minimum height {}", x, z, y);
                continue;
            }

            BlockPos candidate = new BlockPos(x, y, z);

            // Check if terrain is flat enough
            if (isTerrainFlat(level, candidate, STRUCTURE_WIDTH, STRUCTURE_DEPTH, MAX_HEIGHT_VARIATION)) {
                // Check if surface is suitable (not water, not lava)
                if (isSurfaceSuitable(level, candidate)) {
                    ChronoDawn.LOGGER.info("Found suitable position for Time Keeper Village at {} (attempt {})",
                        candidate, attempt + 1);
                    return candidate;
                }
            }
        }

        return null;
    }

    /**
     * Check if terrain is flat enough for structure placement.
     *
     * @param level ServerLevel
     * @param center Center position
     * @param width Structure width (X)
     * @param depth Structure depth (Z)
     * @param maxVariation Maximum height variation allowed
     * @return true if terrain is flat enough
     */
    private static boolean isTerrainFlat(ServerLevel level, BlockPos center, int width, int depth, int maxVariation) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        int halfWidth = width / 2;
        int halfDepth = depth / 2;

        for (int dx = -halfWidth; dx <= halfWidth; dx++) {
            for (int dz = -halfDepth; dz <= halfDepth; dz++) {
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX() + dx, center.getZ() + dz);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }

        return (maxY - minY) <= maxVariation;
    }

    /**
     * Check if the surface is suitable for structure placement.
     * Avoids water, lava, and other unsuitable surfaces.
     *
     * @param level ServerLevel
     * @param pos Surface position
     * @return true if surface is suitable
     */
    private static boolean isSurfaceSuitable(ServerLevel level, BlockPos pos) {
        // Check center and corners
        BlockPos[] checkPositions = {
            pos,
            pos.offset(STRUCTURE_WIDTH / 2, 0, STRUCTURE_DEPTH / 2),
            pos.offset(-STRUCTURE_WIDTH / 2, 0, STRUCTURE_DEPTH / 2),
            pos.offset(STRUCTURE_WIDTH / 2, 0, -STRUCTURE_DEPTH / 2),
            pos.offset(-STRUCTURE_WIDTH / 2, 0, -STRUCTURE_DEPTH / 2)
        };

        for (BlockPos checkPos : checkPositions) {
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, checkPos.getX(), checkPos.getZ());
            BlockState surfaceBlock = level.getBlockState(new BlockPos(checkPos.getX(), y - 1, checkPos.getZ()));

            // Avoid water
            if (surfaceBlock.is(Blocks.WATER)) {
                return false;
            }

            // Avoid lava
            if (surfaceBlock.is(Blocks.LAVA)) {
                return false;
            }

            // Avoid ice (frozen water)
            if (surfaceBlock.is(Blocks.ICE) || surfaceBlock.is(Blocks.PACKED_ICE) || surfaceBlock.is(Blocks.BLUE_ICE)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Place the Time Keeper Village structure at the specified position.
     *
     * @param level ServerLevel
     * @param pos Placement position
     * @return true if placement succeeded
     */
    private static boolean placeVillage(ServerLevel level, BlockPos pos) {
        var structureTemplateManager = level.getStructureManager();

        // Load template
        var templateOptional = structureTemplateManager.get(VILLAGE_TEMPLATE);
        if (templateOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load Time Keeper Village template: {}", VILLAGE_TEMPLATE);
            ChronoDawn.LOGGER.error("Make sure the NBT file exists at: data/chronodawn/structure/time_keeper_village.nbt");
            return false;
        }

        StructureTemplate template = templateOptional.get();

        // Get template size
        var templateSize = template.getSize();
        ChronoDawn.LOGGER.info("Time Keeper Village template size: {}x{}x{}",
            templateSize.getX(), templateSize.getY(), templateSize.getZ());

        // Calculate placement position (center the structure)
        int offsetX = templateSize.getX() / 2;
        int offsetZ = templateSize.getZ() / 2;
        BlockPos placementPos = pos.offset(-offsetX, 0, -offsetZ);

        // Fill foundation below structure to prevent floating/gaps
        fillFoundation(level, placementPos, templateSize.getX(), templateSize.getZ());

        // Create placement settings - ignore entities in NBT, we spawn them programmatically
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setIgnoreEntities(true);

        // Place structure
        template.placeInWorld(level, placementPos, placementPos, settings, level.random, 2);

        ChronoDawn.LOGGER.info("Placed Time Keeper Village at {} (template origin: {})",
            pos, placementPos);

        // Spawn Time Keepers programmatically
        spawnTimeKeepers(level, pos);

        // Set loot table for chests in the structure
        setChestLootTables(level, placementPos, templateSize);

        return true;
    }

    /**
     * Spawn Time Keeper entities at the village.
     *
     * Spawns 2 Time Keepers at fixed positions relative to village center.
     *
     * @param level ServerLevel
     * @param villageCenter Center position of the village
     */
    private static void spawnTimeKeepers(ServerLevel level, BlockPos villageCenter) {
        // Spawn positions relative to village center (Y+1 to be on floor level)
        BlockPos[] spawnPositions = {
            villageCenter.offset(-2, 1, 0),  // First Time Keeper
            villageCenter.offset(2, 1, 0)    // Second Time Keeper
        };

        int spawnedCount = 0;
        for (BlockPos spawnPos : spawnPositions) {
            TimeKeeperEntity timeKeeper = ModEntities.TIME_KEEPER.get().create(level, net.minecraft.world.entity.EntitySpawnReason.STRUCTURE);
            if (timeKeeper != null) {
                timeKeeper.moveTo(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    level.random.nextFloat() * 360.0F,
                    0.0F
                );
                timeKeeper.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(spawnPos),
                    EntitySpawnReason.STRUCTURE,
                    null
                );
                timeKeeper.setPersistenceRequired();  // Prevent despawning
                level.addFreshEntity(timeKeeper);
                spawnedCount++;

                ChronoDawn.LOGGER.info("Spawned Time Keeper at {}", spawnPos);
            }
        }

        ChronoDawn.LOGGER.info("Spawned {} Time Keepers at Time Keeper Village", spawnedCount);
    }

    /**
     * Set loot tables for all chests in the placed structure.
     *
     * @param level ServerLevel
     * @param placementPos Structure placement origin
     * @param templateSize Structure size (from template)
     */
    private static void setChestLootTables(ServerLevel level, BlockPos placementPos, net.minecraft.core.Vec3i templateSize) {
        int chestsFound = 0;

        // Scan the entire structure volume for chests
        for (int dx = 0; dx < templateSize.getX(); dx++) {
            for (int dy = 0; dy < templateSize.getY(); dy++) {
                for (int dz = 0; dz < templateSize.getZ(); dz++) {
                    BlockPos checkPos = placementPos.offset(dx, dy, dz);
                    BlockState blockState = level.getBlockState(checkPos);

                    // Check if this block is a chest
                    if (blockState.getBlock() instanceof ChestBlock) {
                        var blockEntity = level.getBlockEntity(checkPos);
                        if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
                            // Set the loot table
                            chestBlockEntity.setLootTable(VILLAGE_LOOT_TABLE, level.random.nextLong());
                            chestsFound++;
                            ChronoDawn.LOGGER.info("Set loot table for chest at {}", checkPos);
                        }
                    }
                }
            }
        }

        if (chestsFound > 0) {
            ChronoDawn.LOGGER.info("Set loot tables for {} chest(s) in Time Keeper Village", chestsFound);
        } else {
            ChronoDawn.LOGGER.warn("No chests found in Time Keeper Village structure");
        }
    }

    /**
     * Fill foundation below the structure to prevent floating/gaps.
     * Uses dirt for natural appearance, filling down until hitting solid ground.
     *
     * @param level ServerLevel
     * @param placementPos Structure placement origin
     * @param width Structure width (X)
     * @param depth Structure depth (Z)
     */
    private static void fillFoundation(ServerLevel level, BlockPos placementPos, int width, int depth) {
        int filledBlocks = 0;
        int maxDepth = 10;  // Maximum depth to fill

        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int x = placementPos.getX() + dx;
                int z = placementPos.getZ() + dz;
                int surfaceY = placementPos.getY();

                // Fill downward from structure base until hitting solid ground
                for (int dy = 1; dy <= maxDepth; dy++) {
                    BlockPos fillPos = new BlockPos(x, surfaceY - dy, z);
                    BlockState existingBlock = level.getBlockState(fillPos);

                    // Stop if we hit solid ground
                    if (existingBlock.isSolid() && !existingBlock.is(Blocks.WATER) && !existingBlock.is(Blocks.LAVA)) {
                        break;
                    }

                    // Fill with dirt (or coarse dirt for variety)
                    level.setBlock(fillPos, Blocks.DIRT.defaultBlockState(), 2);
                    filledBlocks++;
                }
            }
        }

        if (filledBlocks > 0) {
            ChronoDawn.LOGGER.info("Filled {} foundation blocks below Time Keeper Village", filledBlocks);
        }
    }
}

