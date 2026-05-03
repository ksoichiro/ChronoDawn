package com.chronodawn.client;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * Block tint provider for Chrono Dawn cobwebs.
 *
 * <p>Chrono Dawn worldgen places {@code chronodawn:chrono_cobweb}, which keeps
 * vanilla web behavior while making the bright white web texture pick up a
 * noticeable amount of the local biome grass color.
 */
public final class CobwebColorProvider {

    private static final int WHITE = 0xFFFFFF;
    private static final float BLEND = 0.6f;

    private CobwebColorProvider() {}

    /** Block-color callback. Returns {@code -1} for {@code tintIndex != 0}. */
    public static int blockTint(BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        if (tintIndex != 0) return -1;
        if (world == null || pos == null) return WHITE;

        int biome = BiomeColors.getAverageGrassColor(world, pos);
        int r = blendChannel((biome >> 16) & 0xFF);
        int g = blendChannel((biome >> 8) & 0xFF);
        int b = blendChannel(biome & 0xFF);
        return (r << 16) | (g << 8) | b;
    }

    static int blendChannel(int biomeC) {
        float tint = 255f + BLEND * ((float) biomeC - 255f);
        return Math.max(0, Math.min(255, Math.round(tint)));
    }
}
