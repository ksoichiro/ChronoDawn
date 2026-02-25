package com.chronodawn.mixin.client;

import com.chronodawn.core.dimension.ChronoDawnBiomeProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
     * NOTE: 1.21.2 API changes:
     * - Added Vector4f fogColor parameter (RGBA fog color)
     * - Method now returns a value (CallbackInfoReturnable instead of CallbackInfo)
     * - RenderSystem.setShaderFogStart/End() methods removed
     *
     * @param camera The camera (player viewpoint)
     * @param fogMode The fog rendering mode
     * @param fogColor The fog color (RGBA, 0.0-1.0 per channel)
     * @param viewDistance The current view distance
     * @param thickFog Whether thick fog is enabled (e.g., in water)
     * @param partialTick Partial tick for interpolation
     * @param cir Callback info returnable
     */
    @Inject(
        method = "setupFog",
        at = @At("TAIL")
    )
    private static void modifyDarkForestFog(
        Camera camera,
        FogRenderer.FogMode fogMode,
        Vector4f fogColor,
        float viewDistance,
        boolean thickFog,
        float partialTick,
        CallbackInfoReturnable<?> cir
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
        if (currentBiome.is(ChronoDawnBiomeProvider.CHRONO_DAWN_DARK_FOREST)) {
            // TODO: 1.21.2 - Fog modification API not yet implemented
            //
            // PROBLEM: RenderSystem.setShaderFogStart/End() methods have been removed in 1.21.2
            // According to NeoForge migration primer, fog methods for individual values
            // have been replaced with a FogParameters data object.
            //
            // POSSIBLE SOLUTIONS (requires investigation):
            // 1. Return modified FogData/FogParameters via cir.setReturnValue()
            //    - Need to find FogData class structure in 1.21.2
            //    - Build new FogData with custom start/end values
            //
            // 2. Use @ModifyVariable or @Redirect to intercept FogData creation
            //    - Inject earlier in setupFog() method
            //    - Modify FogData before it's finalized
            //
            // 3. Use loader-specific fog events (NeoForge/Fabric)
            //    - NeoForge: FogEvent.FogDensity or similar
            //    - Fabric: May need custom event or different approach
            //
            // 4. Access FogData internals via Accessor mixin
            //    - Create FogDataAccessor to expose setStart()/setEnd()
            //    - Directly modify FogData after creation
            //
            // NEXT STEPS:
            // - Decompile FogRenderer.setupFog() in 1.21.2 to see FogData structure
            // - Check SimpleFogControl or IMB11/Fog mods for reference implementation
            // - Test with minimal fog modification to verify approach
            //
            // TEMPORARY: Feature disabled until proper API is found
            // Original implementation (1.21.1):
            //   RenderSystem.setShaderFogStart(DARK_FOREST_FOG_START);  // 5.0f
            //   RenderSystem.setShaderFogEnd(DARK_FOREST_FOG_END);      // 60.0f
        }
    }
}
