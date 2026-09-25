package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.TimeKeeperVillageSettings;
import com.chronodawn.data.TimeKeeperVillageData;
import com.chronodawn.entities.mobs.TimeKeeperEntity;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.registry.ModBlocks;
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
    // Terrain flatness tolerance
    private static final int MAX_HEIGHT_VARIATION = 3;

    // Maximum placement attempts per search range
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

        TimeKeeperVillageSettings settings = ChronoDawnConfig.get().world().timeKeeperVillage();
        if (!settings.enabled()) return;

        TimeKeeperVillageData data = TimeKeeperVillageData.get(level);
        if (data.isPlaced()) {
            ChronoDawn.LOGGER.debug("Time Keeper Village already placed at {}", data.getPosition());
            return;
        }

        var templateOptional = level.getStructureManager().get(CompatResourceLocation.parse(settings.templateId()));
        if (templateOptional.isEmpty()) {
            ChronoDawn.LOGGER.error("Failed to load Time Keeper Village template: {}", settings.templateId());
            return;
        }
        StructureTemplate template = templateOptional.get();
        BlockPos playerEntryPos = player.blockPosition();
        BlockPos villagePos = findSuitablePosition(level, playerEntryPos, template.getSize().getX(), template.getSize().getZ(), settings);

        if (villagePos != null) {
            boolean success = placeVillage(level, villagePos, template, settings);
            if (success) {
                data.setPlaced(villagePos);
                ChronoDawn.LOGGER.debug("Successfully placed Time Keeper Village at {} for player {}",
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
    private static BlockPos findSuitablePosition(
        ServerLevel level, BlockPos center, int structureWidth, int structureDepth, TimeKeeperVillageSettings settings
    ) {
        int minDistance = settings.preferredMinDistance();
        int maxDistance = settings.preferredMaxDistance();
        int phase = 1;
        while (true) {
            ChronoDawn.LOGGER.debug("Searching for Time Keeper Village location (phase {}: {}-{} blocks)", phase, minDistance, maxDistance);
            BlockPos result = searchInRange(level, center, minDistance, maxDistance, structureWidth, structureDepth);
            if (result != null) return result;
            if (maxDistance >= settings.maxDistance()) break;
            minDistance = maxDistance;
            maxDistance = Math.min(settings.maxDistance(), maxDistance * 2);
            phase++;
        }
        ChronoDawn.LOGGER.warn("No suitable position found after searching all configured distance ranges");
        return null;
    }

    private static BlockPos searchInRange(
        ServerLevel level, BlockPos center, int minDistance, int maxDistance, int structureWidth, int structureDepth
    ) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int range = maxDistance - minDistance;
            int dx = level.random.nextInt(range + 1) + minDistance;
            int dz = level.random.nextInt(range + 1) + minDistance;
            if (level.random.nextBoolean()) dx = -dx;
            if (level.random.nextBoolean()) dz = -dz;
            int x = center.getX() + dx;
            int z = center.getZ() + dz;
            level.getChunk(x >> 4, z >> 4);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            if (y <= level.getMinY()) continue;
            BlockPos candidate = new BlockPos(x, y, z);
            if (isTerrainFlat(level, candidate, structureWidth, structureDepth, MAX_HEIGHT_VARIATION)
                && isSurfaceSuitable(level, candidate, structureWidth, structureDepth)) {
                ChronoDawn.LOGGER.debug("Found suitable position for Time Keeper Village at {} (attempt {})", candidate, attempt + 1);
                return candidate;
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
    private static boolean isSurfaceSuitable(ServerLevel level, BlockPos pos, int structureWidth, int structureDepth) {
        // Check center and corners
        BlockPos[] checkPositions = {
            pos,
            pos.offset(structureWidth / 2, 0, structureDepth / 2),
            pos.offset(-structureWidth / 2, 0, structureDepth / 2),
            pos.offset(structureWidth / 2, 0, -structureDepth / 2),
            pos.offset(-structureWidth / 2, 0, -structureDepth / 2)
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
    private static boolean placeVillage(
        ServerLevel level, BlockPos pos, StructureTemplate template, TimeKeeperVillageSettings villageSettings
    ) {

        // Get template size
        var templateSize = template.getSize();
        ChronoDawn.LOGGER.debug("Time Keeper Village template size: {}x{}x{}",
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

        ChronoDawn.LOGGER.debug("Placed Time Keeper Village at {} (template origin: {})",
            pos, placementPos);

        // Spawn Time Keepers programmatically
        spawnTimeKeepers(level, pos, villageSettings);

        // Set loot table for chests in the structure
        setChestLootTables(level, placementPos, templateSize, villageSettings);

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
    private static void spawnTimeKeepers(
        ServerLevel level, BlockPos villageCenter, TimeKeeperVillageSettings villageSettings
    ) {
        int spawnedCount = 0;
        for (int index = 0; index < villageSettings.timeKeeperCount(); index++) {
            BlockPos spawnPos = getTimeKeeperSpawnPosition(villageCenter, index, villageSettings.timeKeeperCount());
            TimeKeeperEntity timeKeeper = ModEntities.TIME_KEEPER.get().create(level, net.minecraft.world.entity.EntitySpawnReason.STRUCTURE);
            if (timeKeeper != null) {
                // In 1.21.5, use setPos + setYRot/setXRot instead of moveTo
                timeKeeper.setPos(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5
                );
                timeKeeper.setYRot(level.random.nextFloat() * 360.0F);
                timeKeeper.setXRot(0.0F);
                timeKeeper.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(spawnPos),
                    EntitySpawnReason.STRUCTURE,
                    null
                );
                timeKeeper.setPersistenceRequired();  // Prevent despawning
                level.addFreshEntity(timeKeeper);
                spawnedCount++;

                ChronoDawn.LOGGER.debug("Spawned Time Keeper at {}", spawnPos);
            }
        }

        ChronoDawn.LOGGER.debug("Spawned {} Time Keepers at Time Keeper Village", spawnedCount);
    }

    private static BlockPos getTimeKeeperSpawnPosition(BlockPos center, int index, int count) {
        if (count == 2) return center.offset(index == 0 ? -2 : 2, 1, 0);
        int columns = (int) Math.ceil(Math.sqrt(count));
        int row = index / columns;
        int column = index % columns;
        return center.offset((column * 2) - (columns - 1), 1, (row * 2) - (columns - 1));
    }

    /**
     * Set loot tables for all chests in the placed structure.
     *
     * @param level ServerLevel
     * @param placementPos Structure placement origin
     * @param templateSize Structure size (from template)
     */
    private static void setChestLootTables(
        ServerLevel level, BlockPos placementPos, net.minecraft.core.Vec3i templateSize, TimeKeeperVillageSettings villageSettings
    ) {
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
                            chestBlockEntity.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, CompatResourceLocation.parse(villageSettings.lootTableId())), level.random.nextLong());
                            chestsFound++;
                            ChronoDawn.LOGGER.debug("Set loot table for chest at {}", checkPos);
                        }
                    }
                }
            }
        }

        if (chestsFound > 0) {
            ChronoDawn.LOGGER.debug("Set loot tables for {} chest(s) in Time Keeper Village", chestsFound);
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

                    // Fill with temporal dirt
                    level.setBlock(fillPos, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState(), 2);
                    filledBlocks++;
                }
            }
        }

        if (filledBlocks > 0) {
            ChronoDawn.LOGGER.debug("Filled {} foundation blocks below Time Keeper Village", filledBlocks);
        }
    }
}
