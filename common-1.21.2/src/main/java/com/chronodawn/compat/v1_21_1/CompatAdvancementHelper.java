package com.chronodawn.compat.v1_21_1;

import com.chronodawn.ChronoDawn;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Compatibility helper for advancement operations (Minecraft 1.21.1).
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
        AdvancementHolder advancementHolder = server.getAdvancements().get(advancementId);
        if (advancementHolder == null) {
            ChronoDawn.LOGGER.warn("Advancement not found: {}", advancementId);
            return false;
        }

        // Check if player already has advancement
        var playerAdvancements = player.getAdvancements();
        var progress = playerAdvancements.getOrStartProgress(advancementHolder);

        if (progress.isDone()) {
            return false; // Already has it
        }

        // Grant all criteria to complete the advancement
        for (String criterion : progress.getRemainingCriteria()) {
            playerAdvancements.award(advancementHolder, criterion);
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
        AdvancementHolder advancementHolder = server.getAdvancements().get(advancementId);
        if (advancementHolder == null) {
            return false;
        }

        var playerAdvancements = player.getAdvancements();
        var progress = playerAdvancements.getOrStartProgress(advancementHolder);

        return progress.isDone();
    }
}
