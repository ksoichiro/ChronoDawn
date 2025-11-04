package com.chronosphere.worldgen.spawning;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.HashSet;
import java.util.Set;

/**
 * Time Guardian Spawner
 *
 * Manages spawning of Time Guardian entities in Desert Clock Tower structures.
 *
 * Spawn Conditions (from data-model.md):
 * - Location: Desert Clock Tower top floor (Chronosphere dimension)
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
        Chronosphere.MOD_ID,
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
    private static int tickCounter = 0;

    // Track world dimension to reset counters when changing worlds
    private static net.minecraft.resources.ResourceLocation lastWorldId = null;

    /**
     * Initialize Time Guardian spawning system.
     * Register event listeners for structure generation.
     */
    public static void register() {
        // Use server tick event to check for Desert Clock Tower structures
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
                Chronosphere.LOGGER.info("Time Guardian Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        Chronosphere.LOGGER.info("Registered TimeGuardianSpawner");
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

        // Increment tick counter
        tickCounter++;

        // Only check every CHECK_INTERVAL ticks
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Check if we've reached the spawn limit
        if (spawnedGuardiansCount >= MAX_TIME_GUARDIANS_PER_WORLD) {
            return;
        }

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Check chunks around the first player only (to avoid duplicate processing)
        var player = level.players().get(0);
        ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

        // Check chunks in a 5-chunk radius around player
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                // Check if this chunk contains a Desert Clock Tower structure
                if (hasDesertClockTower(level, chunkPos)) {
                    BlockPos structurePos = chunkPos.getWorldPosition();

                    // Skip if we've already processed this structure
                    if (spawnedStructures.contains(structurePos)) {
                        continue;
                    }

                    // Mark this structure as processed
                    spawnedStructures.add(structurePos);

                    // Spawn Time Guardian at the top of the structure
                    spawnTimeGuardianAtTower(level, chunkPos);

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

        // Check all structures in this chunk
        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            Structure structure = entry.getKey();

            // Get structure's resource location
            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(DESERT_CLOCK_TOWER_ID)) {
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
     */
    private static void spawnTimeGuardianAtTower(ServerLevel level, ChunkPos chunkPos) {
        // Get the structure start
        var structureManager = level.structureManager();
        BlockPos chunkBlockPos = chunkPos.getWorldPosition();

        // Try to get structure at this position
        for (var entry : structureManager.getAllStructuresAt(chunkBlockPos).entrySet()) {
            Structure structure = entry.getKey();

            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(DESERT_CLOCK_TOWER_ID)) {
                Chronosphere.LOGGER.info("Attempting to spawn Time Guardian at Desert Clock Tower in chunk {}", chunkPos);

                // Find the structure's bounds by searching for stone bricks, andesite, and white glass
                BlockPos towerSpawnPos = findTowerTopFloor(level, chunkBlockPos);

                if (towerSpawnPos == null) {
                    Chronosphere.LOGGER.warn("Could not find Desert Clock Tower top floor at chunk {}", chunkPos);
                    return;
                }

                Chronosphere.LOGGER.info("Calculated spawn position: {}", towerSpawnPos);

                // Check if a Time Guardian already exists near this position
                if (isGuardianNearby(level, towerSpawnPos)) {
                    Chronosphere.LOGGER.debug("Time Guardian already exists near {}", towerSpawnPos);
                    return;
                }

                // Find a valid spawn position (air block with solid ground)
                BlockPos spawnPos = findValidSpawnPosition(level, towerSpawnPos);

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
                        null
                    );

                    level.addFreshEntity(guardian);

                    // Increment counter
                    spawnedGuardiansCount++;

                    Chronosphere.LOGGER.info(
                        "Time Guardian spawned at [{}, {}, {}] (Total: {}/{})",
                        spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                        spawnedGuardiansCount, MAX_TIME_GUARDIANS_PER_WORLD
                    );
                }

                // IMPORTANT: Only spawn once per structure
                // Exit immediately after processing this structure
                return;
            }
        }
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
            // Spawn 10 blocks below the highest structure point, offset from center
            BlockPos spawnPos = new BlockPos(
                bestPosition.getX() + 2,
                highestY - 10,
                bestPosition.getZ() + 2
            );
            Chronosphere.LOGGER.info("Found tower top floor at {} (block count: {})", spawnPos, maxBlockCount);
            return spawnPos;
        }

        // Fallback: use the search center at a reasonable height
        Chronosphere.LOGGER.warn("Could not find Desert Clock Tower structure, using fallback position");
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
     * Looks for an air block with solid ground beneath.
     *
     * @param level The ServerLevel to check
     * @param startPos The starting position to search from
     * @return A valid spawn position
     */
    private static BlockPos findValidSpawnPosition(ServerLevel level, BlockPos startPos) {
        // Search downward up to 10 blocks
        for (int i = 0; i < 10; i++) {
            BlockPos checkPos = startPos.below(i);

            // Check if this is a valid spawn position (air with solid ground)
            if (level.getBlockState(checkPos).isAir() &&
                !level.getBlockState(checkPos.below()).isAir()) {
                return checkPos;
            }
        }

        // If no valid position found, return original position
        return startPos;
    }

    /**
     * Reset spawn tracking (useful for testing or world reset).
     */
    public static void reset() {
        spawnedStructures.clear();
        spawnedGuardiansCount = 0;
        tickCounter = 0;
        lastWorldId = null;
        Chronosphere.LOGGER.info("Time Guardian Spawner reset");
    }
}
