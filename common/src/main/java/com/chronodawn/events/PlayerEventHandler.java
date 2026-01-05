package com.chronodawn.events;

import com.chronodawn.ChronoDawn;
import com.chronodawn.core.portal.PortalRegistry;
import com.chronodawn.core.portal.PortalState;
import com.chronodawn.core.portal.PortalStateMachine;
import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.registry.ModItems;
import com.chronodawn.worldgen.spawning.TimeKeeperVillagePlacer;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player event handler using Architectury Event API.
 *
 * This handler manages player-related events such as:
 * - Dimension change (portal deactivation when entering ChronoDawn)
 * - Portal travel blocking (prevent travel through deactivated portals)
 * - Player crafting events (for Unstable Hourglass reversed resonance trigger)
 *
 * Respawn Logic Design:
 * - ChronoDawn respawn follows Minecraft's standard behavior (like End dimension)
 * - Players respawn at their set bed/respawn anchor, or world spawn if none set
 * - Portal Stabilizer does NOT affect respawn location
 * - Portal Stabilizer only makes portal bidirectional (like End return portal after dragon defeat)
 * - Players can escape ChronoDawn by breaking bed and dying (same as End)
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
 * Thread Safety (T429):
 * - Uses ConcurrentHashMap for player dimension tracking to prevent race conditions
 *
 * Reference: data-model.md (Items, Game Mechanics)
 * Task: T087 [US1] Implement respawn handler - REVERTED to Minecraft standard behavior
 * Task: T429 [Thread Safety] Fix non-thread-safe collection usage
 */
