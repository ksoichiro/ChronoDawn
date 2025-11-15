package com.chronosphere.events;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.portal.PortalRegistry;
import com.chronosphere.core.portal.PortalState;
import com.chronosphere.core.portal.PortalStateMachine;
import com.chronosphere.data.ChronosphereGlobalState;
import com.chronosphere.items.tools.SpatiallyLinkedPickaxeItem;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
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
        // Register block break event for Reversing Time Sandstone and Spatially Linked Pickaxe
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            // Check if the broken block is Reversing Time Sandstone
            if (state.is(ModBlocks.REVERSING_TIME_SANDSTONE.get())) {
                handleReversingTimeSandstoneBreak(level, pos, state);
            }

            // Check if player is using Spatially Linked Pickaxe for drop doubling
            if (player != null && !level.isClientSide()) {
                handleSpatiallyLinkedPickaxeDropDoubling(level, pos, state, player);
            }

            return EventResult.pass();
        });

        // Time Hourglass portal ignition handling (including unstable portal checks and item consumption)
        // is now handled entirely by CustomPortalFabric.registerPreIgniteEvent() and registerIgniteEvent()

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

        // Register block interaction event for Master Clock door unlocking
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            // Only process main hand interactions on server side
            if (player.level().isClientSide() || hand != net.minecraft.world.InteractionHand.MAIN_HAND) {
                return EventResult.pass();
            }

            BlockState state = player.level().getBlockState(pos);

            // Check if the clicked block is a boss room door (custom door with BlockEntity)
            if (state.is(ModBlocks.BOSS_ROOM_DOOR.get())) {
                // Determine door type by reading BlockEntity NBT
                boolean isBossRoomDoor = isBossRoomDoor(player.level(), pos);

                // Check appropriate key requirement
                boolean canUnlock;
                Component message;

                if (isBossRoomDoor) {
                    // Boss room door - requires 3 Ancient Gears
                    canUnlock = hasRequiredAncientGears(player);
                    message = canUnlock
                        ? Component.translatable("message.chronosphere.boss_room_unlocked")
                        : Component.translatable("message.chronosphere.boss_room_locked");
                } else {
                    // Entrance door - requires Key to Master Clock
                    canUnlock = hasKeyToMasterClock(player);
                    message = canUnlock
                        ? Component.translatable("message.chronosphere.master_clock_unlocked")
                        : Component.translatable("message.chronosphere.master_clock_locked");
                }

                if (canUnlock) {
                    // Toggle door state
                    boolean isOpen = state.getValue(net.minecraft.world.level.block.DoorBlock.OPEN);
                    boolean wasOpen = isOpen; // Store old state for spawning logic
                    BlockState newState = state.setValue(net.minecraft.world.level.block.DoorBlock.OPEN, !isOpen);

                    // Update both halves of the door
                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf half =
                        state.getValue(net.minecraft.world.level.block.DoorBlock.HALF);

                    BlockPos doorPosToUse = pos; // Track which position we'll use for spawning

                    if (half == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) {
                        // Clicked lower half - update both
                        player.level().setBlock(pos, newState, 3);
                        doorPosToUse = pos;
                        BlockPos upperPos = pos.above();
                        BlockState upperState = player.level().getBlockState(upperPos);
                        if (upperState.getBlock() == ModBlocks.BOSS_ROOM_DOOR.get()) {
                            player.level().setBlock(upperPos, upperState.setValue(net.minecraft.world.level.block.DoorBlock.OPEN, !isOpen), 3);
                        }
                    } else {
                        // Clicked upper half - update both
                        player.level().setBlock(pos, newState, 3);
                        BlockPos lowerPos = pos.below();
                        doorPosToUse = lowerPos; // Use lower position for spawning
                        BlockState lowerState = player.level().getBlockState(lowerPos);
                        if (lowerState.getBlock() == ModBlocks.BOSS_ROOM_DOOR.get()) {
                            player.level().setBlock(lowerPos, lowerState.setValue(net.minecraft.world.level.block.DoorBlock.OPEN, !isOpen), 3);
                        }
                    }

                    // Play door sound
                    player.level().levelEvent(player, isOpen ? 1011 : 1005, pos, 0);

                    // Send message to player (only when opening)
                    if (!isOpen) {
                        player.displayClientMessage(message, true);
                    }

                    // Spawn Time Tyrant when boss room door is opened (not closed)
                    if (isBossRoomDoor && !wasOpen && player.level() instanceof ServerLevel serverLevel) {
                        Chronosphere.LOGGER.info("Boss room door opened at {} - spawning Time Tyrant", doorPosToUse);
                        // Get the updated state after opening
                        BlockState openedState = player.level().getBlockState(doorPosToUse);
                        com.chronosphere.worldgen.spawning.TimeTyrantSpawner.spawnOnDoorOpen(serverLevel, doorPosToUse, openedState);
                    }

                    return EventResult.interruptTrue();
                } else {
                    // Player doesn't have the required key/items
                    player.displayClientMessage(message, true);
                    return EventResult.interruptFalse();
                }
            }

            return EventResult.pass();
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
     * Handle Spatially Linked Pickaxe drop doubling logic.
     * When player breaks a block with Spatially Linked Pickaxe, has 33% chance to double drops.
     *
     * Implementation:
     * - Check if player's main hand item is Spatially Linked Pickaxe
     * - Roll 33% random chance
     * - If successful, generate additional drops from block's loot table
     * - Spawn additional items at block position
     *
     * @param level The level where the block was broken
     * @param pos The position of the broken block
     * @param state The state of the broken block
     * @param player The player who broke the block
     */
    private static void handleSpatiallyLinkedPickaxeDropDoubling(
            net.minecraft.world.level.Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        // Only process on server side
        if (level.isClientSide()) {
            return;
        }

        // Check if player is holding Spatially Linked Pickaxe in main hand
        ItemStack mainHandItem = player.getMainHandItem();
        if (!SpatiallyLinkedPickaxeItem.isSpatiallyLinkedPickaxe(mainHandItem.getItem())) {
            return;
        }

        // Roll 33% chance for drop doubling
        if (level.random.nextDouble() >= SpatiallyLinkedPickaxeItem.getDropDoublingChance()) {
            return; // No doubling this time
        }

        // Generate additional drops using block's loot table
        ServerLevel serverLevel = (ServerLevel) level;

        // Get block entity if present (for chests, etc.)
        BlockEntity blockEntity = level.getBlockEntity(pos);

        // Build loot context
        LootParams.Builder lootParamsBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, mainHandItem)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);

        // Add player parameter if player is ServerPlayer
        if (player instanceof ServerPlayer serverPlayer) {
            lootParamsBuilder.withParameter(LootContextParams.THIS_ENTITY, serverPlayer);
        }

        // Get drops from loot table
        List<ItemStack> drops = state.getDrops(lootParamsBuilder);

        // Spawn additional drops
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(
                        serverLevel,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        drop.copy()
                );
                itemEntity.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(itemEntity);
            }
        }

        Chronosphere.LOGGER.debug("Spatially Linked Pickaxe doubled drops for block at {} (player: {})",
                pos, player.getName().getString());
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

    /**
     * Check if a player has the required number of Ancient Gears in their inventory.
     * This method is used to determine if a player can unlock the Master Clock boss room.
     *
     * Implementation:
     * - Count Ancient Gear items in player's inventory
     * - Return true if count >= AncientGearItem.REQUIRED_COUNT (3)
     *
     * @param player The player to check
     * @return true if player has 3 or more Ancient Gears, false otherwise
     */
    public static boolean hasRequiredAncientGears(net.minecraft.world.entity.player.Player player) {
        if (player == null) {
            return false;
        }

        int gearCount = 0;

        // Count Ancient Gears in player's inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.ANCIENT_GEAR.get())) {
                gearCount += stack.getCount();
            }
        }

        return gearCount >= com.chronosphere.items.quest.AncientGearItem.REQUIRED_COUNT;
    }

    /**
     * Check if a door is a boss room door by reading its BlockEntity NBT data.
     *
     * Door types are stored in BlockEntity NBT and set in structure files:
     * - Boss room door: {DoorType: "boss_room"} - requires 3x Ancient Gears
     * - Entrance door: {DoorType: "entrance"} - requires Key to Master Clock
     *
     * @param level The level
     * @param pos The position of the door
     * @return true if this is a boss room door, false otherwise
     */
    private static boolean isBossRoomDoor(net.minecraft.world.level.Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof com.chronosphere.blocks.BossRoomDoorBlockEntity doorEntity) {
            return doorEntity.isBossRoomDoor();
        }

        // Default to entrance door if BlockEntity not found
        return false;
    }

    /**
     * Check if a player has the Key to Master Clock in their inventory.
     * This method is used to determine if a player can unlock the Master Clock entrance door.
     *
     * Implementation:
     * - Search for Key to Master Clock item in player's inventory
     * - Return true if found (stack size is always 1)
     *
     * @param player The player to check
     * @return true if player has Key to Master Clock, false otherwise
     */
    public static boolean hasKeyToMasterClock(net.minecraft.world.entity.player.Player player) {
        if (player == null) {
            return false;
        }

        // Search for Key to Master Clock in player's inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.KEY_TO_MASTER_CLOCK.get())) {
                return true; // Found the key
            }
        }

        return false; // Key not found
    }
}
