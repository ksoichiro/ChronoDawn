package com.chronodawn.mixin.client;

import com.chronodawn.registry.ModDimensions;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.resources.Identifier;
import com.chronodawn.compat.CompatResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side mixin to modify sky color in ChronoDawn dimension.
 *
 * 1.21.11: Biome.getSkyColor() was removed; sky color is now resolved through
 * EnvironmentAttributeProbe and set in SkyRenderer.extractRenderState().
 * This mixin intercepts extractRenderState() at TAIL to modify the skyColor
 * field when the Time Tyrant has been defeated.
 */
@Mixin(SkyRenderer.class)
public class SkyColorMixin {

    // Default grey sky color (before Time Tyrant defeat)
    // 1.21.11: EnvironmentAttributes includes 0xFF alpha prefix
    @Unique
    private static final int DEFAULT_SKY_COLOR = 0xFF909090;

    // Bright blue sky color (after Time Tyrant defeat)
    @Unique
    private static final int BRIGHT_SKY_COLOR = 0xFF5588DD;

    // Advancement ID for Time Tyrant defeat
    @Unique
    private static final Identifier TIME_TYRANT_DEFEATED_ADVANCEMENT =
        CompatResourceLocation.create("chronodawn", "story/us3/time_tyrant_defeat");

    /**
     * Inject at the end of extractRenderState to modify skyColor after it's been set.
     */
    @Inject(
        method = "extractRenderState",
        at = @At("TAIL")
    )
    private void modifyChronoDawnSkyColor(ClientLevel level, float partialTick,
                                           Camera camera, SkyRenderState state,
                                           CallbackInfo ci) {
        // Only apply to ChronoDawn dimension
        if (!level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        // Only modify if it's the default grey color
        if (state.skyColor != DEFAULT_SKY_COLOR) {
            return;
        }

        // Check if Time Tyrant has been defeated
        if (hasTimeTyrantBeenDefeated()) {
            state.skyColor = BRIGHT_SKY_COLOR;
        }
    }

    @Unique
    private static boolean hasTimeTyrantBeenDefeated() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return false;
        }

        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            return false;
        }

        var advancementManager = connection.getAdvancements();

        var holder = advancementManager.get(TIME_TYRANT_DEFEATED_ADVANCEMENT);
        if (holder == null) {
            return false;
        }

        var progressAccessor = (ClientAdvancementsAccessor) advancementManager;
        var progressMap = progressAccessor.chronodawn$getProgress();

        if (progressMap == null) {
            return false;
        }

        var progress = progressMap.get(holder);
        if (progress == null) {
            return false;
        }

        return progress.isDone();
    }
}
