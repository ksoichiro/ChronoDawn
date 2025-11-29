package com.chronosphere.fabric.event;

import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Block Protection Event Handler (Fabric)
 *
 * Prevents players from breaking blocks in protected boss rooms until the boss is defeated.
 *
 * Features:
 * - Listens to PlayerBlockBreakEvents.BEFORE event
 * - Checks if block position is protected
 * - Cancels break event and displays message in survival mode
 * - Allows creative mode players to break protected blocks
 *
 * Implementation: T224 - Boss room protection system (Fabric)
 */
public class BlockProtectionEventHandler {
    /**
     * Register block protection event handler.
     */
    public static void register() {
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
    }
}
