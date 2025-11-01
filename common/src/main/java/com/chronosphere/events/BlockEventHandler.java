package com.chronosphere.events;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.portal.PortalRegistry;
import com.chronosphere.core.portal.PortalState;
import com.chronosphere.core.portal.PortalStateMachine;
import com.chronosphere.data.ChronosphereGlobalState;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModDimensions;
import com.chronosphere.registry.ModItems;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Block event handler using Architectury Event API.
 *
 * This handler manages block-related events such as:
 * - Block break events (for drop doubling with Spatially Linked Pickaxe)
 * - Block placement events (for Reversing Time Sandstone restoration logic)
 * - Block interaction events (for portal activation)
 *
 * Implemented features:
 * - Reversing Time Sandstone restoration (3-second delay)
 *
 * TODO: Implement specific event handlers in future phases:
 * - Spatially Linked Pickaxe drop doubling logic
 * - Portal frame validation and activation
 *
 * Reference: data-model.md (Blocks, Portal System)
 */
public class BlockEventHandler {
    /**
     * Restoration data for pending block restorations.
     */
    private static class RestorationData {
        final ServerLevel level;
        final BlockState originalState;
        int ticksRemaining;

        RestorationData(ServerLevel level, BlockState originalState) {
            this.level = level;
            this.originalState = originalState;
            this.ticksRemaining = 60; // 3 seconds = 60 ticks
        }
    }

    /**
     * Map to track pending block restorations.
     * Key: BlockPos (immutable), Value: RestorationData
     */
    private static final Map<BlockPos, RestorationData> pendingRestorations = new HashMap<>();

