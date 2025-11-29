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
 * Prevents players from breaking blocks in protected boss rooms until the boss is defeated.
 *
 * Features:
 * - Listens to BlockEvent.BreakEvent
 * - Checks if block position is protected
 * - Cancels break event and displays message in survival mode
 * - Allows creative mode players to break protected blocks
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
}
