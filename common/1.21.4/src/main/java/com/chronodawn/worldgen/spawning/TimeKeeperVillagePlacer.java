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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
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

    // Tallest vanilla plant is bamboo, which can grow 16 blocks high.
    private static final int MAX_PLANT_CLEAR_HEIGHT = 32;

    // Must match the depth used when filling the village foundation.
    private static final int MAX_FOUNDATION_DEPTH = 10;

    // Covers plants affected by shape updates from blocks at the structure boundary.
    private static final int VEGETATION_CLEAR_MARGIN = 1;

    // The deferred search tries a few loaded candidates per tick and spends this many ticks on each
    // distance range before moving to the next one.
    private static final int ATTEMPTS_PER_TICK = 10;
    private static final int TICKS_PER_SEARCH_PHASE = 60;

    /** A village search waiting for chunks around the arrival point to load. Server thread only. */
    private static final class PendingPlacement {
        final ServerLevel level;
        final BlockPos entryPos;
        int ticks;

        PendingPlacement(ServerLevel level, BlockPos entryPos) {
            this.level = level;
            this.entryPos = entryPos;
        }
    }

    private static PendingPlacement pendingPlacement;

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
        // Searching here would load and generate candidate chunks on the server thread inside the
        // arrival tick, which stalled a fresh world for about two seconds. The search runs over the
        // following ticks instead (tickPendingPlacement) and uses only chunks that are already loaded.
        if (pendingPlacement == null || pendingPlacement.level != level) {
            pendingPlacement = new PendingPlacement(level, player.blockPosition());
        }
    }

    /**
     * Continue a Time Keeper Village search started by {@link #onPlayerEnterChronoDawn}.
     * Called once per server tick. Each tick tries a few candidates whose chunks are all loaded, so no
     * chunk is generated on the server thread. When every distance range is exhausted without a
     * loaded candidate, the original search that loads chunks synchronously runs as a fallback.
     *
     * @param server Running server
     */
    public static void tickPendingPlacement(net.minecraft.server.MinecraftServer server) {
        PendingPlacement pending = pendingPlacement;
        if (pending == null) return;

        ServerLevel level = server.getLevel(ModDimensions.CHRONO_DAWN_DIMENSION);
        TimeKeeperVillageSettings settings = ChronoDawnConfig.get().world().timeKeeperVillage();
        // A different level means the server was restarted in the same JVM (integrated server).
        if (level != pending.level || !settings.enabled() || TimeKeeperVillageData.get(level).isPlaced()) {
            pendingPlacement = null;
            return;
        }
        var templateOptional = level.getStructureManager().get(CompatResourceLocation.parse(settings.templateId()));
        if (templateOptional.isEmpty()) {
            pendingPlacement = null;
            return;
        }
        StructureTemplate template = templateOptional.get();
        int width = template.getSize().getX();
        int depth = template.getSize().getZ();

        int[] range = searchRange(settings, pending.ticks / TICKS_PER_SEARCH_PHASE);
        BlockPos villagePos = null;
        if (range != null) {
            villagePos = searchInRange(level, pending.entryPos, range[0], range[1], width, depth,
                ATTEMPTS_PER_TICK, true);
            if (villagePos == null) {
                pending.ticks++;
                return;
            }
        }
        pendingPlacement = null;
        if (villagePos == null) {
            ChronoDawn.LOGGER.debug("No loaded candidate for the Time Keeper Village, searching with chunk loading");
            villagePos = findSuitablePosition(level, pending.entryPos, width, depth, settings);
        }

        if (villagePos != null) {
            boolean success = placeVillage(level, villagePos, template, settings);
            if (success) {
                TimeKeeperVillageData.get(level).setPlaced(villagePos);
                ChronoDawn.LOGGER.debug("Successfully placed Time Keeper Village at {} after {} ticks",
                    villagePos, pending.ticks);
            } else {
                ChronoDawn.LOGGER.warn("Failed to place Time Keeper Village structure at {}", villagePos);
            }
        } else {
            ChronoDawn.LOGGER.warn("Could not find suitable position for Time Keeper Village near player entry point {}",
                pending.entryPos);
        }
    }

    /**
     * Distance range of a search phase, or null after the last one.
     * Phases are the same ranges findSuitablePosition walks through.
     */
    private static int[] searchRange(TimeKeeperVillageSettings settings, int phase) {
        int minDistance = settings.preferredMinDistance();
        int maxDistance = settings.preferredMaxDistance();
        for (int i = 0; i < phase; i++) {
            if (maxDistance >= settings.maxDistance()) return null;
            minDistance = maxDistance;
            maxDistance = Math.min(settings.maxDistance(), maxDistance * 2);
        }
        return new int[] {minDistance, maxDistance};
    }

    /** Whether every chunk that the village footprint and its margin touch is fully loaded. */
    private static boolean isFootprintLoaded(ServerLevel level, int x, int z, int width, int depth) {
        int minChunkX = (x - width / 2 - VEGETATION_CLEAR_MARGIN) >> 4;
        int maxChunkX = (x + width / 2 + VEGETATION_CLEAR_MARGIN) >> 4;
        int minChunkZ = (z - depth / 2 - VEGETATION_CLEAR_MARGIN) >> 4;
        int maxChunkZ = (z + depth / 2 + VEGETATION_CLEAR_MARGIN) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
                    return false;
                }
            }
        }
        return true;
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
            BlockPos result = searchInRange(level, center, minDistance, maxDistance, structureWidth, structureDepth,
                MAX_ATTEMPTS, false);
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
        ServerLevel level, BlockPos center, int minDistance, int maxDistance, int structureWidth, int structureDepth,
        int maxAttempts, boolean loadedChunksOnly
    ) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int range = maxDistance - minDistance;
            int dx = level.random.nextInt(range + 1) + minDistance;
            int dz = level.random.nextInt(range + 1) + minDistance;
            if (level.random.nextBoolean()) dx = -dx;
            if (level.random.nextBoolean()) dz = -dz;
            int x = center.getX() + dx;
            int z = center.getZ() + dz;
            if (loadedChunksOnly) {
                // Never generate chunks here: skip candidates whose chunks are not ready yet.
                if (!isFootprintLoaded(level, x, z, structureWidth, structureDepth)) continue;
            } else {
                level.getChunk(x >> 4, z >> 4);
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            if (y <= level.getMinY()) continue;
            BlockPos candidate = new BlockPos(x, y, z);
            if (isTerrainFlat(level, candidate, structureWidth, structureDepth, MAX_HEIGHT_VARIATION)
                && isSurfaceSuitable(level, candidate, structureWidth, structureDepth)
                && !overlapsExistingStructure(level, candidate, structureWidth, structureDepth)) {
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

            // Avoid man-made surfaces such as the flat roof of an existing structure
            if (isManMadeSurface(surfaceBlock)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Whether the block is a building material that does not occur as natural ground.
     * Backs up the structure bounding box check for structures that have no tracked StructureStart.
     */
    private static boolean isManMadeSurface(BlockState state) {
        return state.is(BlockTags.PLANKS)
            || state.is(BlockTags.LOGS)
            || state.is(BlockTags.STAIRS)
            || state.is(BlockTags.SLABS)
            || state.is(BlockTags.WALLS)
            || state.is(BlockTags.FENCES)
            || state.is(BlockTags.WOOL)
            || state.is(BlockTags.STONE_BRICKS);
    }

    /**
     * Check if the village footprint (plus the vegetation clear margin) overlaps the horizontal
     * bounding box of any existing structure.
     *
     * Only X/Z is compared: the village is placed on the surface, and a roof must be rejected
     * even though its height differs from the structure origin. startsForStructure also returns
     * starts that merely reference the chunk, so large structures spanning chunks are covered.
     */
    private static boolean overlapsExistingStructure(ServerLevel level, BlockPos center, int width, int depth) {
        int minX = center.getX() - width / 2 - VEGETATION_CLEAR_MARGIN;
        int maxX = center.getX() + width / 2 + VEGETATION_CLEAR_MARGIN;
        int minZ = center.getZ() - depth / 2 - VEGETATION_CLEAR_MARGIN;
        int maxZ = center.getZ() + depth / 2 + VEGETATION_CLEAR_MARGIN;

        for (int chunkX = minX >> 4; chunkX <= (maxX >> 4); chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= (maxZ >> 4); chunkZ++) {
                var starts = level.structureManager()
                    .startsForStructure(new net.minecraft.world.level.ChunkPos(chunkX, chunkZ), structure -> true);
                for (var start : starts) {
                    if (start.isValid() && start.getBoundingBox().intersects(minX, minZ, maxX, maxZ)) {
                        return true;
                    }
                }
            }
        }
        return false;
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

        // Clear plants before changing their supporting blocks so they do not break into dropped items.
        clearVegetation(level, placementPos, templateSize.getX(), templateSize.getZ());

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

    private static void clearVegetation(ServerLevel level, BlockPos placementPos, int width, int depth) {
        for (int dx = -VEGETATION_CLEAR_MARGIN; dx < width + VEGETATION_CLEAR_MARGIN; dx++) {
            for (int dz = -VEGETATION_CLEAR_MARGIN; dz < depth + VEGETATION_CLEAR_MARGIN; dz++) {
                for (int dy = -MAX_FOUNDATION_DEPTH; dy < MAX_PLANT_CLEAR_HEIGHT; dy++) {
                    BlockPos plantPos = placementPos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(plantPos);
                    if (isVegetation(state)) {
                        // UPDATE_KNOWN_SHAPE avoids support updates that would drop the remaining plant blocks.
                        level.setBlock(plantPos, Blocks.AIR.defaultBlockState(), 2 | 16);
                    }
                }
            }
        }
    }

    private static boolean isVegetation(BlockState state) {
        return state.is(BlockTags.REPLACEABLE_BY_TREES)
            || state.is(BlockTags.FLOWERS)
            || state.getBlock() instanceof BushBlock
            || state.getBlock() instanceof CropBlock
            || state.getBlock() instanceof DoublePlantBlock;
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

        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                int x = placementPos.getX() + dx;
                int z = placementPos.getZ() + dz;
                int surfaceY = placementPos.getY();

                // Fill downward from structure base until hitting solid ground
                for (int dy = 1; dy <= MAX_FOUNDATION_DEPTH; dy++) {
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
