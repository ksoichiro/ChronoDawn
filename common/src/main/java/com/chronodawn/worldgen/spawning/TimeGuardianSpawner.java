package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

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
    private static final ResourceLocation DESERT_CLOCK_TOWER_ID = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "desert_clock_tower"
    );

    // Track structure positions where we've already attempted to spawn Time Guardians (thread-safe)
    private static final Set<BlockPos> spawnedStructures = ConcurrentHashMap.newKeySet();

    // Maximum Time Guardians per world (from data-model.md)
    private static final int MAX_TIME_GUARDIANS_PER_WORLD = 3;

    // Counter for spawned Time Guardians in current world (thread-safe)
    private static final AtomicInteger spawnedGuardiansCount = new AtomicInteger(0);

    // Check interval (in ticks) - check every 5 seconds
    private static final int CHECK_INTERVAL = 100;
    private static final AtomicInteger tickCounter = new AtomicInteger(0);

    // Track world dimension to reset counters when changing worlds
    private static volatile net.minecraft.resources.ResourceLocation lastWorldId = null;

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
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndSpawnGuardians(ServerLevel level) {
        // Reset tracking if we're in a different world
        ResourceLocation currentWorldId = level.dimension().location();
        if (lastWorldId == null || !lastWorldId.equals(currentWorldId)) {
            reset();
            lastWorldId = currentWorldId;
        }

        // Increment tick counter and check interval
        int currentTick = tickCounter.incrementAndGet();
        if (currentTick < CHECK_INTERVAL) {
            return;
        }
        tickCounter.set(0);

        // Check if we've reached the spawn limit
        int currentCount = spawnedGuardiansCount.get();
        if (currentCount >= MAX_TIME_GUARDIANS_PER_WORLD) {
            // Count actual Time Guardians in the world to verify
            int actualGuardianCount = level.getEntities(ModEntities.TIME_GUARDIAN.get(), entity -> true).size();

            // If the counter is wrong (actual count is less), reset the counter
            if (actualGuardianCount < currentCount) {
                ChronoDawn.LOGGER.warn("Counter mismatch detected! Resetting counter from {} to {}",
                    currentCount, actualGuardianCount);
                spawnedGuardiansCount.set(actualGuardianCount);
            } else {
                return;
            }
        }

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

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
                                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
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

                        // Skip if already spawned
                        if (spawnedStructures.contains(structureCenter)) {
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
                            spawnedStructures.add(structureCenter);
                            spawnedGuardiansCount.incrementAndGet();
                            ChronoDawn.LOGGER.info("Successfully spawned Time Guardian at {}", spawnPos);

                            // Check limit again
                            if (spawnedGuardiansCount.get() >= MAX_TIME_GUARDIANS_PER_WORLD) {
                                return;
                            }
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
                MobSpawnType.STRUCTURE,
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
     * Reset spawn tracking (useful for testing or world reset).
     */
    public static void reset() {
        int previousCount = spawnedGuardiansCount.get();
        int previousStructures = spawnedStructures.size();

        spawnedStructures.clear();
        spawnedGuardiansCount.set(0);
        tickCounter.set(0);
        lastWorldId = null;

        ChronoDawn.LOGGER.info("Time Guardian Spawner reset (was tracking {} guardians, {} structures)",
            previousCount, previousStructures);
    }
}
