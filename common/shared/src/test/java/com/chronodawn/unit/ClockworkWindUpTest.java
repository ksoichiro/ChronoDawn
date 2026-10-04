package com.chronodawn.unit;

import com.chronodawn.items.consumables.ClockworkWindUp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClockworkWindUpTest {

    @Test
    void startsFromOneWindWhenNoEffectIsActive() {
        assertEquals(ClockworkWindUp.WIND_TICKS, ClockworkWindUp.nextDuration(0));
    }

    @Test
    void addsOneWindToRemainingDuration() {
        assertEquals(250 + ClockworkWindUp.WIND_TICKS, ClockworkWindUp.nextDuration(250));
    }

    @Test
    void threeCookiesInARowStackToNinetySeconds() {
        int duration = 0;
        for (int i = 0; i < 3; i++) {
            duration = ClockworkWindUp.nextDuration(duration);
        }
        assertEquals(90 * 20, duration);
    }

    @Test
    void clampsToMaxDurationNearTheCap() {
        assertEquals(ClockworkWindUp.MAX_TICKS,
                ClockworkWindUp.nextDuration(ClockworkWindUp.MAX_TICKS - 100));
    }

    @Test
    void staysAtMaxDurationWhenAlreadyFullyWound() {
        assertEquals(ClockworkWindUp.MAX_TICKS, ClockworkWindUp.nextDuration(ClockworkWindUp.MAX_TICKS));
    }

    @Test
    void negativeDurationIsTreatedAsNoEffect() {
        assertEquals(ClockworkWindUp.WIND_TICKS, ClockworkWindUp.nextDuration(-5));
    }
}
