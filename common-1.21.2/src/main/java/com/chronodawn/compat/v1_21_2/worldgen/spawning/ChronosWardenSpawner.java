package com.chronodawn.worldgen.spawning;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import com.chronodawn.data.BossSpawnData;
import com.chronodawn.entities.bosses.ChronosWardenEntity;
import com.chronodawn.registry.ModEntities;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chronos Warden Spawner
 *
 * Manages spawning of Chronos Warden entity in Guardian Vault structure.
 *
 * Spawn Conditions:
 * - Location: Guardian Vault structure (ChronoDawn dimension)
 * - Frequency: One per Guardian Vault door
 * - Trigger: Spawns when Guardian Vault door is opened
 * - Persistence: Uses SavedData to prevent duplicate spawning after server restart
 *
 * Implementation Strategy:
 * - Event-based spawning via BossRoomDoorBlock
 * - Uses SavedData to persist spawn state across server restarts
 * - Tracks spawned doors to avoid duplicate spawning
 * - Calculates spawn position relative to door facing direction
 *
 * Reference: T234 - Chronos Warden implementation
 * Task: Chronos Warden spawn logic (Guardian Vault structure)
 */
public class ChronosWardenSpawner {

    /**
     * Initialize Chronos Warden spawning system.
     */
    public static void register() {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> {
            if (level instanceof ServerLevel serverLevel) {
                ChronoDawn.LOGGER.info("Chronos Warden Spawner initialized for dimension: {}", serverLevel.dimension().location());
            }
        });

        ChronoDawn.LOGGER.info("Registered ChronosWardenSpawner");
    }

    /**
     * Check if a Chronos Warden already exists near the given position.
     *
     * @param level The ServerLevel to check
     * @param pos The position to check around
     * @return true if a Chronos Warden is nearby
     */
    private static boolean isWardenNearby(ServerLevel level, BlockPos pos) {
        double searchRadius = 50.0; // Search within 50 blocks
        var nearbyWardens = level.getEntitiesOfClass(
            ChronosWardenEntity.class,
            new net.minecraft.world.phys.AABB(pos).inflate(searchRadius)
        );
        return !nearbyWardens.isEmpty();
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
     * Spawn Chronos Warden when Guardian Vault Door is opened.
     * This is called from BossRoomDoorBlock when a player opens the guardian vault door.
     *
     * @param level The ServerLevel where the door was opened
     * @param doorPos The position of the door
     * @param doorState The BlockState of the door
     */
    public static void spawnOnDoorOpen(ServerLevel level, BlockPos doorPos, BlockState doorState) {
        ChronoDawn.LOGGER.info("Guardian Vault Door opened at {}", doorPos);

        // Get saved data for this world (persists across server restarts)
        BossSpawnData data = CompatSavedData.computeIfAbsent(
            level.getDataStorage(),
            BossSpawnData::new,
            BossSpawnData::load,
            BossSpawnData.getDataName()
        );

        // Check if we've already spawned from this door
        if (data.hasChronosWardenDoorSpawned(doorPos)) {
            ChronoDawn.LOGGER.info("Chronos Warden already spawned from this door - not spawning again");
            return;
        }

        // Get door's facing direction
        var facing = doorState.getValue(DoorBlock.FACING);

        // Calculate spawn position (10 blocks inside the door, 10 blocks to the right)
        // This places the boss at the center of the room
        BlockPos spawnPos = doorPos.relative(facing, 10)           // 10 blocks inside
                                   .relative(facing.getClockWise(), 10)  // 10 blocks to the right
                                   .above(1);

        // Find valid spawn position (air with solid ground)
        spawnPos = findValidSpawnPosition(level, spawnPos);

        // Check if a Chronos Warden already exists near this position
        if (isWardenNearby(level, spawnPos)) {
            ChronoDawn.LOGGER.info("Chronos Warden already exists near {} - not spawning", spawnPos);
            return;
        }

        // Note: Boss room protection is now handled by BossRoomProtectionProcessor
        // during structure generation (marker blocks in NBT → BoundingBox calculation)

        // Create and spawn Chronos Warden
        ChronosWardenEntity warden = ModEntities.CHRONOS_WARDEN.get().create(level);
        if (warden != null) {
            warden.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0.0f, 0.0f
            );

            warden.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawnPos),
                EntitySpawnReason.TRIGGERED, // Use TRIGGERED instead of STRUCTURE
                null
            );

            // Note: Door position no longer needed for protection (handled by marker blocks)
            // Boss room protection uses marker block positions as unique IDs

            level.addFreshEntity(warden);

            // Mark this door as spawned (persisted to disk)
            data.markChronosWardenDoorSpawned(doorPos);

            ChronoDawn.LOGGER.info(
                "Chronos Warden spawned at [{}, {}, {}] from Guardian Vault Door",
                spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()
            );
        } else {
            ChronoDawn.LOGGER.error("Failed to create Chronos Warden entity");
        }
    }

    /**
     * Reset spawn tracking for a specific world (useful for testing or debugging).
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
        data.resetChronosWarden();
        ChronoDawn.LOGGER.info("Chronos Warden Spawner reset for dimension: {}", level.dimension().location());
    }
}
