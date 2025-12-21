package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.TemporalPhantomEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Temporal Phantom Spawner
 *
 * Manages spawning of Temporal Phantom entity in Phantom Catacombs boss_room.
 *
 * Spawn Conditions:
 * - Location: Phantom Catacombs boss_room (ChronoDawn dimension)
 * - Frequency: One per structure
 * - Condition: Spawns when player enters boss_room for the first time
 *
 * Implementation Strategy:
 * - Tracks boss_room positions after PhantomCatacombsBossRoomPlacer completes
 * - Checks if any player enters boss_room bounding box
 * - Spawns Temporal Phantom at boss_room center when triggered
 * - Tracks spawned boss_rooms to avoid duplicate spawning
 *
 * Reference: T236 - Temporal Phantom implementation (Phase 2)
 * Task: Temporal Phantom spawn logic (Phantom Catacombs boss_room)
 */
public class TemporalPhantomSpawner {
    private static final ResourceLocation PHANTOM_CATACOMBS_ID = ResourceLocation.fromNamespaceAndPath(
        ChronoDawn.MOD_ID,
        "phantom_catacombs"
    );

    // Track boss_room positions (per dimension)
    // Key: dimension ID, Value: Set of boss_room center positions
    private static final Map<ResourceLocation, Set<BlockPos>> bossRoomPositions = new HashMap<>();

    // Track boss_rooms where Temporal Phantom has already spawned (per dimension)
    private static final Map<ResourceLocation, Set<BlockPos>> spawnedBossRooms = new HashMap<>();

    // Check interval (in ticks) - check every 1 second
    private static final int CHECK_INTERVAL = 20;
    private static final Map<ResourceLocation, Integer> tickCounters = new HashMap<>();

    /**
     * Register boss_room position for later spawn checking.
     * Called by PhantomCatacombsBossRoomPlacer after successful boss_room placement.
     *
     * @param level ServerLevel
     * @param bossRoomCenter Boss_room center position
     */
    public static void registerBossRoom(ServerLevel level, BlockPos bossRoomCenter) {
        ResourceLocation dimensionId = level.dimension().location();
        bossRoomPositions.putIfAbsent(dimensionId, new HashSet<>());
        bossRoomPositions.get(dimensionId).add(bossRoomCenter.immutable());

        ChronoDawn.LOGGER.info(
            "Registered Phantom Catacombs boss_room at {} in dimension {} for Temporal Phantom spawning",
            bossRoomCenter,
            dimensionId
        );
    }

