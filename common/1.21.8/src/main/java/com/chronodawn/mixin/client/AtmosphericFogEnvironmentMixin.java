package com.chronodawn.mixin.client;

import com.chronodawn.core.dimension.ChronoDawnBiomeProvider;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side mixin to apply dense fog inside the ChronoDawn Dark Forest biome.
 *
 * 1.21.6 reworked the fog pipeline: {@code FogRenderer.setupFog} no longer
 * returns {@code FogParameters}, and per-environment subclasses of
 * {@code FogEnvironment} mutate a shared {@link FogData} struct instead.
 * We hook {@link AtmosphericFogEnvironment#setupFog} (the air/atmosphere case)
 * and tighten the resulting start/end values when standing in the biome.
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
        Entity entity,
        BlockPos pos,
        ClientLevel level,
        float viewDistance,
        DeltaTracker deltaTracker,
        CallbackInfo ci
    ) {
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
