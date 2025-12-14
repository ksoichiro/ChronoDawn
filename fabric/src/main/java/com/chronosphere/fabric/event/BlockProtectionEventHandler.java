package com.chronosphere.fabric.event;

import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import com.chronosphere.worldgen.protection.PermanentProtectionHandler;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

/**
 * Block Protection Event Handler (Fabric)
 *
 * Prevents players from breaking or placing blocks in protected boss rooms until the boss is defeated.
 * Also prevents breaking permanently protected blocks (e.g., Master Clock walls).
 *
 * Features:
 * - Listens to PlayerBlockBreakEvents.BEFORE event (block breaking)
 * - Listens to UseBlockCallback.EVENT (block placement)
 * - Checks if block position is protected (temporary or permanent)
 * - Cancels events and displays message in survival mode
 * - Allows creative mode players to bypass protection
 *
 * Implementation: T224 - Boss room protection system (Fabric)
 *                T302 - Permanent Master Clock wall protection
 */
public class BlockProtectionEventHandler {
    /**
     * Register block protection event handlers.
     */
    public static void register() {
        // Prevent block breaking in protected areas
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            // Allow creative mode players to break anything
            if (player.isCreative()) {
                return true;
            }

            // Check if this block is protected (boss room or permanent)
            boolean isPermanentlyProtected = PermanentProtectionHandler.isProtected(level, pos);
            boolean isBossRoomProtected = BlockProtectionHandler.isProtected(level, pos);

            if (isPermanentlyProtected) {
                // Display warning message for permanent protection
                player.displayClientMessage(
                    Component.translatable("message.chronosphere.permanent_protected"),
                    true // action bar
                );

                com.chronosphere.Chronosphere.LOGGER.info("Blocked permanent protected block break at {} by {}", pos, player.getName().getString());

                // Cancel block break event
                return false;
            }

            if (isBossRoomProtected) {
                // Display warning message for boss room protection
                player.displayClientMessage(
                    Component.translatable("message.chronosphere.boss_room_protected"),
                    true // action bar
                );

                // Cancel block break event
                return false;
            }

            // Allow block break
            return true;
        });

        // Prevent block placement in protected areas
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            // Allow creative mode players to place anything
            if (player.isCreative()) {
                return InteractionResult.PASS;
            }

            // Only check block placement, not other interactions (door open, button press, etc.)
            // Check if player is holding a block item that could be placed
            var heldItem = player.getItemInHand(hand);
            if (heldItem.isEmpty() || !(heldItem.getItem() instanceof net.minecraft.world.item.BlockItem)) {
                // Not holding a block item - allow interaction (door, button, etc.)
                return InteractionResult.PASS;
            }

            // Check if the placement position is protected (boss room or permanent)
            var pos = hitResult.getBlockPos().relative(hitResult.getDirection());
            boolean isPermanentlyProtected = PermanentProtectionHandler.isProtected(level, pos);
            boolean isBossRoomProtected = BlockProtectionHandler.isProtected(level, pos);

            if (isPermanentlyProtected) {
                // Display warning message for permanent protection
                player.displayClientMessage(
                    Component.translatable("message.chronosphere.permanent_no_placement"),
                    true // action bar
                );

                // Cancel block placement
                return InteractionResult.FAIL;
            }

            if (isBossRoomProtected) {
                // Display warning message for boss room protection
                player.displayClientMessage(
                    Component.translatable("message.chronosphere.boss_room_no_placement"),
                    true // action bar
                );

                // Cancel block placement
                return InteractionResult.FAIL;
            }

            // Allow block placement
            return InteractionResult.PASS;
        });
    }
}
