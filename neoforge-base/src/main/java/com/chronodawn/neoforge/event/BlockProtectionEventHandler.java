package com.chronodawn.neoforge.event;

import com.chronodawn.ChronoDawn;
import com.chronodawn.worldgen.protection.BlockProtectionHandler;
import com.chronodawn.worldgen.protection.PermanentProtectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Block Protection Event Handler (NeoForge)
 *
 * Prevents players from breaking or placing blocks in protected boss rooms until the boss is defeated.
 * Also prevents breaking permanently protected blocks (e.g., Master Clock walls).
 *
 * Features:
 * - Listens to BlockEvent.BreakEvent (block breaking)
 * - Listens to PlayerInteractEvent.RightClickBlock (block placement)
 * - Checks if block position is protected (temporary or permanent)
 * - Cancels events and displays message in survival mode
 * - Allows creative mode players to bypass protection
 *
 * Implementation: T224 - Boss room protection system (NeoForge)
 *                T302 - Permanent Master Clock wall protection
 *                T720 - Fixed NeoForge block placement item loss
 */
@EventBusSubscriber(modid = ChronoDawn.MOD_ID)
public class BlockProtectionEventHandler {
    /**
     * Handle block break event.
     * Prevents breaking blocks in protected boss rooms.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();

        // Allow creative mode players to break anything
        if (player.isCreative()) {
            return;
        }

        // Check if this block is protected (boss room or permanent)
        boolean isPermanentlyProtected = PermanentProtectionHandler.isProtected(level, event.getPos());
        boolean isBossRoomProtected = BlockProtectionHandler.isProtected(level, event.getPos());

        if (isPermanentlyProtected) {
            // Display warning message for permanent protection
            player.displayClientMessage(
                Component.translatable("message.chronodawn.permanent_protected"),
                true // action bar
            );

            ChronoDawn.LOGGER.debug("Blocked permanent protected block break at {} by {}", event.getPos(), player.getName().getString());

            // Cancel block break event
            event.setCanceled(true);
            return;
        }

        if (isBossRoomProtected) {
            // Display warning message for boss room protection
            player.displayClientMessage(
                Component.translatable("message.chronodawn.boss_room_protected"),
                true // action bar
            );

            // Cancel block break event
            event.setCanceled(true);
        }
    }

    /**
     * Handle right-click block interaction event.
     * Prevents placing blocks in protected boss rooms BEFORE item consumption.
     *
     * This event fires BEFORE the block is placed, allowing us to cancel the action
     * without consuming the item from the player's inventory.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();

        // Allow creative mode players to place anything
        if (player.isCreative()) {
            return;
        }

        // Only check block placement, not other interactions (door open, button press, etc.)
        // Check if player is holding a block item that could be placed
        var heldItem = player.getItemInHand(event.getHand());
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof BlockItem)) {
            // Not holding a block item - allow interaction (door, button, etc.)
            return;
        }

        // Calculate the placement position (clicked block + face direction)
        var pos = event.getPos().relative(event.getFace());

        // Check if this position is protected (boss room or permanent)
        boolean isPermanentlyProtected = PermanentProtectionHandler.isProtected(level, pos);
        boolean isBossRoomProtected = BlockProtectionHandler.isProtected(level, pos);

        if (isPermanentlyProtected) {
            // Display warning message for permanent protection
            player.displayClientMessage(
                Component.translatable("message.chronodawn.permanent_no_placement"),
                true // action bar
            );

            ChronoDawn.LOGGER.debug("Blocked permanent protected block placement at {} by {}", pos, player.getName().getString());

            // Cancel block placement (FAIL = cancel without consuming item)
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            return;
        }

        if (isBossRoomProtected) {
            // Display warning message for boss room protection
            player.displayClientMessage(
                Component.translatable("message.chronodawn.boss_room_no_placement"),
                true // action bar
            );

            ChronoDawn.LOGGER.debug("Blocked boss room protected block placement at {} by {}", pos, player.getName().getString());

            // Cancel block placement (FAIL = cancel without consuming item)
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }
}
