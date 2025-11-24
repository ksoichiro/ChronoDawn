package com.chronosphere.worldgen.spawning;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.ClockworkColossusEntity;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clockwork Colossus Spawner
 *
 * Manages spawning of Clockwork Colossus entity in Clockwork Depths structure.
 *
 * Spawn Conditions:
 * - Location: Clockwork Depths Engine Room (Chronosphere dimension)
 * - Trigger: Player enters within 20 blocks of the spawn marker
 * - Max per structure: 1
 *
 * Implementation Strategy:
 * - Uses server tick event to check for Clockwork Depths structures
 * - Searches for Structure Void marker block in engine room
 * - Spawns Clockwork Colossus when player approaches marker
 * - Spawn position: 3 blocks above marker, 7 blocks away from center
 *
 * NBT Setup Required:
 * - Place multiple Clockwork Blocks (chronosphere:clockwork_block) at desired spawn positions
 * - The boss will spawn on top of the selected Clockwork Block (Y+1)
 * - Recommended: 3-5 Clockwork Blocks around the room
 * - Clockwork Blocks are visible and thematic, fitting the structure's aesthetic
 *
 * Reference: T235 - Clockwork Colossus implementation
 */
public class ClockworkColossusSpawner {
    private static final ResourceLocation CLOCKWORK_DEPTHS_ID = ResourceLocation.fromNamespaceAndPath(
        Chronosphere.MOD_ID,
        "clockwork_depths"
    );

    // Track structure positions where we've already spawned Clockwork Colossus (per dimension)
    private static final Map<ResourceLocation, Set<BlockPos>> spawnedStructures = new HashMap<>();

    // Check interval (in ticks) - check every 2 seconds
    private static final int CHECK_INTERVAL = 40;
    private static final Map<ResourceLocation, Integer> tickCounters = new HashMap<>();

    // Distance threshold for player proximity spawning
    private static final double SPAWN_DISTANCE = 20.0;

