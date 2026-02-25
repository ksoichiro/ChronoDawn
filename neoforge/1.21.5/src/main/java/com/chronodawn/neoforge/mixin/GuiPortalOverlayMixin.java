package com.chronodawn.neoforge.mixin;

import com.chronodawn.client.PortalFadeHandler;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to render portal overlay and fade effects on the player's screen (Minecraft 1.21.2).
 *
 * This mixin injects into the Gui.render method to add two visual effects:
 * 1. Portal overlay: Orange/gold gradient from screen edges when inside ChronoDawn Portal
 * 2. Fade effect: Black fade-in when teleporting between dimensions
 */
@Mixin(Gui.class)
public class GuiPortalOverlayMixin {

    @Unique
    private DeltaTracker chronodawn$deltaTracker;

    /**
     * Inject at the end of Gui.render to add our custom portal effects.
     * Uses TAIL to ensure we render on top of all other GUI elements.
     * This version uses DeltaTracker parameter for 1.21.2 compatibility.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void renderChronoDawnPortalOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        chronodawn$deltaTracker = deltaTracker;
        chronodawn$renderPortalEffects(guiGraphics);
    }

    /**
     * Common portal effects rendering logic.
     */
    @Unique
    private void chronodawn$renderPortalEffects(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        // 1. Render portal overlay effect
        // Check multiple positions: feet, body center, and eyes
        BlockPos feetPos = player.blockPosition();
        BlockPos eyePos = BlockPos.containing(player.getEyePosition());

        BlockState feetState = minecraft.level.getBlockState(feetPos);
        BlockState eyeState = minecraft.level.getBlockState(eyePos);

        // Check if player is in portal at any position
        boolean inPortal = feetState.is(ModBlocks.CHRONO_DAWN_PORTAL.get()) ||
                          eyeState.is(ModBlocks.CHRONO_DAWN_PORTAL.get());

        if (inPortal) {
            BlockPos portalPos = feetState.is(ModBlocks.CHRONO_DAWN_PORTAL.get()) ? feetPos : eyePos;
            float overlayAlpha = chronodawn$calculateOverlayAlpha(player, portalPos);
            if (overlayAlpha > 0.0f) {
                chronodawn$renderPortalOverlay(guiGraphics, overlayAlpha);
            }
        }

        // 2. Render fade effect
        float fadeAlpha = PortalFadeHandler.getFadeAlpha();
        if (fadeAlpha > 0.0f) {
            int color = (int)(fadeAlpha * 255) << 24; // Black with alpha
            guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), color);
        }
    }

    /**
     * Calculate overlay alpha based on player's distance from portal center.
     * Returns constant alpha when in portal for consistent effect.
     */
    @Unique
    private float chronodawn$calculateOverlayAlpha(LocalPlayer player, BlockPos portalPos) {
        // Return constant alpha for consistent, visible effect
        return 0.6f; // 60% opacity - same as Nether portal
    }

    /**
     * Render portal overlay with orange/gold gradient from screen edges.
     * Color: RGB(219, 136, 19) = 0xDB8813 (same as ChronoDawnPortalBlock)
     */
    @Unique
    private void chronodawn$renderPortalOverlay(GuiGraphics guiGraphics, float alpha) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // Portal color: RGB(219, 136, 19) = 0xDB8813
        int r = 219;
        int g = 136;
        int b = 19;

        // Animate alpha using sine wave for "pulsing" effect
        float partialTick = chronodawn$deltaTracker != null ? chronodawn$deltaTracker.getGameTimeDeltaPartialTick(false) : 0.0f;
        float time = minecraft.level.getGameTime() + partialTick;
        float pulseAlpha = alpha * (0.8f + 0.2f * (float)Math.sin(time * 0.1f));
        int a = (int)(pulseAlpha * 255);
        int colorWithAlpha = (a << 24) | (r << 16) | (g << 8) | b;

        // Render semi-transparent overlay over entire screen
        guiGraphics.fill(0, 0, screenWidth, screenHeight, colorWithAlpha);
    }
}
