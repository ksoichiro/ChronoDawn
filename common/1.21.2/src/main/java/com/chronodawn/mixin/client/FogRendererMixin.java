package com.chronodawn.mixin.client;

import com.chronodawn.core.dimension.ChronoDawnBiomeProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogParameters;
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
 * 1.21.2 changed the API: {@code FogRenderer.setupFog} now returns a
 * {@link FogParameters} record instead of mutating GL state via
 * {@code RenderSystem.setShaderFogStart/End}. We override the return value
 * with a copy that keeps the vanilla color/shape/alpha but tightens
 * start/end to produce dense fog inside the biome.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    @Unique
    private static final float DARK_FOREST_FOG_START = 5.0f;
    @Unique
    private static final float DARK_FOREST_FOG_END = 60.0f;

    @Inject(
        method = "setupFog",
        at = @At("TAIL"),
        cancellable = true
    )
    private static void modifyDarkForestFog(
        Camera camera,
        FogRenderer.FogMode fogMode,
        Vector4f fogColor,
        float viewDistance,
        boolean thickFog,
        float partialTick,
        CallbackInfoReturnable<FogParameters> cir
    ) {
        if (fogMode != FogRenderer.FogMode.FOG_TERRAIN) {
            return;
        }
        if (thickFog) {
            return;
        }
        if (!(camera.getEntity().level() instanceof ClientLevel clientLevel)) {
            return;
        }

        BlockPos cameraPos = camera.getBlockPosition();
        Holder<Biome> currentBiome = clientLevel.getBiome(cameraPos);

        if (!currentBiome.is(ChronoDawnBiomeProvider.CHRONO_DAWN_DARK_FOREST)) {
            return;
        }

        FogParameters original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        // Use Math.min so vanilla mob-effect fog (blindness/darkness) inside
        // the biome stays denser than the biome override instead of being widened.
        cir.setReturnValue(new FogParameters(
            Math.min(original.start(), DARK_FOREST_FOG_START),
            Math.min(original.end(), DARK_FOREST_FOG_END),
            original.shape(),
            original.red(),
            original.green(),
            original.blue(),
            original.alpha()
        ));
    }
}
