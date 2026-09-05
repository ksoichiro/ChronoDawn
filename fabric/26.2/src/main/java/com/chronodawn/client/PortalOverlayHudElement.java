package com.chronodawn.client;

import com.chronodawn.registry.ModBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Renders the portal overlay and dimension-fade effects on the player's screen.
 *
 * 26.2: {@code Gui} moved to a two-phase render pipeline where
 * {@code extractRenderState(DeltaTracker, boolean, boolean)} no longer receives a
 * {@link GuiGraphicsExtractor} to draw with directly - actual drawing now happens in a
 * separate stage this mod can't hook via a plain Mixin injection. Fabric API's
 * {@link HudElement} still receives a drawing-capable {@link GuiGraphicsExtractor} and is
 * composited on top of the vanilla HUD by Fabric's own registry, so it replaces the
 * {@code GuiPortalOverlayMixin} approach used by every other supported version.
 */
public class PortalOverlayHudElement implements HudElement {

    private DeltaTracker chronodawn$deltaTracker;

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        chronodawn$deltaTracker = deltaTracker;
        chronodawn$renderPortalEffects(guiGraphics);
    }

    private void chronodawn$renderPortalEffects(GuiGraphicsExtractor guiGraphics) {
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
            int color = (int) (fadeAlpha * 255) << 24; // Black with alpha
            guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), color);
        }
    }

    /**
     * Calculate overlay alpha based on player's distance from portal center.
     * Returns constant alpha when in portal for consistent effect.
     */
    private float chronodawn$calculateOverlayAlpha(LocalPlayer player, BlockPos portalPos) {
        // Return constant alpha for consistent, visible effect
        return 0.6f; // 60% opacity - same as Nether portal
    }

    /**
     * Render portal overlay with orange/gold gradient from screen edges.
     * Color: RGB(219, 136, 19) = 0xDB8813 (same as ChronoDawnPortalBlock)
     */
    private void chronodawn$renderPortalOverlay(GuiGraphicsExtractor guiGraphics, float alpha) {
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
        float pulseAlpha = alpha * (0.8f + 0.2f * (float) Math.sin(time * 0.1f));
        int a = (int) (pulseAlpha * 255);
        int colorWithAlpha = (a << 24) | (r << 16) | (g << 8) | b;

        // Render semi-transparent overlay over entire screen
        guiGraphics.fill(0, 0, screenWidth, screenHeight, colorWithAlpha);
    }
}
