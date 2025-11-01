package com.chronosphere.events;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.portal.PortalRegistry;
import com.chronosphere.core.portal.PortalState;
import com.chronosphere.core.portal.PortalStateMachine;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModDimensions;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

/**
 * Player event handler using Architectury Event API.
 *
 * This handler manages player-related events such as:
 * - Dimension change (portal deactivation when entering Chronosphere)
 * - Portal travel blocking (prevent travel through deactivated portals)
 * - Player crafting events (for Unstable Hourglass reversed resonance trigger)
 *
 * Respawn Logic Design:
 * - Chronosphere respawn follows Minecraft's standard behavior (like End dimension)
 * - Players respawn at their set bed/respawn anchor, or world spawn if none set
 * - Portal Stabilizer does NOT affect respawn location
 * - Portal Stabilizer only makes portal bidirectional (like End return portal after dragon defeat)
 * - Players can escape Chronosphere by breaking bed and dying (same as End)
 * - This design maintains tension without excessive difficulty
 *
 * Design Rationale (from user feedback):
 * Similar to End dimension:
 * - Entry: One-way portal (requires Time Hourglass, like End Portal)
 * - Return: Portal Stabilizer makes it bidirectional (like End return portal after dragon)
 * - Death: Normal Minecraft respawn (at bed/anchor or world spawn)
 * - Escape: Break bed and die to return to Overworld
 * - Tension: Portal is one-way initially, but not punishing on death
 *
 * TODO: Implement in future phases:
 * - Unstable Hourglass crafting trigger (reversed resonance effect)
 * - Item cooldown management (Time Clock, Time Guardian's Mail rollback)
 *
 * Reference: data-model.md (Items, Game Mechanics)
 * Task: T087 [US1] Implement respawn handler - REVERTED to Minecraft standard behavior
 */
public class PlayerEventHandler {
    /**
     * Register player event listeners.
     */
    public static void register() {
        // Register entity add event to detect dimension change
        // This event fires when entity is added to a level (including dimension change)
        EntityEvent.ADD.register((entity, level) -> {
            if (entity instanceof ServerPlayer player && level instanceof ServerLevel serverLevel) {
                // Check if player just entered Chronosphere
                // We use a small delay to ensure player position is set
                serverLevel.getServer().execute(() -> {
                    if (player.serverLevel().dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
                        onPlayerChangeDimension(player);
                    }
                });
            }
            return dev.architectury.event.EventResult.pass();
        });

        Chronosphere.LOGGER.info("Registered PlayerEventHandler");
    }

    /**
     * Handle player dimension change.
     * Deactivates portal when player enters Chronosphere for the first time.
     *
     * @param player Player who changed dimension
     */
    private static void onPlayerChangeDimension(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ResourceKey<Level> dimensionKey = level.dimension();

        // Check if player entered Chronosphere
        if (!dimensionKey.equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
            return;
        }

        Chronosphere.LOGGER.info("Player {} entered Chronosphere", player.getName().getString());

        // Find portal near player
        BlockPos playerPos = player.blockPosition();
        PortalStateMachine portal = findNearbyPortal(level, playerPos);

        if (portal != null) {
            // Check if portal is already registered and stabilized
            if (portal.getCurrentState() == PortalState.STABILIZED) {
                Chronosphere.LOGGER.info("Portal {} is already stabilized, skipping deactivation", portal.getPortalId());
                return;
            }

            // Deactivate portal
            if (portal.deactivate()) {
                // Remove portal blocks
                extinguishPortal(level, playerPos);
                Chronosphere.LOGGER.info("Deactivated portal {} after player entry", portal.getPortalId());
            }
        } else {
            // Register new portal as deactivated
            UUID portalId = UUID.randomUUID();
            portal = new PortalStateMachine(portalId, dimensionKey, playerPos);
            portal.setState(PortalState.DEACTIVATED);
            PortalRegistry.getInstance().registerPortal(portal);

            // Remove portal blocks
            extinguishPortal(level, playerPos);
            Chronosphere.LOGGER.info("Registered and deactivated new portal {} at {}", portalId, playerPos);
        }
    }

    /**
     * Find a portal near the specified position.
     *
     * @param level Level
     * @param pos Center position
     * @return Portal state machine, or null if not found
     */
    private static PortalStateMachine findNearbyPortal(Level level, BlockPos pos) {
        PortalRegistry registry = PortalRegistry.getInstance();

        // Search in a 10x10x10 area
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos searchPos = pos.offset(x, y, z);
                    PortalStateMachine portal = registry.getPortalAt(searchPos);
                    if (portal != null) {
                        return portal;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Extinguish portal by removing portal blocks near the position.
     *
     * @param level Level
     * @param centerPos Center position
     */
    private static void extinguishPortal(ServerLevel level, BlockPos centerPos) {
        // Search for portal blocks in a 10x10x10 area and remove them
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos searchPos = centerPos.offset(x, y, z);
                    if (level.getBlockState(searchPos).is(Blocks.NETHER_PORTAL)) {
                        level.removeBlock(searchPos, false);
                    }
                }
            }
        }

        Chronosphere.LOGGER.info("Extinguished portal blocks near {}", centerPos);
    }
}

