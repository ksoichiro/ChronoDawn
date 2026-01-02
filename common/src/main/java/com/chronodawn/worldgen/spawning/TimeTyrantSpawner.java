package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.core.dimension.DimensionStabilizer;
import com.chronodawn.entities.bosses.TimeTyrantEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Time Tyrant Spawner
 *
 * Manages spawning of Time Tyrant entity in Master Clock structure.
 *
 * Spawn Conditions:
 * - Location: Master Clock boss room (ChronoDawn dimension)
 * - Frequency: One per Master Clock door
 * - Max per world: 1 (only one Time Tyrant per dimension)
 * - Trigger: Spawns when Master Clock door is opened
 * - Condition: Only spawns if Time Tyrant has not been defeated
 *
 * Implementation Strategy:
 * - Event-based spawning via BossRoomDoorBlock
 * - Tracks spawned doors to avoid duplicate spawning
 * - Checks global defeat status before spawning
 * - Calculates spawn position relative to door facing direction
 *
 * Reference: master-clock-design.md (Boss Room Specifications)
 * Task: Time Tyrant spawn logic (Master Clock boss room)
 */
public class TimeTyrantSpawner {
    // Track boss room doors that have already spawned Time Tyrant (thread-safe)
    private static final Set<BlockPos> spawnedDoors = Collections.synchronizedSet(new HashSet<>());

    // Maximum Time Tyrants per world
    private static final int MAX_TIME_TYRANTS_PER_WORLD = 1;

    // Counter for spawned Time Tyrants in current world (thread-safe)
    private static final AtomicInteger spawnedTyrantsCount = new AtomicInteger(0);

    /**
     * Initialize Time Tyrant spawning system.
     */
    public static void register() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
                ChronoDawn.LOGGER.info("Time Tyrant Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        ChronoDawn.LOGGER.info("Registered TimeTyrantSpawner");
    }

    /**
     * Check if a Time Tyrant already exists near the given position.
     *
     * @param level The ServerLevel to check
     * @param pos The position to check around
     * @return true if a Time Tyrant is nearby
     */
    private static boolean isTyrantNearby(ServerLevel level, BlockPos pos) {
        double searchRadius = 50.0; // Search within 50 blocks (boss room is large)
        var nearbyTyrants = level.getEntitiesOfClass(
            TimeTyrantEntity.class,
            new net.minecraft.world.phys.AABB(pos).inflate(searchRadius)
        );
        return !nearbyTyrants.isEmpty();
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
     * Spawn Time Tyrant when Boss Room Door is opened.
     * This is called from BossRoomDoorBlock when a player opens the boss room door.
     *
     * @param level The ServerLevel where the door was opened
     * @param doorPos The position of the door
     * @param doorState The BlockState of the door
     */
    public static void spawnOnDoorOpen(ServerLevel level, BlockPos doorPos, BlockState doorState) {
        ChronoDawn.LOGGER.info("Boss Room Door opened at {}", doorPos);

        // Check if Time Tyrant has already been defeated
        if (DimensionStabilizer.isTyrantDefeated(level.getServer())) {
            ChronoDawn.LOGGER.info("Time Tyrant already defeated - not spawning");
            return;
        }

        // Check if we've already spawned from this door
        if (spawnedDoors.contains(doorPos)) {
            ChronoDawn.LOGGER.info("Time Tyrant already spawned from this door - not spawning again");
            return;
        }

        // Check if we've reached the spawn limit
        int currentCount = spawnedTyrantsCount.get();
        if (currentCount >= MAX_TIME_TYRANTS_PER_WORLD) {
            ChronoDawn.LOGGER.info("Time Tyrant spawn limit reached ({}/{}) - not spawning",
                currentCount, MAX_TIME_TYRANTS_PER_WORLD);
            return;
        }

        // Get door's facing direction
        var facing = doorState.getValue(DoorBlock.FACING);

        // Calculate spawn position (15 blocks inside the door, 4 blocks above ground)
        // Boss room has elevated central platform
        BlockPos spawnPos = doorPos.relative(facing, 15).above(4);

        // Find valid spawn position (air with solid ground)
        spawnPos = findValidSpawnPosition(level, spawnPos);

        // Check if a Time Tyrant already exists near this position
        if (isTyrantNearby(level, spawnPos)) {
            ChronoDawn.LOGGER.info("Time Tyrant already exists near {} - not spawning", spawnPos);
            return;
        }

        // Create and spawn Time Tyrant
        TimeTyrantEntity tyrant = ModEntities.TIME_TYRANT.get().create(level);
        if (tyrant != null) {
            tyrant.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0.0f, 0.0f
            );

            tyrant.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.TRIGGERED, // Use TRIGGERED instead of STRUCTURE
                null
            );

            level.addFreshEntity(tyrant);

            // Mark this door as spawned
            spawnedDoors.add(doorPos);

            // Increment counter
            int newCount = spawnedTyrantsCount.incrementAndGet();

            ChronoDawn.LOGGER.info(
                "Time Tyrant spawned at [{}, {}, {}] from Boss Room Door (Total: {}/{})",
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                newCount, MAX_TIME_TYRANTS_PER_WORLD
            );
        } else {
            ChronoDawn.LOGGER.error("Failed to create Time Tyrant entity");
        }
    }

    /**
     * Reset spawn tracking (useful for testing or world reset).
     */
    public static void reset() {
        spawnedDoors.clear();
        spawnedTyrantsCount.set(0);
        ChronoDawn.LOGGER.info("Time Tyrant Spawner reset");
    }
}
