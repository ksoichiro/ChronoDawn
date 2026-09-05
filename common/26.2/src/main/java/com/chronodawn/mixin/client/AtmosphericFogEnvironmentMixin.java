package com.chronodawn.mixin.client;

import com.chronodawn.core.dimension.ChronoDawnBiomeProvider;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side mixin to apply dense fog inside the ChronoDawn Dark Forest biome.
 *
 * 1.21.11 dropped the (Entity, BlockPos) pair from the per-environment
 * {@code setupFog} signature in favour of {@link Camera}, so this version
 * derives the block position from the camera. Otherwise the override logic
 * is identical to 1.21.6-1.21.10.
 */
@Mixin(AtmosphericFogEnvironment.class)
public class AtmosphericFogEnvironmentMixin {

    @Unique
    private static final float DARK_FOREST_FOG_START = 5.0f;
    @Unique
    private static final float DARK_FOREST_FOG_END = 60.0f;

    @Inject(
        method = "setupFog",
        at = @At("TAIL")
    )
    private void chronodawn$applyDarkForestFog(
        FogData fogData,
        Camera camera,
        ClientLevel level,
        float viewDistance,
        DeltaTracker deltaTracker,
        CallbackInfo ci
    ) {
        BlockPos pos = camera.blockPosition();
        Holder<Biome> biome = level.getBiome(pos);
        if (!biome.is(ChronoDawnBiomeProvider.CHRONO_DAWN_DARK_FOREST)) {
            return;
        }

        fogData.environmentalStart = Math.min(fogData.environmentalStart, DARK_FOREST_FOG_START);
        fogData.environmentalEnd = Math.min(fogData.environmentalEnd, DARK_FOREST_FOG_END);
        fogData.renderDistanceStart = Math.min(fogData.renderDistanceStart, DARK_FOREST_FOG_START);
        fogData.renderDistanceEnd = Math.min(fogData.renderDistanceEnd, DARK_FOREST_FOG_END);
    }
}
