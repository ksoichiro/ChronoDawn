package com.chronodawn.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TemporalPlantColorProvider}.
 *
 * The class is loader-agnostic and lives under {@code common/shared/}, so testing
 * it from a single per-version test module covers all 11 Minecraft versions.
 */
class TemporalPlantColorProviderTest {

    @Test
    void itemTint_returnsNoTintSentinel_forAnyTintIndex() {
        assertEquals(-1, TemporalPlantColorProvider.itemTint(0));
        assertEquals(-1, TemporalPlantColorProvider.itemTint(1));
        assertEquals(-1, TemporalPlantColorProvider.itemTint(2));
    }

    @Test
    void blockTint_nonZeroTintIndex_returnsMinusOne() {
        assertEquals(-1, TemporalPlantColorProvider.blockTint(null, null, 1));
    }

    @Test
    void blockTint_nullWorld_returnsWhite() {
        assertEquals(0xFFFFFF, TemporalPlantColorProvider.blockTint(null, null, 0));
    }

    @Test
    void blendChannel_baselineMatch_returns255() {
        // When biome channel == baseline channel, ratio = 1.0, scaled = 255,
        // tint = 255 + 0.5 * (255 - 255) = 255.
        assertEquals(255, TemporalPlantColorProvider.blendChannel(0x5B, 0x5B));
        assertEquals(255, TemporalPlantColorProvider.blendChannel(0xC4, 0xC4));
    }

    @Test
    void blendChannel_biomeBelowBaseline_dampensTowardBiome() {
        // dark_forest R = 0x44 over plains R = 0x5B:
        // ratio = 0x44 / 0x5B = 0.7472...
        // scaled = 190.5
        // tint = 255 + 0.5 * (190.5 - 255) = 222.75 -> 223 (rounded)
        int tint = TemporalPlantColorProvider.blendChannel(0x44, 0x5B);
        assertTrue(tint >= 222 && tint <= 224,
            "Expected tint near 223 for biome=0x44 base=0x5B, got " + tint);
    }

    @Test
    void blendChannel_biomeAboveBaseline_clampsAt255() {
        // ratio > 1 means scaled is clamped to 255 -> tint = 255.
        // (Highlights stay neutral; we don't brighten the texture.)
        assertEquals(255, TemporalPlantColorProvider.blendChannel(0xFF, 0x80));
    }

    @Test
    void blendChannel_zeroBaseline_fallsBackToOne() {
        // Defensive guard: baseC == 0 makes ratio default to 1f, so tint = 255.
        assertEquals(255, TemporalPlantColorProvider.blendChannel(0x44, 0));
    }

    @Test
    void blendChannel_zeroBiome_neverNegative() {
        // ratio = 0, scaled = 0, tint = 255 + 0.5 * (-255) = 127.5 -> 128.
        int tint = TemporalPlantColorProvider.blendChannel(0, 0xFF);
        assertTrue(tint >= 127 && tint <= 128,
            "Expected tint near 128 for biome=0 base=0xFF, got " + tint);
    }
}
