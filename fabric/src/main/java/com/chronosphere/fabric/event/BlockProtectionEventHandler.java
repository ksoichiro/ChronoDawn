package com.chronosphere.fabric.event;

import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

/**
 * Block Protection Event Handler (Fabric)
 *
 * Prevents players from breaking or placing blocks in protected boss rooms until the boss is defeated.
 *
 * Features:
 * - Listens to PlayerBlockBreakEvents.BEFORE event (block breaking)
 * - Listens to UseBlockCallback.EVENT (block placement)
 * - Checks if block position is protected
 * - Cancels events and displays message in survival mode
 * - Allows creative mode players to bypass protection
 *
 * Implementation: T224 - Boss room protection system (Fabric)
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

            // Check if this block is protected
            if (BlockProtectionHandler.isProtected(level, pos)) {
                // Display warning message
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

            // Check if the placement position is protected
            var pos = hitResult.getBlockPos().relative(hitResult.getDirection());
            if (BlockProtectionHandler.isProtected(level, pos)) {
                // Display warning message
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
