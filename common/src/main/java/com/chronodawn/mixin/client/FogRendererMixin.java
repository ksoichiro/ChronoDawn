package com.chronodawn.mixin.client;

import com.chronodawn.core.dimension.ChronoDawnBiomeProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side mixin to modify fog density in the ChronoDawn Dark Forest biome.
 *
 * This mixin checks if the camera is inside the Dark Forest biome and applies
 * dense fog only when actually in the biome (not in surrounding areas).
 *
 * Fog parameters:
 * - Normal fog: start ~0, end ~viewDistance (usually 128-256 blocks)
 * - Dark Forest fog (inside biome only): start 5, end 60 blocks (very dense)
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    // Fog parameters for Dark Forest
    @Unique
    private static final float DARK_FOREST_FOG_START = 5.0f;
    @Unique
    private static final float DARK_FOREST_FOG_END = 60.0f;

    /**
     * Inject after fog setup to override fog distances when in Dark Forest biome.
     *
     * @param camera The camera (player viewpoint)
     * @param fogMode The fog rendering mode
     * @param viewDistance The current view distance
     * @param thickFog Whether thick fog is enabled (e.g., in water)
     * @param partialTick Partial tick for interpolation
     * @param ci Callback info
     */
    @Inject(
        method = "setupFog",
        at = @At("TAIL")
    )
    private static void modifyDarkForestFog(
        Camera camera,
        FogRenderer.FogMode fogMode,
        float viewDistance,
        boolean thickFog,
        float partialTick,
        CallbackInfo ci
    ) {
        // Only modify terrain fog, not sky fog
        if (fogMode != FogRenderer.FogMode.FOG_TERRAIN) {
            return;
        }

        // Skip if already in thick fog (water, lava, etc.)
        if (thickFog) {
            return;
        }

        // Get client level
        if (!(camera.getEntity().level() instanceof ClientLevel clientLevel)) {
            return;
        }

        BlockPos cameraPos = camera.getBlockPosition();
        Holder<Biome> currentBiome = clientLevel.getBiome(cameraPos);

        // Apply fog only if camera is actually inside Dark Forest biome
        if (currentBiome.is(ChronoDawnBiomeProvider.CHRONOSPHERE_DARK_FOREST)) {
            RenderSystem.setShaderFogStart(DARK_FOREST_FOG_START);
            RenderSystem.setShaderFogEnd(DARK_FOREST_FOG_END);
        }
    }
}
