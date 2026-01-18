package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.BossSpawnData;
import com.chronodawn.compat.CompatSavedData;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Time Guardian Spawner
 *
 * Manages spawning of Time Guardian entities in Desert Clock Tower structures.
 *
 * Spawn Conditions:
 * - Location: Desert Clock Tower top floor (ChronoDawn dimension)
 * - Frequency: One per Desert Clock Tower structure
 * - Persistence: Uses SavedData to prevent duplicate spawning after server restart
 *
 * Implementation Strategy:
 * - Uses server tick event to periodically check for newly generated Desert Clock Towers
 * - Spawns Time Guardian at the top floor of the structure
 * - Uses SavedData to persist spawn state across server restarts
 * - Tracks spawned structures to avoid duplicate spawning
 *
 * Reference: data-model.md (Boss Spawning - Time Guardian)
 * Task: T114 [US2] Create Time Guardian spawn logic (spawns on Desert Clock Tower top floor)
 */
public class TimeGuardianSpawner {
    private static final ResourceLocation DESERT_CLOCK_TOWER_ID = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "desert_clock_tower"
    );

    // Check interval (in ticks) - check every 5 seconds
    private static final int CHECK_INTERVAL = 100;
    // T430: Use per-dimension tick counters to prevent cross-dimension interference
    private static final Map<ResourceLocation, AtomicInteger> tickCounters = new ConcurrentHashMap<>();

    /**
     * Initialize Time Guardian spawning system.
     * Register event listeners for structure generation.
     */
    public static void register() {
        // Use server tick event to check for Desert Clock Tower structures
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
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

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Get saved data for this world (persists across server restarts)
        BossSpawnData data = CompatSavedData.computeIfAbsent(
            level.getDataStorage(),
            BossSpawnData::new,
            BossSpawnData::load,
            BossSpawnData.getDataName()
        );

        // Check all players for nearby Desert Clock Tower structures
        var structureManager = level.structureManager();

        for (var player : level.players()) {
            BlockPos playerPos = player.blockPosition();
            ChunkPos playerChunkPos = new ChunkPos(playerPos);

            // Check chunks in a 8-chunk radius around player
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                    // Find structures matching Desert Clock Tower ID
                    var structures = structureManager.startsForStructure(
                        chunkPos,
                        structure -> {
                            var location = level.registryAccess()
                                .registry(net.minecraft.core.registries.Registries.STRUCTURE).orElseThrow()
                                .getKey(structure);
                            return location != null && location.equals(DESERT_CLOCK_TOWER_ID);
                        }
                    );

                    // Process each Desert Clock Tower structure
                    for (StructureStart structureStart : structures) {
                        // Use structure bounding box instead of expensive block scanning
                        var boundingBox = structureStart.getBoundingBox();
                        BlockPos structureCenter = new BlockPos(
                            (boundingBox.minX() + boundingBox.maxX()) / 2,
                            (boundingBox.minY() + boundingBox.maxY()) / 2,
                            (boundingBox.minZ() + boundingBox.maxZ()) / 2
                        );

                        // Skip if already spawned (check persisted data)
                        if (data.hasTimeGuardianStructureSpawned(structureCenter)) {
                            continue;
                        }

                        // Check if player is within reasonable distance (64 blocks)
                        double distanceSq = playerPos.distSqr(structureCenter);
                        if (distanceSq > 64 * 64) {
                            continue;
                        }

                        double distance = Math.sqrt(distanceSq);
                        ChronoDawn.LOGGER.info("Found Desert Clock Tower near player at {} (distance: {} blocks) - attempting spawn",
                            structureCenter, distance);

                        // Spawn at 5th floor (approximately 80% of structure height from bottom)
                        // This ensures spawning inside the tower, not on the rooftop
                        int structureHeight = boundingBox.maxY() - boundingBox.minY();
                        BlockPos spawnPos = new BlockPos(
                            structureCenter.getX(),
                            boundingBox.minY() + (int)(structureHeight * 0.8),
                            structureCenter.getZ()
                        );

                        if (spawnTimeGuardian(level, spawnPos)) {
                            data.markTimeGuardianStructureSpawned(structureCenter);
                            ChronoDawn.LOGGER.info("Successfully spawned Time Guardian at {}", spawnPos);
                        }
                    }
                }
            }
        }
    }

    /**
     * Spawn a Time Guardian at the given position.
     *
     * @param level The ServerLevel to spawn in
     * @param spawnPos The position to spawn at
     * @return true if spawn was successful, false otherwise
     */
    private static boolean spawnTimeGuardian(ServerLevel level, BlockPos spawnPos) {
        // Check if a Time Guardian already exists near this position
        if (isGuardianNearby(level, spawnPos)) {
            return true; // Already has guardian (success state)
        }

        // Find a valid spawn position (air block with solid ground)
        BlockPos validPos = findValidSpawnPosition(level, spawnPos);
        if (validPos == null) {
            ChronoDawn.LOGGER.warn("Could not find valid spawn position near {}", spawnPos);
            return false;
        }

        // Create and spawn Time Guardian
        TimeGuardianEntity guardian = ModEntities.TIME_GUARDIAN.get().create(level);
        if (guardian != null) {
            guardian.moveTo(
                validPos.getX() + 0.5,
                validPos.getY(),
                validPos.getZ() + 0.5,
                0.0f, 0.0f
            );

            guardian.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(validPos),
                EntitySpawnReason.STRUCTURE,
                null
            );

            level.addFreshEntity(guardian);

            ChronoDawn.LOGGER.info("Successfully spawned Time Guardian at {}", validPos);
            return true;
        } else {
            ChronoDawn.LOGGER.error("Failed to create Time Guardian entity at {}", validPos);
            return false;
        }
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
                return checkPos;
            }
        }

        // If vertical search failed, try horizontal search in a 5x5 area
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                // Skip center (already checked in vertical search)
                if (dx == 0 && dz == 0) continue;

                BlockPos horizontalPos = startPos.offset(dx, 0, dz);

                // Search downward from each horizontal position
                for (int dy = 0; dy < 15; dy++) {
                    BlockPos checkPos = horizontalPos.below(dy);

                    if (isValidSpawnLocation(level, checkPos, entityHeight)) {
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
     * Reset spawn tracking for a specific world (useful for testing or debugging).
     * T430: Now properly resets dimension-specific tick counter.
     *
     * @param level The ServerLevel to reset spawn data for
     */
    public static void reset(ServerLevel level) {
        BossSpawnData data = CompatSavedData.computeIfAbsent(
            level.getDataStorage(),
            BossSpawnData::new,
            BossSpawnData::load,
            BossSpawnData.getDataName()
        );
        data.resetTimeGuardian();

        ResourceLocation dimensionId = level.dimension().location();
        tickCounters.remove(dimensionId);
        ChronoDawn.LOGGER.info("Time Guardian Spawner reset for dimension: {}", dimensionId);
    }
}
