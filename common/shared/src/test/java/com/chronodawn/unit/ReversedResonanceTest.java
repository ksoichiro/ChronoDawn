package com.chronodawn.unit;

import com.chronodawn.core.time.ReversedResonance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ReversedResonance applies effects through ServerLevel, so only the documented tuning
 * constants are checked here.
 */
class ReversedResonanceTest {

    @Test
    void durationsAreThirtyAndSixtySeconds() {
        assertEquals(30 * 20, ReversedResonance.DURATION_SHORT);
        assertEquals(60 * 20, ReversedResonance.DURATION_LONG);
        assertTrue(ReversedResonance.DURATION_LONG > ReversedResonance.DURATION_SHORT);
    }

    @Test
    void effectRangeIsTwentyBlocks() {
        assertEquals(20.0, ReversedResonance.EFFECT_RANGE);
    }
}
