package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.BossSpawnData;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entropy Keeper Spawner
 *
 * Manages spawning of Entropy Keeper entity in Entropy Crypt structure.
 *
 * Spawn Conditions:
 * - Location: Entropy Crypt boss chamber (ChronoDawn dimension)
 * - Frequency: One per structure
 * - Condition: Spawns when player enters boss chamber
 * - Persistence: Uses SavedData to prevent duplicate spawning after server restart
 *
 * Implementation Strategy:
 * - Detects Entropy Crypt structures via structure registry
 * - Uses Amethyst Block as marker for boss spawn position
 * - Uses SavedData to persist spawn state across server restarts
 * - Tracks spawned structures to avoid duplicate spawning
 * - Spawns Entropy Keeper when player approaches marker
 *
 * Reference: T237 - Entropy Keeper implementation (Phase 2)
 * Task: Entropy Keeper spawn logic (Entropy Crypt structure)
 */
public class EntropyKeeperSpawner {
    private static final ResourceLocation ENTROPY_CRYPT_ID = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "entropy_crypt"
    );

    // Cache found markers to avoid repeated expensive searches (per-server runtime cache)
    // Key: dimension ID, Value: Map of structure identifier → marker position (T430: dimension isolation)
    private static final Map<ResourceLocation, Map<BlockPos, BlockPos>> cachedMarkers = new ConcurrentHashMap<>();

    // Track chunks we've already searched (to avoid re-scanning) (per-server runtime cache)
    // Key: dimension ID, Value: Map of chunk position → timestamp (T430: dimension isolation)
    private static final Map<ResourceLocation, Map<ChunkPos, Long>> searchedChunks = new ConcurrentHashMap<>();
    private static final long SEARCH_CACHE_DURATION_MS = 300000; // Cache for 5 minutes

    // Check interval (in ticks) - check every 2 seconds
    private static final int CHECK_INTERVAL = 40;
    private static int tickCounter = 0;

    /**
     * Initialize Entropy Keeper spawning system.
     */
    public static void register() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
                ChronoDawn.LOGGER.info("Entropy Keeper Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        // Register server tick event
        TickEvent.SERVER_POST.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                checkAndSpawnKeeper(level);
            }
        });

        ChronoDawn.LOGGER.info("Registered EntropyKeeperSpawner");
    }

    /**
     * Check for Entropy Crypt structures and spawn Entropy Keeper if player is nearby.
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndSpawnKeeper(ServerLevel level) {
        // Only process Chrono Dawn dimension
        if (!level.dimension().equals(com.chronodawn.registry.ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        // Increment tick counter and check interval
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Get saved data for this world (persists across server restarts)
        BossSpawnData data = level.getDataStorage().computeIfAbsent(
            BossSpawnData.factory(),
            BossSpawnData.getDataName()
        );

        // Check chunks around each player
        for (ServerPlayer player : level.players()) {
            ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

            // Check chunks in a 5-chunk radius around player
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunkPos.x + x, playerChunkPos.z + z);

                    // Check if this chunk contains an Entropy Crypt structure
                    if (hasEntropyCrypt(level, chunkPos)) {
                        BlockPos structurePos = chunkPos.getWorldPosition();

                        // Skip if we've already processed this structure
                        if (data.isEntropyKeeperStructureProcessed(structurePos)) {
                            continue;
                        }

                        // Mark this structure as processed
                        data.markEntropyKeeperStructureProcessed(structurePos);

                        ChronoDawn.LOGGER.info("Found Entropy Crypt structure at chunk {} (block pos: {})", chunkPos, structurePos);

                        // Check cache first, then find boss chamber marker and spawn if player is nearby
                        BlockPos markerPos = findBossChamberMarker(level, chunkPos, structurePos);

                        if (markerPos != null && !data.hasEntropyKeeperMarkerSpawned(markerPos)) {
                            // Check if player is within spawn trigger range (20 blocks)
                            double distance = player.position().distanceTo(markerPos.getCenter());

                            if (distance <= 20.0) {
                                ChronoDawn.LOGGER.info(
                                    "Player {} is near Entropy Crypt boss marker at {} (distance: {}), spawning Entropy Keeper",
                                    player.getName().getString(),
                                    markerPos,
                                    distance
                                );

                                // Spawn Entropy Keeper
                                spawnEntropyKeeper(level, markerPos);
                                data.markEntropyKeeperMarkerSpawned(markerPos);

                                // Remove marker block
                                level.setBlock(markerPos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if a chunk contains an Entropy Crypt structure.
     *
     * @param level The ServerLevel to check
     * @param chunkPos The chunk position to check
     * @return true if the chunk contains an Entropy Crypt
     */
    private static boolean hasEntropyCrypt(ServerLevel level, ChunkPos chunkPos) {
        var structureManager = level.structureManager();

        for (var entry : structureManager.getAllStructuresAt(chunkPos.getWorldPosition()).entrySet()) {
            Structure structure = entry.getKey();

            var structureLocation = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getKey(structure);

            if (structureLocation != null && structureLocation.equals(ENTROPY_CRYPT_ID)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Find boss chamber marker (Amethyst Block) in Entropy Crypt structure.
     * Uses caching to avoid repeated expensive searches.
     * T430: Now dimension-aware to prevent cross-dimension cache collisions.
     *
     * @param level The ServerLevel
     * @param chunkPos The chunk position containing the structure
     * @param structurePos The structure position
     * @return The marker position, or null if not found
     */
    private static BlockPos findBossChamberMarker(ServerLevel level, ChunkPos chunkPos, BlockPos structurePos) {
        long startTime = System.nanoTime();
        ResourceLocation dimensionId = level.dimension().location();

        // Initialize dimension-specific caches if needed
        cachedMarkers.putIfAbsent(dimensionId, new ConcurrentHashMap<>());
        searchedChunks.putIfAbsent(dimensionId, new ConcurrentHashMap<>());

        Map<BlockPos, BlockPos> dimensionMarkerCache = cachedMarkers.get(dimensionId);
        Map<ChunkPos, Long> dimensionSearchCache = searchedChunks.get(dimensionId);

        // Check if we've already found this marker (cache lookup)
        BlockPos cachedMarker = dimensionMarkerCache.get(structurePos);
        if (cachedMarker != null) {
            ChronoDawn.LOGGER.debug("Using cached marker position for structure at {}: {}", structurePos, cachedMarker);
            return cachedMarker;
        }

        // Check if we've searched this chunk recently
        Long lastSearchTime = dimensionSearchCache.get(chunkPos);
        long currentTime = System.currentTimeMillis();

        // Periodically clean up expired search cache
        if (dimensionSearchCache.size() > 100) {
            dimensionSearchCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > SEARCH_CACHE_DURATION_MS
            );
        }

        if (lastSearchTime != null && currentTime - lastSearchTime < SEARCH_CACHE_DURATION_MS) {
            // We've searched this chunk recently and found nothing
            ChronoDawn.LOGGER.debug("Chunk {} recently searched, skipping", chunkPos);
            return null;
        }

        // Search for Amethyst Block marker in a limited area around structure
        // Reduced search radius to 30 blocks (was 50) and limited Y range
        int searchRadius = 30;
        int minY = Math.max(structurePos.getY() - 20, level.getMinBuildHeight());
        int maxY = Math.min(structurePos.getY() + 20, level.getMaxBuildHeight());

        BlockPos foundMarker = null;

        for (int y = maxY; y >= minY; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = new BlockPos(structurePos.getX() + x, y, structurePos.getZ() + z);
                    BlockState state = level.getBlockState(checkPos);

                    // Check for Amethyst Block (boss spawn marker)
                    if (state.is(Blocks.AMETHYST_BLOCK)) {
                        foundMarker = checkPos.immutable();
                        break;
                    }
                }
                if (foundMarker != null) break;
            }
            if (foundMarker != null) break;
        }

        // Cache the result (dimension-specific)
        dimensionSearchCache.put(chunkPos, currentTime);
        if (foundMarker != null) {
            dimensionMarkerCache.put(structurePos, foundMarker);
        }

        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        if (durationMs > 50) {
            ChronoDawn.LOGGER.warn("Entropy Crypt marker search took {}ms at chunk {} - found: {}",
                durationMs, chunkPos, foundMarker != null);
        } else {
            ChronoDawn.LOGGER.debug("Entropy Crypt marker search took {}ms at chunk {} - found: {}",
                durationMs, chunkPos, foundMarker != null);
        }

        return foundMarker;
    }

    /**
     * Spawn Entropy Keeper at the marker position.
     *
     * @param level The ServerLevel
     * @param markerPos The marker position (Amethyst Block)
     */
    private static void spawnEntropyKeeper(ServerLevel level, BlockPos markerPos) {
        // Find ground level for spawn
        BlockPos spawnPos = findGroundLevel(level, markerPos);

        ChronoDawn.LOGGER.info(
            "Spawning Entropy Keeper in Entropy Crypt at {} (marker: {}, dimension: {})",
            spawnPos,
            markerPos,
            level.dimension().location()
        );

        // Create and spawn Entropy Keeper entity
        EntropyKeeperEntity keeper = ModEntities.ENTROPY_KEEPER.get().create(level);
        if (keeper != null) {
            keeper.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0.0f,
                0.0f
            );
            keeper.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.STRUCTURE, null);

            boolean addedToWorld = level.addFreshEntity(keeper);

            if (addedToWorld) {
                ChronoDawn.LOGGER.info(
                    "Successfully spawned Entropy Keeper at {} (UUID: {})",
                    spawnPos,
                    keeper.getUUID()
                );
            } else {
                ChronoDawn.LOGGER.error(
                    "Failed to add Entropy Keeper entity to world at {}",
                    spawnPos
                );
            }
        } else {
            ChronoDawn.LOGGER.error(
                "Failed to create Entropy Keeper entity at {} (ModEntities.ENTROPY_KEEPER.get().create() returned null)",
                spawnPos
            );
        }
    }

    /**
     * Find ground level for boss spawn position.
     *
     * @param level ServerLevel
     * @param center Center position
     * @return Ground level position
     */
    private static BlockPos findGroundLevel(ServerLevel level, BlockPos center) {
        // Scan downward from marker to find solid ground
        for (int y = center.getY(); y >= center.getY() - 10; y--) {
            BlockPos checkPos = new BlockPos(center.getX(), y, center.getZ());
            BlockPos belowPos = checkPos.below();

            BlockState belowState = level.getBlockState(belowPos);
            BlockState checkState = level.getBlockState(checkPos);

            // Check if there's solid ground below and air/passable above
            if (!belowState.isAir() && (checkState.isAir() || !checkState.blocksMotion())) {
                return checkPos;
            }
        }

        // Fallback to center position above marker
        return center.above();
    }

    /**
     * Reset spawn tracking for a specific world (useful for testing or debugging).
     * T430: Now properly clears dimension-specific caches.
     *
     * @param level The ServerLevel to reset spawn data for
     */
    public static void reset(ServerLevel level) {
        BossSpawnData data = level.getDataStorage().computeIfAbsent(
            BossSpawnData.factory(),
            BossSpawnData.getDataName()
        );
        data.resetEntropyKeeper();
        tickCounter = 0;

        ResourceLocation dimensionId = level.dimension().location();
        cachedMarkers.remove(dimensionId);
        searchedChunks.remove(dimensionId);
        ChronoDawn.LOGGER.info("Entropy Keeper Spawner reset for dimension: {}", dimensionId);
    }
}