    /**
     * Register block event listeners.
     */
    public static void register() {
        // Register block break event for Reversing Time Sandstone
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            // Check if the broken block is Reversing Time Sandstone
            if (state.is(ModBlocks.REVERSING_TIME_SANDSTONE.get())) {
                handleReversingTimeSandstoneBreak(level, pos, state);
            }
            return EventResult.pass();
        });

        // Register right-click block event for Time Hourglass warning
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            // Only process on server side
            if (player.level().isClientSide()) {
                return EventResult.pass();
            }

            // Check if player is using Time Hourglass
            if (!player.getItemInHand(hand).is(ModItems.TIME_HOURGLASS.get())) {
                return EventResult.pass();
            }

            // Only restrict in Chronosphere dimension
            if (!player.level().dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
                return EventResult.pass();
            }

            // Check if clicked block is Clockstone Block (portal frame)
            BlockState clickedBlock = player.level().getBlockState(pos);
            if (!clickedBlock.is(ModBlocks.CLOCKSTONE_BLOCK.get())) {
                return EventResult.pass();
            }

            // Check global state: are portals unstable?
            if (player.level() instanceof ServerLevel serverLevel) {
                ChronosphereGlobalState globalState = ChronosphereGlobalState.get(serverLevel.getServer());
                if (globalState.arePortalsUnstable()) {
                    // Portals are unstable - Time Hourglass cannot be used
                    player.displayClientMessage(
                        Component.translatable("item.chronosphere.time_hourglass.portal_deactivated"),
                        true
                    );
                    Chronosphere.LOGGER.info("Player {} attempted to ignite portal with Time Hourglass while portals are unstable",
                        player.getName().getString());
                }
            }

            // Let Custom Portal API process normally, but portal blocks will be removed by tick event
            return EventResult.pass();
        });

        // Register server tick event to remove portal blocks when portals are unstable
        TickEvent.SERVER_POST.register(server -> {
            // Check global state
            ChronosphereGlobalState globalState = ChronosphereGlobalState.get(server);
            if (!globalState.arePortalsUnstable()) {
                return; // Portals are stable, allow normal operation
            }

            // Portals are unstable - remove any portal blocks in Chronosphere
            ServerLevel chronosphereLevel = server.getLevel(ModDimensions.CHRONOSPHERE_DIMENSION);
            if (chronosphereLevel == null) {
                return;
            }

            // Check all loaded chunks for custom portal blocks
            // Note: This only checks near registered portals to avoid scanning entire dimension
            for (PortalStateMachine portal : PortalRegistry.getInstance().getAllPortals()) {
                if (!portal.getSourceDimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
                    continue;
                }

                BlockPos portalPos = portal.getPosition();
                boolean foundAndRemovedBlocks = false;

                for (int x = -10; x <= 10; x++) {
                    for (int y = -10; y <= 10; y++) {
                        for (int z = -10; z <= 10; z++) {
                            BlockPos checkPos = portalPos.offset(x, y, z);
                            BlockState state = chronosphereLevel.getBlockState(checkPos);
                            var block = state.getBlock();
                            var blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);

                            if (blockId.getNamespace().equals("customportalapi") && blockId.getPath().equals("customportalblock")) {
                                chronosphereLevel.removeBlock(checkPos, false);
                                foundAndRemovedBlocks = true;
                            }
                        }
                    }
                }

                if (foundAndRemovedBlocks) {
                    Chronosphere.LOGGER.debug("Removed unstable portal blocks near {}", portalPos);
                }
            }
        });

        // Register server tick event to process restoration timers
        TickEvent.SERVER_LEVEL_POST.register(level -> {
            processRestorationTimers(level);
        });

        Chronosphere.LOGGER.info("Registered BlockEventHandler with Reversing Time Sandstone restoration and Time Hourglass control");
    }

    /**
     * Handle Reversing Time Sandstone block break event.
     * Schedules the block to be restored after 3 seconds (60 ticks).
     *
     * @param level The level where the block was broken
     * @param pos The position of the broken block
     * @param state The state of the broken block
     */
    private static void handleReversingTimeSandstoneBreak(net.minecraft.world.level.Level level, BlockPos pos, BlockState state) {
        // Only process on server side
        if (level.isClientSide()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos immutablePos = pos.immutable();

        // Record the position and state for restoration
        pendingRestorations.put(immutablePos, new RestorationData(serverLevel, state));

        Chronosphere.LOGGER.info("Scheduled Reversing Time Sandstone restoration at {} in 3 seconds", immutablePos);
    }

    /**
     * Process restoration timers for all pending block restorations.
     * Called every server tick.
     *
     * @param level The server level (not used directly, but kept for API consistency)
     */
    private static void processRestorationTimers(ServerLevel level) {
        if (pendingRestorations.isEmpty()) {
            return;
        }

        // Create a list of positions to restore (to avoid concurrent modification)
        var toRestore = new HashMap<BlockPos, RestorationData>();

        // Iterate through pending restorations and decrement timers
        pendingRestorations.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            RestorationData data = entry.getValue();

            data.ticksRemaining--;

            if (data.ticksRemaining <= 0) {
                toRestore.put(pos, data);
                return true; // Remove from pending
            }

            return false; // Keep in pending
        });

        // Restore blocks
        for (var entry : toRestore.entrySet()) {
            restoreBlock(entry.getValue().level, entry.getKey(), entry.getValue().originalState);
        }
    }

    /**
     * Restore the Reversing Time Sandstone block at the specified position.
     * If another block has been placed at that position, it will be destroyed first.
     *
     * @param level The server level
     * @param pos The position to restore
     * @param originalState The original block state to restore
     */
    private static void restoreBlock(ServerLevel level, BlockPos pos, BlockState originalState) {
        // Get the current block at the position
        BlockState currentState = level.getBlockState(pos);

        // If another block has been placed, destroy it (no drops)
        if (!currentState.isAir() && !currentState.is(ModBlocks.REVERSING_TIME_SANDSTONE.get())) {
            level.destroyBlock(pos, false); // false = no drops
        }

        // Restore the original block
        level.setBlock(pos, originalState, 3); // 3 = update clients + neighbors

        Chronosphere.LOGGER.info("Restored Reversing Time Sandstone at {}", pos);
    }

    /**
     * Find a portal near the clicked position.
     *
     * @param level Level
     * @param pos Clicked position
     * @return Portal state machine, or null if not found
     */
    private static PortalStateMachine findNearbyPortal(net.minecraft.world.level.Level level, BlockPos pos) {
        PortalRegistry registry = PortalRegistry.getInstance();

        // Search in a 10x10x10 area around the clicked position
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
}
