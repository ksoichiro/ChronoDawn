package com.chronodawn.compat;

import com.chronodawn.ChronoDawn;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Compatibility helper for advancement operations (Minecraft 1.20.1).
 */
public class CompatAdvancementHelper {
    /**
     * Grant an advancement to a player if they don't already have it.
     *
     * @param server Minecraft server
     * @param player Player to grant advancement to
     * @param advancementId Advancement resource location
     * @return true if advancement was granted, false if already had it or not found
     */
    public static boolean grantAdvancement(MinecraftServer server, ServerPlayer player, ResourceLocation advancementId) {
        // Get advancement from server advancement manager
        Advancement advancement = server.getAdvancements().getAdvancement(advancementId);
        if (advancement == null) {
            ChronoDawn.LOGGER.warn("Advancement not found: {}", advancementId);
            return false;
        }

        // Check if player already has advancement
        var playerAdvancements = player.getAdvancements();
        var progress = playerAdvancements.getOrStartProgress(advancement);

        if (progress.isDone()) {
            return false; // Already has it
        }

        // Grant all criteria to complete the advancement
        for (String criterion : progress.getRemainingCriteria()) {
            playerAdvancements.award(advancement, criterion);
        }

        return true;
    }

    /**
     * Check if a player has completed an advancement.
     *
     * @param server Minecraft server
     * @param player Player to check
     * @param advancementId Advancement resource location
     * @return true if player has completed the advancement
     */
    public static boolean hasAdvancement(MinecraftServer server, ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = server.getAdvancements().getAdvancement(advancementId);
        if (advancement == null) {
            return false;
        }

        var playerAdvancements = player.getAdvancements();
        var progress = playerAdvancements.getOrStartProgress(advancement);

        return progress.isDone();
    }
}
