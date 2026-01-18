package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Time Guardian Spawner
 *
 * Manages spawning of Time Guardian entities in Desert Clock Tower structures.
 *
 * Spawn Conditions (from data-model.md):
 * - Location: Desert Clock Tower top floor (ChronoDawn dimension)
 * - Frequency: Spawns in Desert Clock Tower structures
 * - Max per world: 3 (based on max number of Time Guardians)
 *
 * Implementation Strategy:
 * - Uses server tick event to periodically check for newly generated Desert Clock Towers
 * - Spawns Time Guardian at the top floor of the structure
 * - Tracks spawned structures to avoid duplicate spawning
 * - Respects max spawn limit per world
 *
 * Reference: data-model.md (Boss Spawning - Time Guardian)
 * Task: T114 [US2] Create Time Guardian spawn logic (spawns on Desert Clock Tower top floor)
 */
public class TimeGuardianSpawner {
    private static final ResourceLocation DESERT_CLOCK_TOWER_ID = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "desert_clock_tower"
    );

    // Track structure positions where we've already attempted to spawn Time Guardians
    // Use BlockPos instead of ChunkPos for more precise tracking
    private static final Set<BlockPos> spawnedStructures = new HashSet<>();

    // Maximum Time Guardians per world (from data-model.md)
    private static final int MAX_TIME_GUARDIANS_PER_WORLD = 3;

    // Counter for spawned Time Guardians in current world
    private static int spawnedGuardiansCount = 0;

    // Check interval (in ticks) - check every 5 seconds
    private static final int CHECK_INTERVAL = 100;
    // T430: Use per-dimension tick counters to prevent cross-dimension interference
    private static final Map<ResourceLocation, AtomicInteger> tickCounters = new ConcurrentHashMap<>();

    // Track world dimension to reset counters when changing worlds
    private static net.minecraft.resources.ResourceLocation lastWorldId = null;

    /**
     * Initialize Time Guardian spawning system.
     * Register event listeners for structure generation.
     */
    public static void register() {
        // Use server tick event to check for Desert Clock Tower structures
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;
                ChronoDawn.LOGGER.info("Time Guardian Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        ChronoDawn.LOGGER.info("Registered TimeGuardianSpawner");
    }

    /**
     * Check for Desert Clock Tower structures and spawn Time Guardian if needed.
     * This should be called periodically (e.g., from a tick event).
     * T430: Now uses per-dimension tick counter to prevent cross-dimension interference.
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndSpawnGuardians(ServerLevel level) {
        ResourceLocation dimensionId = level.dimension().location();

        // Initialize tick counter for this dimension if needed
        tickCounters.putIfAbsent(dimensionId, new AtomicInteger(0));
        AtomicInteger tickCounter = tickCounters.get(dimensionId);

        // Increment tick counter and check interval
        int currentTick = tickCounter.incrementAndGet();
        if (currentTick < CHECK_INTERVAL) {
            return;
        }
        tickCounter.set(0);

        // Reset tracking if we're in a different world
        ResourceLocation currentWorldId = level.dimension().location();
        if (lastWorldId == null || !lastWorldId.equals(currentWorldId)) {
            reset();
            lastWorldId = currentWorldId;
        }

        // Check if we've reached the spawn limit
        if (spawnedGuardiansCount >= MAX_TIME_GUARDIANS_PER_WORLD) {
            // Count actual Time Guardians in the world to verify
            int actualGuardianCount = level.getEntities(ModEntities.TIME_GUARDIAN.get(), entity -> true).size();

            ChronoDawn.LOGGER.info("Time Guardian spawn check skipped - counter: {}/{}, actual guardians in world: {}",
                spawnedGuardiansCount, MAX_TIME_GUARDIANS_PER_WORLD, actualGuardianCount);

            // If the counter is wrong (actual count is less), reset the counter
            if (actualGuardianCount < spawnedGuardiansCount) {
                ChronoDawn.LOGGER.warn("Counter mismatch detected! Resetting counter from {} to {}",
                    spawnedGuardiansCount, actualGuardianCount);
                spawnedGuardiansCount = actualGuardianCount;
                // Don't return - continue checking
            } else {
                return;
            }
        }

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Check chunks around the first player only (to avoid duplicate processing)
        var player = level.players().get(0);
        ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

        // Check chunks in a 16-chunk radius around player (increased from 5 to 16 chunks = 256 blocks)
        // This ensures Desert Clock Towers are detected even at moderate distances
        int checkRadius = 16;

        for (int x = -checkRadius; x <= checkRadius; x++) {
            for (int z = -checkRadius; z <= checkRadius; z++) {
                ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                // Check if this chunk contains a Desert Clock Tower structure
                if (hasDesertClockTower(level, chunkPos)) {
                    BlockPos structurePos = chunkPos.getWorldPosition();

                    // Skip if we've already successfully spawned at this structure
                    if (spawnedStructures.contains(structurePos)) {
                        continue;
                    }

                    ChronoDawn.LOGGER.info("Found Desert Clock Tower at chunk {} - attempting to spawn Time Guardian", chunkPos);

                    // Attempt to spawn Time Guardian at the top of the structure
                    boolean spawnSuccess = spawnTimeGuardianAtTower(level, chunkPos);

                    // Only mark as processed if spawn was successful
                    if (spawnSuccess) {
                        spawnedStructures.add(structurePos);
                        ChronoDawn.LOGGER.info("Successfully spawned and marked structure at {} as processed", structurePos);
                    } else {
                        ChronoDawn.LOGGER.warn("Failed to spawn Time Guardian at {} - will retry on next check", structurePos);
                    }

                    // Check if we've reached the limit
                    if (spawnedGuardiansCount >= MAX_TIME_GUARDIANS_PER_WORLD) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Check if a chunk contains a Desert Clock Tower structure.
     *
     * @param level The ServerLevel to check
     * @param chunkPos The chunk position to check
     * @return true if the chunk contains a Desert Clock Tower
     */
    private static boolean hasDesertClockTower(ServerLevel level, ChunkPos chunkPos) {
        // Get structure starts for this chunk
        var structureManager = level.structureManager();
        BlockPos worldPos = chunkPos.getWorldPosition();

        // Check all structures in this chunk
        var structures = structureManager.getAllStructuresAt(worldPos);

        for (var entry : structures.entrySet()) {
            Structure structure = entry.getKey();

            // Get structure's resource location
            var structureLocation = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(DESERT_CLOCK_TOWER_ID)) {
                ChronoDawn.LOGGER.info("Detected Desert Clock Tower at chunk {} (world pos: {})", chunkPos, worldPos);
                return true;
            }
        }

        return false;
    }

    /**
     * Spawn a Time Guardian at the top floor of the Desert Clock Tower.
     *
     * @param level The ServerLevel to spawn in
     * @param chunkPos The chunk position containing the structure
     * @return true if spawn was successful, false otherwise
     */
    private static boolean spawnTimeGuardianAtTower(ServerLevel level, ChunkPos chunkPos) {
        // Get the structure start
        var structureManager = level.structureManager();
        BlockPos chunkBlockPos = chunkPos.getWorldPosition();

        // Try to get structure at this position
        for (var entry : structureManager.getAllStructuresAt(chunkBlockPos).entrySet()) {
            Structure structure = entry.getKey();

            var structureLocation = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(DESERT_CLOCK_TOWER_ID)) {
                ChronoDawn.LOGGER.info("Attempting to spawn Time Guardian at Desert Clock Tower in chunk {}", chunkPos);

                // Find the structure's bounds by searching for stone bricks, andesite, and white glass
                BlockPos towerSpawnPos = findTowerTopFloor(level, chunkBlockPos);

                if (towerSpawnPos == null) {
                    ChronoDawn.LOGGER.warn("Could not find Desert Clock Tower top floor at chunk {}", chunkPos);
                    return false;
                }

                ChronoDawn.LOGGER.info("Calculated spawn position: {}", towerSpawnPos);

                // Check if a Time Guardian already exists near this position
                if (isGuardianNearby(level, towerSpawnPos)) {
                    ChronoDawn.LOGGER.debug("Time Guardian already exists near {}", towerSpawnPos);
                    // Return true because this structure already has a guardian (success state)
                    return true;
                }

                // Find a valid spawn position (air block with solid ground)
                BlockPos spawnPos = findValidSpawnPosition(level, towerSpawnPos);

                if (spawnPos == null) {
                    ChronoDawn.LOGGER.warn("Could not find valid spawn position near {} - skipping Time Guardian spawn", towerSpawnPos);
                    return false;
                }

                ChronoDawn.LOGGER.info("Found valid spawn position: {}", spawnPos);

                // Create and spawn Time Guardian
                TimeGuardianEntity guardian = ModEntities.TIME_GUARDIAN.get().create(level);
                if (guardian != null) {
                    guardian.moveTo(
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        0.0f, 0.0f
                    );

                    guardian.finalizeSpawn(
                        level,
                        level.getCurrentDifficultyAt(spawnPos),
                        MobSpawnType.STRUCTURE,
                        null,
                        null
                    );

                    level.addFreshEntity(guardian);

                    // Increment counter AFTER successfully adding entity
                    spawnedGuardiansCount++;

                    ChronoDawn.LOGGER.info(
                        "✓ Time Guardian spawned at [{}, {}, {}] - Counter incremented to {}/{}",
                        spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                        spawnedGuardiansCount, MAX_TIME_GUARDIANS_PER_WORLD
                    );

                    // Return true to indicate successful spawn
                    return true;
                } else {
                    ChronoDawn.LOGGER.error("Failed to create Time Guardian entity at {}", spawnPos);
                    return false;
                }
            }
        }

        // No Desert Clock Tower structure found in this chunk
        return false;
    }

    /**
     * Find the top floor spawn position of the Desert Clock Tower.
     * Searches for stone bricks, andesite, and white glass to locate the tower structure.
     *
     * @param level The ServerLevel to search in
     * @param searchCenter The center position to search around
     * @return The spawn position near the top floor, or null if not found
     */
    private static BlockPos findTowerTopFloor(ServerLevel level, BlockPos searchCenter) {
        // Desert Clock Tower uses stone bricks, andesite, and white glass
        // Search for the highest concentration of these blocks
        int searchRadius = 20;
        int maxY = level.getMaxBuildHeight();
        int minY = level.getMinBuildHeight();

        BlockPos bestPosition = null;
        int maxBlockCount = 0;
        int highestY = minY;

        // Search in horizontal plane at different heights
        for (int y = maxY; y >= minY; y -= 5) {
            for (int x = -searchRadius; x <= searchRadius; x += 3) {
                for (int z = -searchRadius; z <= searchRadius; z += 3) {
                    BlockPos checkPos = new BlockPos(searchCenter.getX() + x, y, searchCenter.getZ() + z);

                    // Count structure blocks nearby (stone bricks, andesite, white glass)
                    int blockCount = 0;
                    for (int dx = -3; dx <= 3; dx++) {
                        for (int dy = -2; dy <= 2; dy++) {
                            for (int dz = -3; dz <= 3; dz++) {
                                BlockPos nearbyPos = checkPos.offset(dx, dy, dz);
                                var blockState = level.getBlockState(nearbyPos);

                                if (blockState.is(net.minecraft.world.level.block.Blocks.STONE_BRICKS) ||
                                    blockState.is(net.minecraft.world.level.block.Blocks.ANDESITE) ||
                                    blockState.is(net.minecraft.world.level.block.Blocks.WHITE_STAINED_GLASS)) {
                                    blockCount++;
                                }
                            }
                        }
                    }

                    // Update best position if this is the highest position with many structure blocks
                    if (blockCount > maxBlockCount || (blockCount > 10 && y > highestY)) {
                        maxBlockCount = blockCount;
                        bestPosition = checkPos;
                        highestY = y;
                    }
                }
            }

            // If we found a good position, stop searching lower
            if (maxBlockCount > 20) {
                break;
            }
        }

        if (bestPosition != null && maxBlockCount > 10) {
            // Spawn 5 blocks below the highest structure point, offset from center
            // Adjusted from -10 to -5 to spawn on 5th floor instead of 4th floor
            BlockPos spawnPos = new BlockPos(
                bestPosition.getX() + 2,
                highestY - 5,
                bestPosition.getZ() + 2
            );
            ChronoDawn.LOGGER.info("Found tower top floor at {} (block count: {})", spawnPos, maxBlockCount);
            return spawnPos;
        }

        // Fallback: use the search center at a reasonable height
        ChronoDawn.LOGGER.warn("Could not find Desert Clock Tower structure, using fallback position");
        return new BlockPos(searchCenter.getX(), level.getSeaLevel() + 40, searchCenter.getZ());
    }


    /**
     * Check if a Time Guardian already exists near the given position.
     *
     * @param level The ServerLevel to check
     * @param pos The position to check around
     * @return true if a Time Guardian is nearby
     */
    private static boolean isGuardianNearby(ServerLevel level, BlockPos pos) {
        double searchRadius = 30.0; // Search within 30 blocks
        var nearbyGuardians = level.getEntitiesOfClass(
            TimeGuardianEntity.class,
            new net.minecraft.world.phys.AABB(pos).inflate(searchRadius)
        );
        return !nearbyGuardians.isEmpty();
    }

    /**
     * Find a valid spawn position near the given position.
     * Looks for an air block with solid ground beneath, avoiding chains and other non-solid blocks.
     *
     * @param level The ServerLevel to check
     * @param startPos The starting position to search from
     * @return A valid spawn position, or null if none found
     */
    private static BlockPos findValidSpawnPosition(ServerLevel level, BlockPos startPos) {
        // Time Guardian entity dimensions (approximate bounding box)
        // Height: 3 blocks (to account for full entity height)
        int entityHeight = 3;

        // First, search downward up to 15 blocks (increased from 10)
        for (int i = 0; i < 15; i++) {
            BlockPos checkPos = startPos.below(i);

            if (isValidSpawnLocation(level, checkPos, entityHeight)) {
                ChronoDawn.LOGGER.debug("Found valid spawn position {} blocks below start position", i);
                return checkPos;
            }
        }

        // If vertical search failed, try horizontal search in a 5x5 area
        ChronoDawn.LOGGER.debug("Vertical search failed, trying horizontal search");
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                // Skip center (already checked in vertical search)
                if (dx == 0 && dz == 0) continue;

                BlockPos horizontalPos = startPos.offset(dx, 0, dz);

                // Search downward from each horizontal position
                for (int dy = 0; dy < 15; dy++) {
                    BlockPos checkPos = horizontalPos.below(dy);

                    if (isValidSpawnLocation(level, checkPos, entityHeight)) {
                        ChronoDawn.LOGGER.debug("Found valid spawn position at horizontal offset ({}, {}) and {} blocks down", dx, dz, dy);
                        return checkPos;
                    }
                }
            }
        }

        // If no valid position found, return null
        ChronoDawn.LOGGER.warn("Could not find any valid spawn position near {} after exhaustive search", startPos);
        return null;
    }

    /**
     * Check if a position is valid for spawning a Time Guardian.
     * Validates that the ground is solid and all blocks where the entity will occupy are air.
     *
     * @param level The ServerLevel to check
     * @param pos The position to check
     * @param entityHeight The height of the entity
     * @return true if the position is valid for spawning
     */
    private static boolean isValidSpawnLocation(ServerLevel level, BlockPos pos, int entityHeight) {
        // Check if the ground block is solid
        if (!level.getBlockState(pos.below()).isSolid()) {
            return false;
        }

        // Check if all blocks where the entity will occupy are air (no chains, no other blocks)
        for (int y = 0; y < entityHeight; y++) {
            BlockPos entityBlockPos = pos.above(y);
            var blockState = level.getBlockState(entityBlockPos);

            // Only accept pure air blocks - this ensures no collision with chains, lanterns, etc.
            if (!blockState.isAir()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Reset spawn tracking (useful for testing or world reset).
     */
    public static void reset() {
        int previousCount = spawnedGuardiansCount;
        int previousStructures = spawnedStructures.size();

        spawnedStructures.clear();
        spawnedGuardiansCount = 0;
        tickCounters.clear();
        lastWorldId = null;

        ChronoDawn.LOGGER.info("Time Guardian Spawner reset (was tracking {} guardians, {} structures)",
            previousCount, previousStructures);
    }
}
