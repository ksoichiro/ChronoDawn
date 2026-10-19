package com.chronodawn.unit;

import com.chronodawn.blocks.ChronoBloomStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boundary checks for the time-of-day to Chrono Bloom stage mapping.
 */
public class ChronoBloomStageTest {

    private static void check(long dayTime, int expected) {
        assertEquals(expected, ChronoBloomStage.stageFor(dayTime), "dayTime=" + dayTime);
    }

    @Test
    void stageForMapsTimeOfDay() {
        check(0, 1);
        check(999, 1);
        check(1000, 2);
        check(10999, 2);
        check(11000, 3);
        check(12999, 3);
        check(13000, 0);
        check(22999, 0);
        check(23000, 1);
        check(23999, 1);
        check(24000, 1);
        check(25000, 2);
        check(-1, 1);
        // -11000 wraps to 13000 (closed), -11001 wraps to 12999 (closing).
        check(-11000, 0);
        check(-11001, 3);
        check(240013000L, 0);
    }

    @Test
    void isNightOnlyDuringClosedStage() {
        assertTrue(ChronoBloomStage.isNight(13000));
        assertFalse(ChronoBloomStage.isNight(12999));
        assertFalse(ChronoBloomStage.isNight(23000));
    }
}
