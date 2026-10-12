package com.chronodawn.unit;

import com.chronodawn.core.wayside.SundialBlessing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
