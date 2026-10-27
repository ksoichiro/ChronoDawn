package com.chronodawn.unit;

import com.chronodawn.core.wayside.CairnHour;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CairnHourTest {

    @Test
    void dawnWrapsAroundMidnightOfTheDayCounter() {
        assertEquals(CairnHour.DAWN, CairnHour.forDayTime(21000));
        assertEquals(CairnHour.DAWN, CairnHour.forDayTime(23999));
        assertEquals(CairnHour.DAWN, CairnHour.forDayTime(0));
        assertEquals(CairnHour.DAWN, CairnHour.forDayTime(2999));
    }

    @Test
    void otherHourBoundaries() {
        assertEquals(CairnHour.NOON, CairnHour.forDayTime(3000));
        assertEquals(CairnHour.NOON, CairnHour.forDayTime(8999));
        assertEquals(CairnHour.DUSK, CairnHour.forDayTime(9000));
        assertEquals(CairnHour.DUSK, CairnHour.forDayTime(14999));
        assertEquals(CairnHour.MIDNIGHT, CairnHour.forDayTime(15000));
        assertEquals(CairnHour.MIDNIGHT, CairnHour.forDayTime(20999));
    }

    @Test
    void wrapsLargeAndNegativeDayTime() {
        assertEquals(CairnHour.DAWN, CairnHour.forDayTime(-1));
        assertEquals(CairnHour.NOON, CairnHour.forDayTime(24000L * 1000 + 6000));
    }

    @Test
    void namesAndTraits() {
        assertEquals("cairn_stone_midnight", CairnHour.MIDNIGHT.stonePath());
        assertEquals("cairn_stone_dawn", CairnHour.DAWN.stonePath());
        assertEquals("message.chronodawn.time_cairn.noon", CairnHour.NOON.messageKey());
        assertEquals(0.9f, CairnHour.DAWN.soundPitch());
        assertEquals(1.1f, CairnHour.NOON.soundPitch());
        assertEquals(0.8f, CairnHour.DUSK.soundPitch());
        assertEquals(0.6f, CairnHour.MIDNIGHT.soundPitch());
        assertEquals(0xFFE08040, CairnHour.DAWN.dustColorArgb());
        assertEquals(0xFFFFE6A0, CairnHour.NOON.dustColorArgb());
        assertEquals(0xFF9050C0, CairnHour.DUSK.dustColorArgb());
        assertEquals(0xFF2040A0, CairnHour.MIDNIGHT.dustColorArgb());
    }
}