public class PlayerEventHandler {
    // Track player dimensions to detect changes
    // T429: Use ConcurrentHashMap for thread-safe access in multiplayer
    private static final Map<UUID, ResourceKey<Level>> playerDimensions = new ConcurrentHashMap<>();

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
                    if (currentDimension.equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
                        // Player entered ChronoDawn
                        onPlayerChangeDimension(player);
                    }
                }

                // Update tracked dimension
                playerDimensions.put(player.getUUID(), currentDimension);
            }
        });

        ChronoDawn.LOGGER.info("Registered PlayerEventHandler");
    }

    /**
     * Handle player dimension change.
     * Deactivates portal when player enters ChronoDawn for the first time.
     *
     * @param player Player who changed dimension
     */
    private static void onPlayerChangeDimension(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ResourceKey<Level> dimensionKey = level.dimension();

        // Check if player entered ChronoDawn
        if (!dimensionKey.equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        ChronoDawn.LOGGER.info("Player {} entered ChronoDawn at position {}",
            player.getName().getString(), player.blockPosition());

        // Give Chronicle Book to player on first entry to ChronoDawn dimension
        giveChronicleBook(player);

        // Place Time Keeper Village near spawn if not already placed
        // This ensures players can find a Time Keeper for trading and Time Compass acquisition
        TimeKeeperVillagePlacer.onPlayerEnterChronoDawn(player);

        // Mark global state: ChronoDawn has been entered
        ChronoDawnGlobalState globalState = ChronoDawnGlobalState.get(player.server);
        globalState.markChronoDawnEntered();

        // If portals are stable, do not deactivate portal
        // This allows free bidirectional travel after stabilization
        if (!globalState.arePortalsUnstable()) {
            ChronoDawn.LOGGER.info("Portals are stable, skipping deactivation for player {}",
                player.getName().getString());
            return;  // Early return - portal remains active
        }

        // Find portal near player
        BlockPos playerPos = player.blockPosition();
        PortalStateMachine portal = findNearbyPortal(level, playerPos);

        if (portal != null) {
            // Check if portal is already registered and stabilized
            if (portal.getCurrentState() == PortalState.STABILIZED) {
                ChronoDawn.LOGGER.info("Portal {} is already stabilized, skipping deactivation", portal.getPortalId());
                return;
            }

            // Deactivate portal
            if (portal.deactivate()) {
                // Remove portal blocks
                extinguishPortal(level, playerPos);
                ChronoDawn.LOGGER.info("Deactivated portal {} after player entry", portal.getPortalId());
            }
        } else {
            // Register new portal as deactivated
            UUID portalId = UUID.randomUUID();
            portal = new PortalStateMachine(portalId, dimensionKey, playerPos);
            portal.setState(PortalState.DEACTIVATED);
            PortalRegistry.getInstance().registerPortal(portal);

            // Remove portal blocks
            extinguishPortal(level, playerPos);
            ChronoDawn.LOGGER.info("Registered and deactivated new portal {} at {}", portalId, playerPos);
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

        ChronoDawn.LOGGER.info("Searching for portal blocks near {} in dimension {}",
            centerPos, level.dimension().location());

        // Search for portal blocks in a larger 30x30x30 area
        for (int x = -15; x <= 15; x++) {
            for (int y = -15; y <= 15; y++) {
                for (int z = -15; z <= 15; z++) {
                    BlockPos searchPos = centerPos.offset(x, y, z);
                    var blockState = level.getBlockState(searchPos);

                    // Check for Custom Portal API's custom portal block
                    // Supports both Fabric (customportalapi) and NeoForge (cpapireforged) versions
                    Block block = blockState.getBlock();
                    ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

                    // Debug: Log any non-air blocks that might be portal-related
                    if (!blockState.isAir() && (blockId.getPath().contains("portal") ||
                        blockId.getNamespace().contains("portal") ||
                        blockId.getNamespace().contains("cpapi"))) {
                        ChronoDawn.LOGGER.info("Found potential portal block: {} at {}", blockId, searchPos);
                    }

                    if (isCustomPortalBlock(blockId)) {
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
                        // Supports both Fabric (customportalapi) and NeoForge (cpapireforged) versions
                        if (isCustomPortalBlock(blockId)) {
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

        ChronoDawn.LOGGER.info("Extinguished {} portal blocks near {}", removedBlocks, centerPos);
    }

    /**
     * Check if the given block ID is a Custom Portal API portal block.
     * Supports both Fabric (customportalapi) and NeoForge (cpapireforged) versions.
     *
     * @param blockId Block resource location
     * @return true if block is a Custom Portal API portal block
     */
    private static boolean isCustomPortalBlock(ResourceLocation blockId) {
        String namespace = blockId.getNamespace();
        String path = blockId.getPath();

        // Fabric version: customportalapi:customportalblock
        if (namespace.equals("customportalapi") && path.equals("customportalblock")) {
            return true;
        }

        // NeoForge version (Custom Portal API Reforged): cpapireforged:custom_portal_block
        if (namespace.equals("cpapireforged") && path.equals("custom_portal_block")) {
            return true;
        }

        return false;
    }

    /**
     * Give Chronicle Book to player if they don't already have it.
     * Chronicle Book is the custom guidebook that replaces Patchouli.
     *
     * @param player Player to give the book to
     */
    private static void giveChronicleBook(ServerPlayer player) {
        // Check if player already has the chronicle book
        if (hasChronicleBook(player)) {
            ChronoDawn.LOGGER.info("Player {} already has Chronicle book, skipping",
                player.getName().getString());
            return;
        }

        try {
            // Create Chronicle Book item stack
            ItemStack bookStack = new ItemStack(ModItems.CHRONICLE_BOOK.get());

            // Give book to player
            if (!player.getInventory().add(bookStack)) {
                // Inventory full, drop at player's feet
                player.drop(bookStack, false);
                ChronoDawn.LOGGER.info("Player {} inventory full, dropped Chronicle book at their position",
                    player.getName().getString());
            } else {
                ChronoDawn.LOGGER.info("Gave Chronicle book to player {}", player.getName().getString());
            }
        } catch (Exception e) {
            ChronoDawn.LOGGER.error("Failed to give Chronicle book to player {}: {}",
                player.getName().getString(), e.getMessage(), e);
        }
    }

    /**
     * Check if player already has the Chronicle book in their inventory.
     *
     * @param player Player to check
     * @return true if player has the book
     */
    private static boolean hasChronicleBook(ServerPlayer player) {
        // Check all inventory slots
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.CHRONICLE_BOOK.get())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove Time Hourglass from player inventory.
     * Time Hourglass is a single-use item that should be consumed when entering ChronoDawn.
     *
     * @param player Player who entered ChronoDawn
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
                ChronoDawn.LOGGER.info("Removed {} Time Hourglass from slot {}", count, i);
            }
        }

        if (removedCount > 0) {
            ChronoDawn.LOGGER.info("Removed total {} Time Hourglass items from player {}'s inventory",
                removedCount, player.getName().getString());
        }
    }
}