    /**
     * Initialize Clockwork Colossus spawning system.
     * Register event listeners for structure generation and player proximity.
     */
    public static void register() {
        // Use server tick event to check for Clockwork Depths structures
        TickEvent.SERVER_POST.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                checkAndSpawnColossus(level);
            }
        });

        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
                Chronosphere.LOGGER.info("Clockwork Colossus Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        Chronosphere.LOGGER.info("Registered ClockworkColossusSpawner");
    }

    /**
     * Check for Clockwork Depths structures and spawn Clockwork Colossus if player is nearby.
     * This is called every server tick.
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndSpawnColossus(ServerLevel level) {
        ResourceLocation dimensionId = level.dimension().location();

        // Initialize tracking for this dimension if needed
        spawnedStructures.putIfAbsent(dimensionId, new HashSet<>());
        tickCounters.putIfAbsent(dimensionId, 0);

        // Increment tick counter for this dimension
        int currentTick = tickCounters.get(dimensionId);
        currentTick++;

        // Only check every CHECK_INTERVAL ticks
        if (currentTick < CHECK_INTERVAL) {
            tickCounters.put(dimensionId, currentTick);
            return;
        }
        tickCounters.put(dimensionId, 0);

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Check chunks around each player
        for (var player : level.players()) {
            ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

            // Check chunks in a 12-chunk radius around player
            for (int x = -12; x <= 12; x++) {
                for (int z = -12; z <= 12; z++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                    // Check if this chunk contains a Clockwork Depths structure
                    if (hasClockworkDepths(level, chunkPos)) {
                        BlockPos structurePos = chunkPos.getWorldPosition();

                        // Skip if we've already processed this structure in this dimension
                        Set<BlockPos> dimensionSpawned = spawnedStructures.get(dimensionId);
                        if (dimensionSpawned.contains(structurePos)) {
                            continue;
                        }

                        // Find all spawn markers in engine room
                        List<BlockPos> markerPositions = findEngineRoomMarkers(level, chunkPos);

                        if (markerPositions.isEmpty()) {
                            // Markers not found yet, but don't mark as processed
                            // (structure might still be generating)
                            continue;
                        }

                        // Check if any player is close enough to trigger spawn
                        boolean playerNearby = false;
                        for (BlockPos marker : markerPositions) {
                            if (isPlayerNearby(level, marker, SPAWN_DISTANCE)) {
                                playerNearby = true;
                                break;
                            }
                        }

                        if (!playerNearby) {
                            continue;
                        }

                        // Mark this structure as processed in this dimension
                        dimensionSpawned.add(structurePos);

                        // Spawn Clockwork Colossus at engine room (random marker position)
                        spawnClockworkColossusAtEngineRoom(level, markerPositions);
                    }
                }
            }
        }
    }

    /**
     * Check if a chunk contains a Clockwork Depths structure.
     *
     * @param level The ServerLevel to check
     * @param chunkPos The chunk position to check
     * @return true if the chunk contains a Clockwork Depths
     */
    private static boolean hasClockworkDepths(ServerLevel level, ChunkPos chunkPos) {
        var structureManager = level.structureManager();

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            Structure structure = entry.getKey();

            var structureLocation = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(CLOCKWORK_DEPTHS_ID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Find all engine room spawn markers (Clockwork Blocks) in Clockwork Depths.
     *
     * The NBT should contain multiple Clockwork Blocks at desired spawn positions.
     *
     * @param level The ServerLevel to search in
     * @param chunkPos The chunk position containing the structure
     * @return List of marker positions (empty if none found)
     */
    private static List<BlockPos> findEngineRoomMarkers(ServerLevel level, ChunkPos chunkPos) {
        List<BlockPos> markers = new ArrayList<>();
        BlockPos searchCenter = chunkPos.getWorldPosition();
        int searchRadius = 100;
        int maxY = 100;
        int minY = -60;

        // Search for all Clockwork Block marker blocks
        for (int y = maxY; y >= minY; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = new BlockPos(searchCenter.getX() + x, y, searchCenter.getZ() + z);
                    var blockState = level.getBlockState(checkPos);

                    // Check if this is a Clockwork Block (our marker)
                    if (blockState.is(ModBlocks.CLOCKWORK_BLOCK.get())) {
                        markers.add(checkPos.immutable());
                    }
                }
            }
        }

        return markers;
    }

    /**
     * Check if any player is within the specified distance of the given position.
     *
     * @param level The ServerLevel to check
     * @param pos The position to check around
     * @param distance The distance threshold
     * @return true if a player is nearby
     */
    private static boolean isPlayerNearby(ServerLevel level, BlockPos pos, double distance) {
        for (var player : level.players()) {
            if (player.blockPosition().distSqr(pos) <= distance * distance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Spawn a Clockwork Colossus at the Engine Room.
     * Randomly selects one of the marker positions and spawns on top of it (Y+1).
     *
     * @param level The ServerLevel to spawn in
     * @param markerPositions List of spawn marker positions (Clockwork Block positions)
     */
    private static void spawnClockworkColossusAtEngineRoom(ServerLevel level, List<BlockPos> markerPositions) {
        // Randomly select one marker position
        BlockPos selectedMarker = markerPositions.get(level.random.nextInt(markerPositions.size()));

        // Check if a Clockwork Colossus already exists near this position
        if (isColossusNearby(level, selectedMarker)) {
            return;
        }

        // Spawn on top of the Clockwork Block (Y+1)
        BlockPos spawnPos = selectedMarker.above();

        // Verify the spawn position is valid (should be air above the Clockwork Block)
        if (!level.getBlockState(spawnPos).isAir()) {
            // Try to find a valid position nearby
            spawnPos = findValidSpawnPosition(level, spawnPos);
        }

        // Create and spawn Clockwork Colossus
        ClockworkColossusEntity colossus = ModEntities.CLOCKWORK_COLOSSUS.get().create(level);
        if (colossus != null) {
            colossus.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                level.random.nextFloat() * 360.0f, // Random rotation
                0.0f
            );

            colossus.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.STRUCTURE,
                null
            );

            level.addFreshEntity(colossus);

            Chronosphere.LOGGER.info(
                "Clockwork Colossus spawned at [{}, {}, {}]",
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()
            );
        } else {
            Chronosphere.LOGGER.error("Failed to create Clockwork Colossus entity");
        }
    }

    /**
     * Check if a Clockwork Colossus already exists near the given position.
     *
     * @param level The ServerLevel to check
     * @param pos The position to check around
     * @return true if a Clockwork Colossus is nearby
     */
    private static boolean isColossusNearby(ServerLevel level, BlockPos pos) {
        double searchRadius = 50.0;
        var nearbyColossuses = level.getEntitiesOfClass(
            ClockworkColossusEntity.class,
            new net.minecraft.world.phys.AABB(pos).inflate(searchRadius)
        );
        return !nearbyColossuses.isEmpty();
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
        tickCounters.clear();
    }
}
