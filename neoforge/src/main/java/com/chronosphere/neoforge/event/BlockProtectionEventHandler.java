package com.chronosphere.neoforge.event;

import com.chronosphere.Chronosphere;
import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Block Protection Event Handler (NeoForge)
 *
 * Prevents players from breaking or placing blocks in protected boss rooms until the boss is defeated.
 *
 * Features:
 * - Listens to BlockEvent.BreakEvent (block breaking)
 * - Listens to BlockEvent.EntityPlaceEvent (block placement)
 * - Checks if block position is protected
 * - Cancels events and displays message in survival mode
 * - Allows creative mode players to bypass protection
 *
 * Implementation: T224 - Boss room protection system (NeoForge)
 */
@EventBusSubscriber(modid = Chronosphere.MOD_ID)
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

        // Check if this block is protected
        if (BlockProtectionHandler.isProtected(level, event.getPos())) {
            // Display warning message
            player.displayClientMessage(
                Component.translatable("message.chronosphere.boss_room_protected"),
                true // action bar
            );

            // Cancel block break event
            event.setCanceled(true);
        }
    }

    /**
     * Handle block place event.
     * Prevents placing blocks in protected boss rooms.
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        // Only check player-placed blocks
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Level level = (Level) event.getLevel();

        // Allow creative mode players to place anything
        if (player.isCreative()) {
            return;
        }

        // Check if this position is protected
        if (BlockProtectionHandler.isProtected(level, event.getPos())) {
            // Display warning message
            player.displayClientMessage(
                Component.translatable("message.chronosphere.boss_room_no_placement"),
                true // action bar
            );

            // Cancel block place event
            event.setCanceled(true);
        }
    }
}
