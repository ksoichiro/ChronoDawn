package com.chronosphere.core.teleport;

import com.chronosphere.blocks.ClockTowerTeleporterBlock;
import com.chronosphere.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the charging and teleportation logic for Clock Tower Teleporter.
 *
 * Called every tick to check if player is still charging and trigger teleport after 3 seconds.
 * Uses server-side Map to track charging state (no NBT persistence needed for temporary data).
 */
public class TeleporterChargingHandler {
    private static final int CHARGE_DURATION_TICKS = 60; // 3 seconds
    private static final double MAX_DISTANCE = 3.0; // Maximum distance from teleporter while charging

    // Server-side charging state storage
    private static final Map<UUID, ChargingData> CHARGING_PLAYERS = new ConcurrentHashMap<>();

    /**
     * Data class to hold charging state for a player.
     */
    private static class ChargingData {
        final long startTime;
        final BlockPos teleporterPos;
        final String direction;

        ChargingData(long startTime, BlockPos teleporterPos, String direction) {
            this.startTime = startTime;
            this.teleporterPos = teleporterPos;
            this.direction = direction;
        }
    }

    /**
     * Start charging a teleporter for a player.
     *
     * @param player The player starting the charge
     * @param teleporterPos The position of the teleporter
     * @param direction The teleport direction (UP or DOWN)
     * @param startTime The game time when charging started
     */
    public static void startCharging(ServerPlayer player, BlockPos teleporterPos, String direction, long startTime) {
        CHARGING_PLAYERS.put(player.getUUID(), new ChargingData(startTime, teleporterPos, direction));
    }

    /**
     * Check and update teleporter charging for a player.
     * Called every server tick.
     *
     * @param player The player to check
     */
    public static void tick(ServerPlayer player) {
        ChargingData data = CHARGING_PLAYERS.get(player.getUUID());

        // Check if player is charging
        if (data == null) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        long currentTime = level.getGameTime();
        long elapsedTicks = currentTime - data.startTime;

        // Check if player is still close enough to the teleporter
        double distance = player.position().distanceTo(Vec3.atCenterOf(data.teleporterPos));
        if (distance > MAX_DISTANCE) {
            cancelCharging(player);
            return;
        }

        // Check if player is still holding right-click (using item)
        // This is approximated by checking if they're still using an item or interacting
        // In practice, we'll just check distance and time

        // Spawn particles based on charge progress
        spawnChargingParticles(level, data.teleporterPos, player.position(), elapsedTicks);

        // Play sounds at intervals
        if (elapsedTicks == 20) {
            // 1 second
            level.playSound(null, data.teleporterPos, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5f, 1.0f);
        } else if (elapsedTicks == 40) {
            // 2 seconds
            level.playSound(null, data.teleporterPos, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.7f, 1.2f);
        }

        // Check if charging is complete
        if (elapsedTicks >= CHARGE_DURATION_TICKS) {
            performTeleport(player, level, data.teleporterPos, data.direction);
            cancelCharging(player);
        }
    }

