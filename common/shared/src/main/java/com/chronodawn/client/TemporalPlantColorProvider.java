package com.chronodawn.client;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * Block / item tint provider for Temporal Tall Grass and Temporal Fern.
 *
 * <p>The plant textures are baked with a deep teal that matches Chrono Dawn's
 * plains {@code grass_color} ({@value #BASELINE}). Without a tint provider, the
 * plants stay teal in every biome, which clashes with biomes whose grass color
 * differs (e.g. {@code chronodawn_dark_forest} = {@code 0x44520C}).
 *
 * <p>This provider returns a tint that, when multiplied with the texel teal,
 * lands the rendered colour halfway between the baseline and the biome's
 * grass color. The math collapses to {@code 0xFFFFFF} (no tint) when the
 * biome's grass color equals the baseline, so plains appearance is preserved
 * exactly.
 *
 * <p>Inventory and first-person held-item rendering pass {@code world == null},
 * which falls through to {@code 0xFFFFFF} so the icon shows the raw texture.
 */
public final class TemporalPlantColorProvider {

    /** Chrono Dawn plains {@code grass_color} — the texture's intended baseline tint. */
    public static final int BASELINE = 0x5B8AC4;

    /** Blend factor between baseline and biome color. {@code 0.5} = even split, {@code 1.0} = full biome. */
    private static final float BLEND = 0.8f;

    private TemporalPlantColorProvider() {}

    /** Block-color callback. Returns {@code -1} for {@code tintIndex != 0}. */
    public static int blockTint(BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        if (tintIndex != 0) return -1;
        if (world == null || pos == null) return 0xFFFFFF;

        int biome = BiomeColors.getAverageGrassColor(world, pos);
        int rTint = blendChannel((biome >> 16) & 0xFF, (BASELINE >> 16) & 0xFF);
        int gTint = blendChannel((biome >>  8) & 0xFF, (BASELINE >>  8) & 0xFF);
        int bTint = blendChannel( biome        & 0xFF,  BASELINE        & 0xFF);
        return (rTint << 16) | (gTint << 8) | bTint;
    }

    /**
     * Item-color callback. Always returns {@code -1} so the inventory icon
     * renders the texture's raw teal — no in-world biome blending is applied
     * to held items. (The ItemColor contract uses {@code -1} as the
     * "no tint" sentinel; {@code tintIndex} is unused because Temporal plants
     * declare only {@code tintindex: 0}.)
     */
    public static int itemTint(int tintIndex) {
        return -1;
    }

    /**
     * Per-channel blend used by {@link #blockTint}. Package-private to allow
     * direct unit testing without reflection.
     *
     * <p>Computes {@code lerp(255, min(biomeC * 255 / baseC, 255), BLEND)} and
     * clamps to {@code [0, 255]}. When {@code baseC == 0} the ratio defaults to
     * {@code 1f} (defensive — the live baseline {@code 0x5B8AC4} has no zero
     * channel, but the constant could change).
     */
    static int blendChannel(int biomeC, int baseC) {
        float ratio = baseC == 0 ? 1f : (float) biomeC / (float) baseC;
        float scaled = Math.min(ratio * 255f, 255f);
        float tint = 255f + BLEND * (scaled - 255f);
        return Math.max(0, Math.min(255, Math.round(tint)));
    }
}
