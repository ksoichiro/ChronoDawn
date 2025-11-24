package com.chronosphere.events;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.portal.PortalRegistry;
import com.chronosphere.core.portal.PortalState;
import com.chronosphere.core.portal.PortalStateMachine;
import com.chronosphere.data.ChronosphereGlobalState;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModDimensions;
import com.chronosphere.registry.ModItems;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
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
    // Track player dimensions to detect changes
    private static final Map<UUID, ResourceKey<Level>> playerDimensions = new HashMap<>();

    /**
     * Register player event listeners.
     */
    public static void register() {
        // Register server tick event to monitor player dimension changes
        TickEvent.SERVER_POST.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ResourceKey<Level> currentDimension = player.level().dimension();
                ResourceKey<Level> previousDimension = playerDimensions.get(player.getUUID());

                // Check if dimension changed
                if (previousDimension != null && !previousDimension.equals(currentDimension)) {
                    // Player changed dimension
                    if (currentDimension.equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
                        // Player entered Chronosphere
                        onPlayerChangeDimension(player);
                    }
                }

                // Update tracked dimension
                playerDimensions.put(player.getUUID(), currentDimension);
            }
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

        // Chronicle of Chronosphere book is given automatically via advancement system
        // See: data/chronosphere/advancement/grant_chronicle_book.json

        // Mark global state: Chronosphere has been entered
        ChronosphereGlobalState globalState = ChronosphereGlobalState.get(player.server);
        globalState.markChronosphereEntered();

        // If portals are stable, do not deactivate portal
        // This allows free bidirectional travel after stabilization
        if (!globalState.arePortalsUnstable()) {
            Chronosphere.LOGGER.info("Portals are stable, skipping deactivation for player {}",
                player.getName().getString());
            return;  // Early return - portal remains active
        }

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
        int removedBlocks = 0;
        boolean foundPortalBlock = false;

        // Search for portal blocks in a larger 30x30x30 area
        for (int x = -15; x <= 15; x++) {
            for (int y = -15; y <= 15; y++) {
                for (int z = -15; z <= 15; z++) {
                    BlockPos searchPos = centerPos.offset(x, y, z);
                    var blockState = level.getBlockState(searchPos);

                    // Check for Custom Portal API's custom portal block
                    Block block = blockState.getBlock();
                    ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

                    if (blockId.getNamespace().equals("customportalapi") && blockId.getPath().equals("customportalblock")) {
                        foundPortalBlock = true;
                        level.removeBlock(searchPos, false);
                        removedBlocks++;
                    }

                    // Also check for nether portal blocks (fallback)
                    if (blockState.is(Blocks.NETHER_PORTAL)) {
                        foundPortalBlock = true;
                        level.removeBlock(searchPos, false);
                        removedBlocks++;
                    }
                }
            }
        }

        if (!foundPortalBlock) {
            // Last resort: search in a very large area
            for (int x = -50; x <= 50; x++) {
                for (int y = -50; y <= 50; y++) {
                    for (int z = -50; z <= 50; z++) {
                        BlockPos searchPos = centerPos.offset(x, y, z);
                        var blockState = level.getBlockState(searchPos);
                        Block block = blockState.getBlock();
                        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

                        // Check for Custom Portal API's custom portal block
                        if (blockId.getNamespace().equals("customportalapi") && blockId.getPath().equals("customportalblock")) {
                            level.removeBlock(searchPos, false);
                            removedBlocks++;
                        }

                        // Also check for nether portal blocks (fallback)
                        if (blockState.is(Blocks.NETHER_PORTAL)) {
                            level.removeBlock(searchPos, false);
                            removedBlocks++;
                        }
                    }
                }
            }
        }

        Chronosphere.LOGGER.info("Extinguished {} portal blocks near {}", removedBlocks, centerPos);
    }

    /**
     * Remove Time Hourglass from player inventory.
     * Time Hourglass is a single-use item that should be consumed when entering Chronosphere.
     *
     * @param player Player who entered Chronosphere
     */
    private static void removeTimeHourglassFromInventory(ServerPlayer player) {
        int removedCount = 0;

        // Search all inventory slots for Time Hourglass
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.TIME_HOURGLASS.get())) {
                int count = stack.getCount();
                player.getInventory().removeItem(i, count);
                removedCount += count;
                Chronosphere.LOGGER.info("Removed {} Time Hourglass from slot {}", count, i);
            }
        }

        if (removedCount > 0) {
            Chronosphere.LOGGER.info("Removed total {} Time Hourglass items from player {}'s inventory",
                removedCount, player.getName().getString());
        }
    }
}

