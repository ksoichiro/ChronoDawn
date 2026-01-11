package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import com.chronodawn.compat.CompatResourceLocation;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Entropy Keeper Spawner
 *
 * Manages spawning of Entropy Keeper entity in Entropy Crypt structure.
 *
 * Spawn Conditions:
 * - Location: Entropy Crypt boss chamber (ChronoDawn dimension)
 * - Frequency: One per structure
 * - Condition: Spawns when player enters boss chamber
 *
 * Implementation Strategy:
 * - Detects Entropy Crypt structures via structure registry
 * - Uses Amethyst Block as marker for boss spawn position
 * - Tracks spawned structures to avoid duplicate spawning
 * - Spawns Entropy Keeper when player approaches marker
 *
 * Reference: T237 - Entropy Keeper implementation (Phase 2)
 * Task: Entropy Keeper spawn logic (Entropy Crypt structure)
 */
public class EntropyKeeperSpawner {
    private static final ResourceLocation ENTROPY_CRYPT_ID = CompatResourceLocation.create(
        ChronoDawn.MOD_ID,
        "entropy_crypt"
    );

    // Track structure positions where we've already attempted to spawn Entropy Keeper (per dimension)
    private static final Map<ResourceLocation, Set<BlockPos>> processedStructures = new HashMap<>();

    // Track boss chamber markers where Entropy Keeper has spawned (per dimension)
    private static final Map<ResourceLocation, Set<BlockPos>> spawnedMarkers = new HashMap<>();

    // Check interval (in ticks) - check every 2 seconds
    private static final int CHECK_INTERVAL = 40;
    // T430: Per-dimension tick counters to prevent cross-dimension interference
    private static final Map<ResourceLocation, Integer> tickCounters = new HashMap<>();

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
        ResourceLocation dimensionId = level.dimension().location();

        // Initialize tick counter for this dimension
        tickCounters.putIfAbsent(dimensionId, 0);
        int tickCounter = tickCounters.get(dimensionId) + 1;
        tickCounters.put(dimensionId, tickCounter);

        // Only check every CHECK_INTERVAL ticks
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounters.put(dimensionId, 0);

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Initialize tracking sets for this dimension
        processedStructures.putIfAbsent(dimensionId, new HashSet<>());
        spawnedMarkers.putIfAbsent(dimensionId, new HashSet<>());
        Set<BlockPos> processed = processedStructures.get(dimensionId);

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
                        if (processed.contains(structurePos)) {
                            continue;
                        }

                        // Mark this structure as processed
                        processed.add(structurePos);

                        ChronoDawn.LOGGER.info("Found Entropy Crypt structure at chunk {} (block pos: {})", chunkPos, structurePos);

                        // Find boss chamber marker and spawn if player is nearby
                        checkAndSpawnAtMarker(level, structurePos, player);
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
     * Find boss chamber marker (Amethyst Block) and spawn Entropy Keeper if player is nearby.
     *
     * @param level The ServerLevel
     * @param structurePos The structure position
     * @param player The player to check proximity
     */
    private static void checkAndSpawnAtMarker(ServerLevel level, BlockPos structurePos, ServerPlayer player) {
        ResourceLocation dimensionId = level.dimension().location();
        Set<BlockPos> spawned = spawnedMarkers.get(dimensionId);
        if (spawned == null) {
            spawned = new HashSet<>();
            spawnedMarkers.put(dimensionId, spawned);
        }

        // Search for Amethyst Block marker in structure area
        int searchRadius = 50;
        int minY = -60;
        int maxY = 100;

        for (int y = maxY; y >= minY; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = new BlockPos(structurePos.getX() + x, y, structurePos.getZ() + z);

                    // Skip if already spawned at this marker
                    if (spawned.contains(checkPos)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(checkPos);

                    // Check for Amethyst Block (boss spawn marker)
                    if (state.is(Blocks.AMETHYST_BLOCK)) {
                        // Check if player is within spawn trigger range (20 blocks)
                        double distance = player.position().distanceTo(checkPos.getCenter());

                        if (distance <= 20.0) {
                            ChronoDawn.LOGGER.info(
                                "Player {} is near Entropy Crypt boss marker at {} (distance: {}), spawning Entropy Keeper",
                                player.getName().getString(),
                                checkPos,
                                distance
                            );

                            // Spawn Entropy Keeper
                            spawnEntropyKeeper(level, checkPos);
                            spawned.add(checkPos);

                            // Remove marker block
                            level.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 3);

                            return;
                        }
                    }
                }
            }
        }
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
            keeper.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.STRUCTURE, null, null);

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
        ResourceLocation dimensionId = level.dimension().location();
        processedStructures.remove(dimensionId);
        spawnedMarkers.remove(dimensionId);
        tickCounters.remove(dimensionId);
        ChronoDawn.LOGGER.info("Entropy Keeper Spawner reset for dimension: {}", dimensionId);
    }
}
