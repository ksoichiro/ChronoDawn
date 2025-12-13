package com.chronosphere.mixin;

import com.chronosphere.Chronosphere;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to scale biome size in Chronosphere dimension.
 *
 * Reduces biome size by scaling noise coordinates, making it easier
 * to explore different biomes without excessive travel.
 *
 * Task: T299 [US1] Adjust biome size/scale to reduce biome area
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class BiomeScalingMixin {

    /**
     * Biome scale factor for Chronosphere dimension.
     * 2.0 = biomes are approximately half the size
     * 3.0 = biomes are approximately 1/3 the size
     */
    private static final double CHRONOSPHERE_BIOME_SCALE = 2.5;

    /**
     * Scale noise coordinates for Chronosphere dimension to reduce biome size.
     *
     * This mixin intercepts the x coordinate in getNoiseBiome() and scales it
     * when the biome source belongs to Chronosphere dimension.
     */
    @ModifyVariable(
        method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true
    )
    private int scaleX(int x) {
        if (this.isChronosphereBiomeSource()) {
            return (int) (x * CHRONOSPHERE_BIOME_SCALE);
        }
        return x;
    }

    /**
     * Scale noise coordinates for Chronosphere dimension to reduce biome size.
     */
    @ModifyVariable(
        method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
        at = @At("HEAD"),
        ordinal = 1,
        argsOnly = true
    )
    private int scaleY(int y) {
        if (this.isChronosphereBiomeSource()) {
            return (int) (y * CHRONOSPHERE_BIOME_SCALE);
        }
        return y;
    }

    /**
     * Scale noise coordinates for Chronosphere dimension to reduce biome size.
     */
    @ModifyVariable(
        method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
        at = @At("HEAD"),
        ordinal = 2,
        argsOnly = true
    )
    private int scaleZ(int z) {
        if (this.isChronosphereBiomeSource()) {
            return (int) (z * CHRONOSPHERE_BIOME_SCALE);
        }
        return z;
    }

    /**
     * Check if this biome source belongs to Chronosphere dimension.
     *
     * @return true if any biome in this source is from chronosphere namespace
     */
    private boolean isChronosphereBiomeSource() {
        MultiNoiseBiomeSource self = (MultiNoiseBiomeSource) (Object) this;

        // Check if any of the biomes belong to chronosphere namespace
        for (Holder<Biome> biomeHolder : self.possibleBiomes()) {
            if (biomeHolder.unwrapKey().isPresent()) {
                ResourceKey<Biome> key = biomeHolder.unwrapKey().get();
                if (key.location().getNamespace().equals(Chronosphere.MOD_ID)) {
                    return true;
                }
            }
        }

        return false;
    }
}
