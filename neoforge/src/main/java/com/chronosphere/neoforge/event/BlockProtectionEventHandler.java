package com.chronosphere.neoforge.event;

import com.chronosphere.Chronosphere;
import com.chronosphere.worldgen.protection.BlockProtectionHandler;
import com.chronosphere.worldgen.protection.PermanentProtectionHandler;
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
 * Also prevents breaking permanently protected blocks (e.g., Master Clock walls).
 *
 * Features:
 * - Listens to BlockEvent.BreakEvent (block breaking)
 * - Listens to BlockEvent.EntityPlaceEvent (block placement)
 * - Checks if block position is protected (temporary or permanent)
 * - Cancels events and displays message in survival mode
 * - Allows creative mode players to bypass protection
 *
 * Implementation: T224 - Boss room protection system (NeoForge)
 *                T302 - Permanent Master Clock wall protection
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

        // Check if this block is protected (boss room or permanent)
        boolean isPermanentlyProtected = PermanentProtectionHandler.isProtected(level, event.getPos());
        boolean isBossRoomProtected = BlockProtectionHandler.isProtected(level, event.getPos());

        if (isPermanentlyProtected) {
            // Display warning message for permanent protection
            player.displayClientMessage(
                Component.translatable("message.chronosphere.permanent_protected"),
                true // action bar
            );

            Chronosphere.LOGGER.info("Blocked permanent protected block break at {} by {}", event.getPos(), player.getName().getString());

            // Cancel block break event
            event.setCanceled(true);
            return;
        }

        if (isBossRoomProtected) {
            // Display warning message for boss room protection
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

        // Check if this position is protected (boss room or permanent)
        boolean isPermanentlyProtected = PermanentProtectionHandler.isProtected(level, event.getPos());
        boolean isBossRoomProtected = BlockProtectionHandler.isProtected(level, event.getPos());

        if (isPermanentlyProtected) {
            // Display warning message for permanent protection
            player.displayClientMessage(
                Component.translatable("message.chronosphere.permanent_no_placement"),
                true // action bar
            );

            // Cancel block place event
            event.setCanceled(true);
            return;
        }

        if (isBossRoomProtected) {
            // Display warning message for boss room protection
            player.displayClientMessage(
                Component.translatable("message.chronosphere.boss_room_no_placement"),
                true // action bar
            );

            // Cancel block place event
            event.setCanceled(true);
        }
    }
}
