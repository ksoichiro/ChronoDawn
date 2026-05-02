package com.chronodawn.unit;

import com.chronodawn.client.TemporalGrassEdgeTint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link TemporalGrassEdgeTint}.
 * Only the pure math helpers (lerpRgb, multiplyRgb) are testable without a Minecraft runtime.
 * Neighborhood scan and provider entry are validated by visual smoke test.
 */
class TemporalGrassEdgeTintTest {

    @Test
    void lerpRgb_t0_returnsA() {
        assertEquals(0x123456, TemporalGrassEdgeTint.lerpRgb(0x123456, 0xABCDEF, 0.0f));
    }

    @Test
    void lerpRgb_t1_returnsB() {
        assertEquals(0xABCDEF, TemporalGrassEdgeTint.lerpRgb(0x123456, 0xABCDEF, 1.0f));
    }

    @Test
    void lerpRgb_tHalf_returnsRoundedMidpoint() {
        // (0x10 + 0x20) / 2 = 0x18 per channel
        assertEquals(0x181818, TemporalGrassEdgeTint.lerpRgb(0x101010, 0x202020, 0.5f));
    }

    @Test
    void lerpRgb_grassToEdgeSample() {
        int actual = TemporalGrassEdgeTint.lerpRgb(0x5B8AC4, 0x9CB6CC, 1.0f / 3.0f);
        int r = Math.round(0x5B + (0x9C - 0x5B) * (1.0f / 3.0f));
        int g = Math.round(0x8A + (0xB6 - 0x8A) * (1.0f / 3.0f));
        int b = Math.round(0xC4 + (0xCC - 0xC4) * (1.0f / 3.0f));
        int expected = (r << 16) | (g << 8) | b;
        assertEquals(expected, actual);
    }

    @Test
    void multiplyRgb_byWhite_isIdentity() {
        assertEquals(0x123456, TemporalGrassEdgeTint.multiplyRgb(0x123456, 0xFFFFFF));
    }

    @Test
    void multiplyRgb_byBlack_isZero() {
        assertEquals(0x000000, TemporalGrassEdgeTint.multiplyRgb(0x123456, 0x000000));
    }

    @Test
    void multiplyRgb_byHalf_halvesEachChannel() {
        int actual = TemporalGrassEdgeTint.multiplyRgb(0x408060, 0x808080);
        int r = (0x40 * 0x80) / 0xFF;
        int g = (0x80 * 0x80) / 0xFF;
        int b = (0x60 * 0x80) / 0xFF;
        int expected = (r << 16) | (g << 8) | b;
        assertEquals(expected, actual);
    }
}
