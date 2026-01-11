package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.ClockworkColossusEntity;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clockwork Colossus Spawner
 *
 * Manages spawning of Clockwork Colossus entity in Clockwork Depths structure.
 *
 * Spawn Conditions:
 * - Location: Clockwork Depths Engine Room (ChronoDawn dimension)
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
 * - Place multiple Clockwork Blocks (chronodawn:clockwork_block) at desired spawn positions
 * - The boss will spawn on top of the selected Clockwork Block (Y+1)
 * - Recommended: 3-5 Clockwork Blocks around the room
 * - Clockwork Blocks are visible and thematic, fitting the structure's aesthetic
 *
 * Reference: T235 - Clockwork Colossus implementation
 */
public class ClockworkColossusSpawner {
    private static final ResourceLocation CLOCKWORK_DEPTHS_ID = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "clockwork_depths"
    );

    // Track structure positions where we've already spawned Clockwork Colossus (per dimension)
    private static final Map<ResourceLocation, Set<BlockPos>> spawnedStructures = new HashMap<>();

    // Track registered engine rooms (bounding boxes from BossRoomProtectionProcessor) (per-server runtime cache)
    // Key: dimension ID, Value: Set of bounding boxes
    private static final Map<ResourceLocation, Set<net.minecraft.world.level.levelgen.structure.BoundingBox>> engineRooms = new ConcurrentHashMap<>();

    // Cache found markers to avoid repeated expensive searches (per-server runtime cache)
    // Key: dimension ID, Value: Map of structure identifier → marker positions (T430: dimension isolation)
    private static final Map<ResourceLocation, Map<BlockPos, List<BlockPos>>> cachedMarkers = new ConcurrentHashMap<>();

    // Track chunks we've already searched (to avoid re-scanning) (per-server runtime cache)
    // Key: dimension ID, Value: Map of chunk position → timestamp (T430: dimension isolation)
    private static final Map<ResourceLocation, Map<ChunkPos, Long>> searchedChunks = new ConcurrentHashMap<>();
    private static final long SEARCH_CACHE_DURATION_MS = 300000; // Cache for 5 minutes

    // Check interval (in ticks) - check every 10 seconds to reduce load
    private static final int CHECK_INTERVAL = 200;
    // T430: Tick counter remains static (not per-dimension) for backward compatibility with 1.20.1
    private static int tickCounter = 0;

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
                ChronoDawn.LOGGER.info("Clockwork Colossus Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        ChronoDawn.LOGGER.info("Registered ClockworkColossusSpawner");
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

            // Check chunks in a 6-chunk radius around player (reduced from 12 to minimize checks)
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                    // Check if this chunk contains a Clockwork Depths structure
                    if (hasClockworkDepths(level, chunkPos)) {
                        // Check if we've already searched this chunk recently
                        Long lastSearchTime = searchedChunks.get(chunkPos);
                        long currentTime = System.currentTimeMillis();

                        // Periodically clean up expired search cache
                        if (searchedChunks.size() > 100) {
                            searchedChunks.entrySet().removeIf(entry ->
                                currentTime - entry.getValue() > SEARCH_CACHE_DURATION_MS
                            );
                        }

                        List<BlockPos> markerPositions;

                        // Use cached markers if available
                        if (lastSearchTime != null && currentTime - lastSearchTime < SEARCH_CACHE_DURATION_MS) {
                            // We've searched this chunk recently - check cache
                            markerPositions = getCachedMarkersForChunk(dimensionId, chunkPos);
                            if (markerPositions == null) {
                                // No markers found in previous search - skip
                                continue;
                            }
                        } else {
                            // First time searching or cache expired - do expensive search
                            markerPositions = findEngineRoomMarkers(level, chunkPos);

                            // Cache the search result
                            searchedChunks.put(chunkPos, currentTime);

                            if (markerPositions.isEmpty()) {
                                // Markers not found - cache this fact to avoid re-searching
                                continue;
                            } else {
                                // Cache found markers (T430: dimension-aware)
                                cacheMarkers(dimensionId, markerPositions);
                            }
                        }

                        // Use the first marker (sorted by coordinates) as structure identifier
                        // This ensures same structure is identified consistently across chunks
                        BlockPos structureId = markerPositions.stream()
                            .min((a, b) -> {
                                int cmp = Integer.compare(a.getX(), b.getX());
                                if (cmp != 0) return cmp;
                                cmp = Integer.compare(a.getY(), b.getY());
                                if (cmp != 0) return cmp;
                                return Integer.compare(a.getZ(), b.getZ());
                            })
                            .orElse(markerPositions.get(0));

                        // Skip if we've already processed this structure in this dimension
                        Set<BlockPos> dimensionSpawned = spawnedStructures.get(dimensionId);
                        if (dimensionSpawned.contains(structureId)) {
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

                        // Mark this structure as processed in this dimension (using first marker as ID)
                        dimensionSpawned.add(structureId);

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
     * Find all engine room spawn markers (Signs with "DANGER!!" text) in Clockwork Depths.
     *
     * The NBT should contain multiple Crimson Signs with "DANGER!!" text at desired spawn positions.
     * Only searches within registered engine room bounding boxes.
     *
     * @param level The ServerLevel to search in
     * @param chunkPos The chunk position containing the structure
     * @return List of marker positions (empty if none found)
     */
    private static List<BlockPos> findEngineRoomMarkers(ServerLevel level, ChunkPos chunkPos) {
        long startTime = System.nanoTime();

        List<BlockPos> markers = new ArrayList<>();
        ResourceLocation dimensionId = level.dimension().location();

        // Get registered engine rooms for this dimension
        Set<net.minecraft.world.level.levelgen.structure.BoundingBox> rooms = engineRooms.get(dimensionId);
        if (rooms == null || rooms.isEmpty()) {
            // No engine rooms registered yet in this dimension
            return markers;
        }

        // Find engine rooms that intersect with this chunk
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMaxX = chunkPos.getMaxBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int chunkMaxZ = chunkPos.getMaxBlockZ();

        for (net.minecraft.world.level.levelgen.structure.BoundingBox room : rooms) {
            // Check if this room intersects with the chunk
            if (room.maxX() < chunkMinX || room.minX() > chunkMaxX ||
                room.maxZ() < chunkMinZ || room.minZ() > chunkMaxZ) {
                continue; // Room doesn't intersect this chunk
            }

            // Search for signs with "DANGER!!" text within this bounding box
            for (BlockPos pos : BlockPos.betweenClosed(room.minX(), room.minY(), room.minZ(),
                                                        room.maxX(), room.maxY(), room.maxZ())) {
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign) {
                    // Check if sign text contains "DANGER!!"
                    boolean isDangerSign = false;

                    // Get sign text from front side
                    var frontText = sign.getFrontText();
                    for (int i = 0; i < 4; i++) {
                        String lineText = frontText.getMessage(i, false).getString();
                        if (lineText.contains("DANGER!!")) {
                            isDangerSign = true;
                            break;
                        }
                    }

                    if (isDangerSign) {
                        markers.add(pos.immutable());
                    }
                }
            }
        }

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        if (durationMs > 100) {
            ChronoDawn.LOGGER.warn("Clockwork Depths marker search took {}ms at chunk {} - found {} markers",
                durationMs, chunkPos, markers.size());
        } else {
            ChronoDawn.LOGGER.debug("Clockwork Depths marker search took {}ms at chunk {} - found {} markers",
                durationMs, chunkPos, markers.size());
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
        if (markerPositions.isEmpty()) {
            ChronoDawn.LOGGER.warn("No spawn markers found. Cannot spawn Clockwork Colossus.");
            return;
        }

        // Randomly select one marker position
        BlockPos selectedMarker = markerPositions.get(level.random.nextInt(markerPositions.size()));

        // Check if a Clockwork Colossus already exists near this position
        if (isColossusNearby(level, selectedMarker)) {
            return;
        }

        // Spawn at sign position (signs are placed at ground level)
        // For wall signs, spawn at the block in front of the sign
        var signBlock = level.getBlockState(selectedMarker);
        BlockPos spawnPos = selectedMarker;

        // If it's a wall sign, get the position in front of it
        if (signBlock.getBlock() instanceof net.minecraft.world.level.block.WallSignBlock) {
            var facing = signBlock.getValue(net.minecraft.world.level.block.WallSignBlock.FACING);
            // Wall sign's facing is the direction it faces OUT from the wall
            // So we spawn in that direction (in front of the sign)
            spawnPos = selectedMarker.relative(facing);
        }

        // Find valid spawn position (air with solid ground beneath)
        var blockAtSpawn = level.getBlockState(spawnPos);
        if (!blockAtSpawn.isAir()) {
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
                null,
                null
            );

            level.addFreshEntity(colossus);

            ChronoDawn.LOGGER.info(
                "Clockwork Colossus spawned at [{}, {}, {}]",
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()
            );
        } else {
            ChronoDawn.LOGGER.error("Failed to create Clockwork Colossus entity");
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
     * Cache found markers for future lookups.
     * T430: Now dimension-aware to prevent cross-dimension cache collisions.
     *
     * @param dimensionId The dimension ID
     * @param markers List of marker positions found in a structure
     */
    private static void cacheMarkers(ResourceLocation dimensionId, List<BlockPos> markers) {
        if (markers.isEmpty()) return;

        // Initialize dimension map if needed
        cachedMarkers.putIfAbsent(dimensionId, new ConcurrentHashMap<>());
        Map<BlockPos, List<BlockPos>> dimensionCache = cachedMarkers.get(dimensionId);

        // Use first marker as key
        BlockPos key = markers.stream()
            .min((a, b) -> {
                int cmp = Integer.compare(a.getX(), b.getX());
                if (cmp != 0) return cmp;
                cmp = Integer.compare(a.getY(), b.getY());
                if (cmp != 0) return cmp;
                return Integer.compare(a.getZ(), b.getZ());
            })
            .orElse(markers.get(0));

        dimensionCache.put(key, new ArrayList<>(markers));
    }

    /**
     * Get cached markers for a chunk (if any).
     * T430: Now dimension-aware to prevent cross-dimension cache collisions.
     *
     * @param dimensionId The dimension ID
     * @param chunkPos Chunk position
     * @return List of marker positions, or null if not cached
     */
    private static List<BlockPos> getCachedMarkersForChunk(ResourceLocation dimensionId, ChunkPos chunkPos) {
        Map<BlockPos, List<BlockPos>> dimensionCache = cachedMarkers.get(dimensionId);
        if (dimensionCache == null) {
            return null;
        }

        // Check if any cached marker set has markers in this chunk
        for (List<BlockPos> markers : dimensionCache.values()) {
            for (BlockPos marker : markers) {
                int markerChunkX = marker.getX() >> 4;
                int markerChunkZ = marker.getZ() >> 4;

                if (markerChunkX == chunkPos.x && markerChunkZ == chunkPos.z) {
                    return markers;
                }
            }
        }

        return null;
    }

    /**
     * Register an engine room bounding box from BossRoomProtectionProcessor.
     * Called when a Clockwork Depths engine room is detected during worldgen.
     *
     * @param level The ServerLevel containing the engine room
     * @param boundingBox The bounding box of the engine room
     */
    public static void registerEngineRoom(ServerLevel level, net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox) {
        ResourceLocation dimensionId = level.dimension().location();
        engineRooms.putIfAbsent(dimensionId, new HashSet<>());
        engineRooms.get(dimensionId).add(boundingBox);

        ChronoDawn.LOGGER.info("Registered Clockwork Depths engine room in dimension {}: {}",
            dimensionId, boundingBox);
    }

    /**
     * Reset spawn tracking for a specific world (useful for testing or debugging).
     * T430: Now properly clears dimension-specific caches.
     *
     * @param level The ServerLevel to reset spawn data for
     */
    public static void reset(ServerLevel level) {
        ResourceLocation dimensionId = level.dimension().location();
        spawnedStructures.remove(dimensionId);
        engineRooms.remove(dimensionId);
        cachedMarkers.remove(dimensionId);
        searchedChunks.remove(dimensionId);
        tickCounter = 0;
        ChronoDawn.LOGGER.info("Clockwork Colossus Spawner reset for dimension: {}", dimensionId);
    }
}
