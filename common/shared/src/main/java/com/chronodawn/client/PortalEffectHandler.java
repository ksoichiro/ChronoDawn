package com.chronodawn.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Handles portal-related effects on the client side.
 *
 * This handler manages fade effect trigger:
 * - Detects dimension changes and triggers fade-in effect
 *
 * Should be called every client tick from the platform-specific tick event handlers.
 */
public class PortalEffectHandler {
    private static ResourceKey<Level> lastDimension = null;

    /**
     * Main tick handler called every client tick.
     * Manages nausea effect and dimension change detection.
     */
    public static void onClientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        // Tick fade handler
        PortalFadeHandler.tick();

        // Check for dimension change (for fade effect)
        checkDimensionChange(minecraft);
    }

    /**
     * Check if dimension has changed and trigger fade-in effect.
     *
     * @param minecraft Minecraft client instance
     */
    private static void checkDimensionChange(Minecraft minecraft) {
        if (minecraft.level == null) return;

        ResourceKey<Level> currentDim = minecraft.level.dimension();
        if (lastDimension != null && !lastDimension.equals(currentDim)) {
            // Dimension changed, trigger fade in
            PortalFadeHandler.triggerFadeIn();
        }
        lastDimension = currentDim;
    }
}