    /**
     * Spawn charging particles based on progress.
     */
    private static void spawnChargingParticles(ServerLevel level, BlockPos teleporterPos, Vec3 playerPos, long elapsedTicks) {
        double progress = Math.min(1.0, elapsedTicks / (double) CHARGE_DURATION_TICKS);

        // Particle count increases with progress
        int particleCount = (int) (5 + progress * 25);

        for (int i = 0; i < particleCount / 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;

            level.sendParticles(
                ParticleTypes.PORTAL,
                teleporterPos.getX() + 0.5 + offsetX,
                teleporterPos.getY() + 0.5 + offsetY,
                teleporterPos.getZ() + 0.5 + offsetZ,
                1,
                0, 0, 0,
                0.1
            );
        }

        // Add dragon breath particles in final second
        if (progress > 0.66) {
            level.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                teleporterPos.getX() + 0.5,
                teleporterPos.getY() + 0.5,
                teleporterPos.getZ() + 0.5,
                2,
                0.3, 0.3, 0.3,
                0.05
            );
        }
    }

    /**
     * Perform the actual teleportation.
     */
    private static void performTeleport(ServerPlayer player, ServerLevel level, BlockPos teleporterPos, String direction) {
        BlockState state = level.getBlockState(teleporterPos);

        if (!(state.getBlock() instanceof ClockTowerTeleporterBlock)) {
            return;
        }

        // Calculate target position
        BlockPos targetPos;

        if (direction.equals("DOWN")) {
            // For DOWN teleporters, use stored target position from BlockEntity
            if (level.getBlockEntity(teleporterPos) instanceof com.chronosphere.blocks.ClockTowerTeleporterBlockEntity be) {
                BlockPos storedTarget = be.getTargetPos();
                if (storedTarget != null) {
                    targetPos = storedTarget;
                } else {
                    // Fallback to relative position
                    int floorHeight = 8;
                    targetPos = teleporterPos.offset(0, -floorHeight, 0);
                }
            } else {
                // Fallback to relative position
                int floorHeight = 8;
                targetPos = teleporterPos.offset(0, -floorHeight, 0);
            }
        } else {
            // For UP teleporters, use relative position
            int floorHeight = 8;
            targetPos = teleporterPos.offset(0, floorHeight, 0);
        }

        // Find safe landing position near target (offset from teleporter block)
        Vec3 safeLandingPos = findSafeLandingPosition(level, targetPos);

        // Teleport sound and particles at departure
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(
            ParticleTypes.PORTAL,
            player.getX(), player.getY() + 1.0, player.getZ(),
            50,
            0.5, 0.5, 0.5,
            0.2
        );

        // Perform teleport to safe landing position
        player.teleportTo(
            safeLandingPos.x,
            safeLandingPos.y,
            safeLandingPos.z
        );

        // Teleport sound and particles at arrival
        level.playSound(null, targetPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(
            ParticleTypes.PORTAL,
            targetPos.getX() + 0.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5,
            50,
            0.5, 0.5, 0.5,
            0.2
        );
    }

    /**
     * Cancel charging and clear player data.
     */
    public static void cancelCharging(ServerPlayer player) {
        CHARGING_PLAYERS.remove(player.getUUID());
    }

    /**
     * Check if player is currently charging a teleporter.
     */
    public static boolean isCharging(ServerPlayer player) {
        return CHARGING_PLAYERS.containsKey(player.getUUID());
    }

    /**
     * Find safe landing position near target teleporter.
     * Checks positions around the teleporter block to find a safe spot with:
     * - Solid ground beneath
     * - Air blocks for player body (2 blocks high)
     * - Not inside the teleporter block itself
     *
     * @param level The server level
     * @param targetPos The target teleporter block position
     * @return Safe landing position (Vec3 with coordinates)
     */
    private static Vec3 findSafeLandingPosition(ServerLevel level, BlockPos targetPos) {
        // Try positions around the teleporter (prioritize horizontal offsets)
        BlockPos[] candidateOffsets = {
            targetPos.offset(2, 0, 0),   // 2 blocks east
            targetPos.offset(-2, 0, 0),  // 2 blocks west
            targetPos.offset(0, 0, 2),   // 2 blocks south
            targetPos.offset(0, 0, -2),  // 2 blocks north
            targetPos.offset(1, 0, 1),   // 1 block southeast
            targetPos.offset(-1, 0, -1), // 1 block northwest
            targetPos.offset(1, 0, -1),  // 1 block northeast
            targetPos.offset(-1, 0, 1),  // 1 block southwest
            targetPos.offset(1, 0, 0),   // 1 block east (closer)
            targetPos.offset(-1, 0, 0),  // 1 block west (closer)
            targetPos.offset(0, 0, 1),   // 1 block south (closer)
            targetPos.offset(0, 0, -1),  // 1 block north (closer)
        };

        for (BlockPos candidate : candidateOffsets) {
            if (isSafeLandingPosition(level, candidate)) {
                // Return center of block + 1 block up (player feet position)
                return new Vec3(candidate.getX() + 0.5, candidate.getY() + 1.0, candidate.getZ() + 0.5);
            }
        }

        // Fallback: teleporter position + offset (even if not ideal)
        return new Vec3(targetPos.getX() + 2.5, targetPos.getY() + 1.0, targetPos.getZ() + 0.5);
    }

    /**
     * Check if a position is safe for player landing.
     *
     * @param level The server level
     * @param pos The position to check
     * @return true if position is safe for landing
     */
    private static boolean isSafeLandingPosition(ServerLevel level, BlockPos pos) {
        // Check ground beneath (must be solid)
        if (!level.getBlockState(pos.below()).isSolid()) {
            return false;
        }

        // Check player body space (2 blocks high)
        for (int i = 0; i < 2; i++) {
            BlockPos checkPos = pos.above(i);
            BlockState blockState = level.getBlockState(checkPos);

            // Position must be air or passable (not solid blocks)
            if (blockState.isSolid()) {
                return false;
            }
        }

        return true;
    }
}
