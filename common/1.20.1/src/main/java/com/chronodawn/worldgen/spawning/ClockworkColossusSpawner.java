package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.ClockworkColossusEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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
 * - Uses server tick event to check registered engine rooms
 * - Checks if any player is inside a registered engine room
 * - Searches for DANGER!! sign markers within the engine room
 * - Spawns Clockwork Colossus when player approaches marker
 *
 * Reference: T235 - Clockwork Colossus implementation
 */
public class ClockworkColossusSpawner {
    // Track structure positions where we've already spawned Clockwork Colossus
    private static final Set<BlockPos> spawnedStructures = new HashSet<>();

    // Track registered engine rooms (bounding boxes from BossRoomProtectionProcessor) (per-server runtime cache)
    // Key: dimension ID, Value: Set of bounding boxes
    private static final Map<ResourceLocation, Set<BoundingBox>> engineRooms = new ConcurrentHashMap<>();

    // Check interval (in ticks) - check every 10 seconds to reduce load
    private static final int CHECK_INTERVAL = 200;
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
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) level;
                ChronoDawn.LOGGER.debug("Clockwork Colossus Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        ChronoDawn.LOGGER.debug("Registered ClockworkColossusSpawner");
    }

    /**
     * Check for Clockwork Depths structures and spawn Clockwork Colossus if player is nearby.
     * Uses direct engine room bounding box check (same approach as 1.21.1+).
     *
     * @param level The ServerLevel to check
     */
    public static void checkAndSpawnColossus(ServerLevel level) {
        // Only process Chrono Dawn dimension (Clockwork Depths only spawns there)
        if (!level.dimension().equals(com.chronodawn.registry.ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        ResourceLocation dimensionId = level.dimension().location();

        // Increment tick counter
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        // Only process if there are players in the dimension
        if (level.players().isEmpty()) {
            return;
        }

        // Check if any player is inside a registered engine room
        Set<BoundingBox> rooms = engineRooms.get(dimensionId);
        if (rooms == null || rooms.isEmpty()) {
            return;
        }

        for (var player : level.players()) {
            BlockPos playerPos = player.blockPosition();

            // Check if player is inside any engine room
            for (BoundingBox room : rooms) {
                if (room.isInside(playerPos)) {

                    // Use room center as structure identifier for tracking
                    BlockPos roomCenter = new BlockPos(
                        (room.minX() + room.maxX()) / 2,
                        (room.minY() + room.maxY()) / 2,
                        (room.minZ() + room.maxZ()) / 2
                    );

                    // Check if we've already spawned in this room
                    if (spawnedStructures.contains(roomCenter)) {
                        continue;
                    }

                    // Search for markers in this engine room
                    List<BlockPos> markerPositions = findEngineRoomMarkersInBoundingBox(level, room);

                    if (markerPositions.isEmpty()) {
                        continue;
                    }

                    // Check if player is within spawn distance of any marker
                    boolean playerNearby = false;

                    for (BlockPos marker : markerPositions) {
                        if (player.blockPosition().distSqr(marker) <= SPAWN_DISTANCE * SPAWN_DISTANCE) {
                            playerNearby = true;
                            break;
                        }
                    }

                    if (!playerNearby) {
                        continue;
                    }

                    // Mark this room as spawned
                    spawnedStructures.add(roomCenter);
                    ChronoDawn.LOGGER.debug("Spawning Clockwork Colossus in engine room at {}", roomCenter);

                    // Spawn Clockwork Colossus
                    spawnClockworkColossusAtEngineRoom(level, markerPositions);
                    break; // Only spawn once per player check
                }
            }
        }
    }

    /**
     * Find all markers (Signs with "DANGER!!" text) within a specific engine room bounding box.
     *
     * @param level The ServerLevel
     * @param room The engine room bounding box
     * @return List of marker positions
     */
    private static List<BlockPos> findEngineRoomMarkersInBoundingBox(ServerLevel level, BoundingBox room) {
        List<BlockPos> markers = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(room.minX(), room.minY(), room.minZ(),
                                                    room.maxX(), room.maxY(), room.maxZ())) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign) {
                boolean isDangerSign = false;

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

        return markers;
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

            ChronoDawn.LOGGER.debug(
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
     * Register an engine room bounding box from BossRoomProtectionProcessor.
     * Called when a Clockwork Depths engine room is detected during worldgen.
     *
     * @param level The ServerLevel containing the engine room
     * @param boundingBox The bounding box of the engine room
     */
    public static void registerEngineRoom(ServerLevel level, BoundingBox boundingBox) {
        ResourceLocation dimensionId = level.dimension().location();
        engineRooms.putIfAbsent(dimensionId, new HashSet<>());
        engineRooms.get(dimensionId).add(boundingBox);

        ChronoDawn.LOGGER.debug("Registered Clockwork Depths engine room in dimension {}: {}",
            dimensionId, boundingBox);
    }

    /**
     * Reset spawn tracking for a specific world (useful for testing or debugging).
     *
     * @param level The ServerLevel to reset spawn data for
     */
    public static void reset(ServerLevel level) {
        ResourceLocation dimensionId = level.dimension().location();
        spawnedStructures.clear();
        engineRooms.remove(dimensionId);
        tickCounter = 0;
        ChronoDawn.LOGGER.debug("Clockwork Colossus Spawner reset for dimension: {}", dimensionId);
    }
}
