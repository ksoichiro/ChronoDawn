package com.chronodawn.unit;

import com.chronodawn.core.wayside.SundialBlessing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SundialBlessingTest {

    @Test
    void phaseBoundaries() {
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(0));
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(4999));
        assertEquals(SundialBlessing.NOON, SundialBlessing.forDayTime(5000));
        assertEquals(SundialBlessing.NOON, SundialBlessing.forDayTime(6999));
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(7000));
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(12999));
        assertEquals(SundialBlessing.NIGHT, SundialBlessing.forDayTime(13000));
        assertEquals(SundialBlessing.NIGHT, SundialBlessing.forDayTime(23999));
    }

    @Test
    void wrapsLargeAndNegativeDayTime() {
        assertEquals(SundialBlessing.NOON, SundialBlessing.forDayTime(24000L * 1000 + 6000));
        assertEquals(SundialBlessing.NIGHT, SundialBlessing.forDayTime(-1));
        assertEquals(SundialBlessing.DAY, SundialBlessing.forDayTime(-24000));
        assertEquals(-1.0, SundialBlessing.shadowOffsetX(24000L * 5), 1e-9);
    }

    @Test
    void effectsPerPhase() {
        assertEquals(SundialBlessing.Effect.SPEED, SundialBlessing.DAY.effect());
        assertEquals(0, SundialBlessing.DAY.amplifier());
        assertEquals(SundialBlessing.Effect.SPEED, SundialBlessing.NOON.effect());
        assertEquals(1, SundialBlessing.NOON.amplifier());
        assertEquals(SundialBlessing.Effect.NIGHT_VISION, SundialBlessing.NIGHT.effect());
        assertEquals(0, SundialBlessing.NIGHT.amplifier());
    }

    @Test
    void noonHasTheMostParticles() {
        assertEquals(true, SundialBlessing.NOON.particleCount() > SundialBlessing.DAY.particleCount());
        assertEquals(true, SundialBlessing.DAY.particleCount() > SundialBlessing.NIGHT.particleCount());
    }

    @Test
    void shadowPointsAwayFromTheSun() {
        // The sun rises in the east (+X), so the morning shadow points west (-X).
        assertEquals(-1.0, SundialBlessing.shadowOffsetX(0), 1e-9);
        assertEquals(0.0, SundialBlessing.shadowOffsetX(6000), 1e-9);
        assertEquals(1.0, SundialBlessing.shadowOffsetX(12000), 1e-9);
        assertEquals(1.0, SundialBlessing.shadowOffsetX(12999), 1e-9);
        assertEquals(0.0, SundialBlessing.shadowOffsetX(18000), 1e-9);
    }

    @Test
    void messageKeysAreDistinct() {
        assertEquals("message.chronodawn.sundial_gnomon.day", SundialBlessing.DAY.messageKey());
        assertEquals("message.chronodawn.sundial_gnomon.noon", SundialBlessing.NOON.messageKey());
        assertEquals("message.chronodawn.sundial_gnomon.night", SundialBlessing.NIGHT.messageKey());
    }

    @Test
    void particleOffsetsStayOutsideThePedestal() {
        for (int count : new int[] {12, 24}) {
            for (long t = 0; t < 13000; t += 50) {
                double[][] offsets = SundialBlessing.shadowParticleOffsets(t, count);
                assertEquals(count, offsets.length);
                for (double[] o : offsets) {
                    assertTrue(Math.hypot(o[0], o[1]) >= 0.6 - 1e-9, "dayTime " + t);
                }
            }
        }
    }

    @Test
    void nightHasNoParticleOffsets() {
        assertEquals(0, SundialBlessing.shadowParticleOffsets(13000, 8).length);
        assertEquals(0, SundialBlessing.shadowParticleOffsets(18000, 8).length);
        assertEquals(0, SundialBlessing.shadowParticleOffsets(23999, 8).length);
    }

    @Test
    void noonParticlesFormARing() {
        for (long t : new long[] {5000, 6000, 6999}) {
            double[][] offsets = SundialBlessing.shadowParticleOffsets(t, 24);
            assertEquals(24, offsets.length);
            for (double[] o : offsets) {
                assertEquals(0.8, Math.hypot(o[0], o[1]), 1e-9);
            }
        }
    }

    @Test
    void particleLineFollowsTheShadowDirection() {
        for (double[] o : SundialBlessing.shadowParticleOffsets(1000, 12)) {
            assertTrue(o[0] < 0);
        }
        for (double[] o : SundialBlessing.shadowParticleOffsets(11000, 12)) {
            assertTrue(o[0] > 0);
        }
    }

    @Test
    void particleOffsetsEdgeCounts() {
        assertEquals(0, SundialBlessing.shadowParticleOffsets(1000, 0).length);
        double[][] single = SundialBlessing.shadowParticleOffsets(1000, 1);
        assertEquals(1, single.length);
        assertEquals(-0.6, single[0][0], 1e-9);
    }

    @Test
    void nightChimeIsSofter() {
        assertTrue(SundialBlessing.NIGHT.soundVolume() < SundialBlessing.DAY.soundVolume());
    }
}
