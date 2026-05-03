package com.chronodawn.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CobwebColorProvider}.
 */
class CobwebColorProviderTest {

    @Test
    void blockTint_nonZeroTintIndex_returnsMinusOne() {
        assertEquals(-1, CobwebColorProvider.blockTint(null, null, 1));
    }

    @Test
    void blockTint_nullWorld_returnsWhite() {
        assertEquals(0xFFFFFF, CobwebColorProvider.blockTint(null, null, 0));
    }

    @Test
    void blendChannel_whiteBiome_keepsWhite() {
        assertEquals(255, CobwebColorProvider.blendChannel(255));
    }

    @Test
    void blendChannel_darkForestChannel_subtlyDarkens() {
        int tint = CobwebColorProvider.blendChannel(0x44);
        assertTrue(tint >= 142 && tint <= 144, "Expected tint near 143 for biome channel 0x44, got " + tint);
    }
}