    /**
     * Initialize Temporal Phantom spawning system.
     */
    public static void register() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
                ChronoDawn.LOGGER.info("Temporal Phantom Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        // Register server tick event to check for players entering boss_rooms
        dev.architectury.event.events.common.TickEvent.SERVER_POST.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                checkAndSpawnPhantom(level);
            }
        });

        ChronoDawn.LOGGER.info("Registered TemporalPhantomSpawner");
    }

    /**
     * Check for players entering boss_rooms and spawn Temporal Phantom if needed.
     * This should be called periodically from server tick event.
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndSpawnPhantom(ServerLevel level) {
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

        // No boss_rooms registered in this dimension
        if (!bossRoomPositions.containsKey(dimensionId)) {
            return;
        }

        // No players in this dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Initialize spawned tracking for this dimension
        spawnedBossRooms.putIfAbsent(dimensionId, new HashSet<>());
        Set<BlockPos> spawned = spawnedBossRooms.get(dimensionId);
        Set<BlockPos> bossRooms = bossRoomPositions.get(dimensionId);

        ChronoDawn.LOGGER.debug(
            "Checking Temporal Phantom spawn conditions in dimension {}: {} boss_rooms registered, {} players present",
            dimensionId,
            bossRooms.size(),
            level.players().size()
        );

        // Check each boss_room
        for (BlockPos bossRoomCenter : bossRooms) {
            // Skip if already spawned
            if (spawned.contains(bossRoomCenter)) {
                continue;
            }

            // Check if any player is near boss_room entrance
            // Boss_room is 21x21x9, check if player is within or near the room
            AABB bossRoomBounds = new AABB(bossRoomCenter).inflate(12, 6, 12);

            boolean playerNearby = false;
            ServerPlayer nearestPlayer = null;
            for (ServerPlayer player : level.players()) {
                if (bossRoomBounds.contains(player.position())) {
                    playerNearby = true;
                    nearestPlayer = player;
                    break;
                }
            }

            if (playerNearby) {
                ChronoDawn.LOGGER.info(
                    "Player {} entered boss_room at {} (dimension: {}), spawning Temporal Phantom",
                    nearestPlayer.getName().getString(),
                    bossRoomCenter,
                    dimensionId
                );
                // Spawn Temporal Phantom at boss_room center
                spawnTemporalPhantom(level, bossRoomCenter);
                spawned.add(bossRoomCenter);
            }
        }
    }

    /**
     * Spawn Temporal Phantom at boss_room center.
     *
     * @param level ServerLevel
     * @param bossRoomCenter Boss_room center position
     */
    private static void spawnTemporalPhantom(ServerLevel level, BlockPos bossRoomCenter) {
        // Find ground level (Y coordinate where boss should spawn)
        BlockPos spawnPos = findGroundLevel(level, bossRoomCenter);

        ChronoDawn.LOGGER.info(
            "Spawning Temporal Phantom in Phantom Catacombs boss_room at {} (center: {}, dimension: {})",
            spawnPos,
            bossRoomCenter,
            level.dimension().location()
        );

        // Create and spawn Temporal Phantom entity
        TemporalPhantomEntity phantom = ModEntities.TEMPORAL_PHANTOM.get().create(level);
        if (phantom != null) {
            phantom.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0.0f,
                0.0f
            );
            phantom.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.STRUCTURE, null);

            boolean addedToWorld = level.addFreshEntity(phantom);

            if (addedToWorld) {
                ChronoDawn.LOGGER.info(
                    "Successfully spawned Temporal Phantom at {} (UUID: {})",
                    spawnPos,
                    phantom.getUUID()
                );
            } else {
                ChronoDawn.LOGGER.error(
                    "Failed to add Temporal Phantom entity to world at {} (entity created but addFreshEntity returned false)",
                    spawnPos
                );
            }
        } else {
            ChronoDawn.LOGGER.error(
                "Failed to create Temporal Phantom entity at {} (ModEntities.TEMPORAL_PHANTOM.get().create() returned null)",
                spawnPos
            );
        }
    }

    /**
     * Find ground level for boss spawn position.
     * Scans downward from center position to find solid ground.
     *
     * @param level ServerLevel
     * @param center Center position
     * @return Ground level position
     */
    private static BlockPos findGroundLevel(ServerLevel level, BlockPos center) {
        ChronoDawn.LOGGER.debug("Searching for ground level from center position: {}", center);

        // Scan downward from center to find solid ground
        for (int y = center.getY(); y >= center.getY() - 10; y--) {
            BlockPos checkPos = new BlockPos(center.getX(), y, center.getZ());
            BlockPos belowPos = checkPos.below();

            BlockState belowState = level.getBlockState(belowPos);
            BlockState checkState = level.getBlockState(checkPos);

            // Check if there's solid ground below and air above
            if (!belowState.isAir() && checkState.isAir()) {
                ChronoDawn.LOGGER.info(
                    "Found ground level at Y={} (below: {}, current: {})",
                    y,
                    belowState.getBlock().getName().getString(),
                    checkState.getBlock().getName().getString()
                );
                return checkPos;
            }
        }

        // Fallback to center position if no ground found
        ChronoDawn.LOGGER.warn(
            "No ground level found within 10 blocks below center {}, using center position as fallback",
            center
        );
        return center;
    }

    /**
     * Clear tracking data for a dimension (useful for dimension unload).
     *
     * @param dimensionId Dimension resource location
     */
    public static void clearDimension(ResourceLocation dimensionId) {
        bossRoomPositions.remove(dimensionId);
        spawnedBossRooms.remove(dimensionId);
        tickCounters.remove(dimensionId);
    }

    /**
     * Reset all tracking data (useful for world reload).
     */
    public static void reset() {
        bossRoomPositions.clear();
        spawnedBossRooms.clear();
        tickCounters.clear();
    }
}
