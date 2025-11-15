package com.chronosphere.core.dimension;

import com.chronosphere.Chronosphere;
import com.chronosphere.data.ChronosphereGlobalState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Dimension Stabilizer - Handles dimension stabilization after Time Tyrant defeat.
 *
 * When Time Tyrant is defeated, the Chronosphere dimension becomes permanently stabilized:
 * - Time Tyrant cannot respawn
 * - Dimension is now safe for exploration
 * - Players receive a broadcast message
 *
 * This class manages the global state update for dimension stabilization.
 *
 * Reference: data-model.md (Time Tyrant Defeat Mechanics)
 * Task: T140 [US3] Implement dimension stabilization on defeat
 */
public class DimensionStabilizer {
    /**
     * Stabilize the Chronosphere dimension after Time Tyrant defeat.
     *
     * This method:
     * 1. Updates global state to mark Time Tyrant as defeated
     * 2. Broadcasts a message to all players
     * 3. Logs the stabilization event
     *
     * @param level The ServerLevel where Time Tyrant was defeated
     */
    public static void stabilizeDimension(ServerLevel level) {
        MinecraftServer server = level.getServer();

        // Update global state
        ChronosphereGlobalState globalState = ChronosphereGlobalState.get(server);
        globalState.markTyrantDefeated();

        Chronosphere.LOGGER.info("Chronosphere dimension stabilized after Time Tyrant defeat");

        // Broadcast message to all players
        Component message = Component.translatable("message.chronosphere.tyrant_defeated")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.displayClientMessage(message, false);

            // Send title overlay
            Component title = Component.translatable("message.chronosphere.tyrant_defeated_title")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            Component subtitle = Component.translatable("message.chronosphere.tyrant_defeated_subtitle")
                .withStyle(ChatFormatting.YELLOW);

            player.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(
                    10, // fade in
                    70, // stay
                    20  // fade out
                )
            );

            player.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title)
            );

            player.connection.send(
                new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle)
            );
        }
    }

    /**
     * Check if Time Tyrant has been defeated.
     *
     * @param server Minecraft server
     * @return true if Time Tyrant has been defeated
     */
    public static boolean isTyrantDefeated(MinecraftServer server) {
        ChronosphereGlobalState globalState = ChronosphereGlobalState.get(server);
        return globalState.isTyrantDefeated();
    }
}
