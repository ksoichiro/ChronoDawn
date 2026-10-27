/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.core.wayside;

import java.util.Locale;

/**
 * The quarter of the day a Time Cairn dial reads, chosen from the dimension's time of day.
 *
 * <p>Each hour gives its own cairn stone. Version-independent so the boundaries can be unit
 * tested. DAWN spans the end of one day counter and the start of the next (21000 to 2999).
 */
public enum CairnHour {
    DAWN(0.9f, 0xFFE08040),
    NOON(1.1f, 0xFFFFE6A0),
    DUSK(0.8f, 0xFF9050C0),
    MIDNIGHT(0.6f, 0xFF2040A0);

    public static final long TICKS_PER_DAY = 24000L;
    public static final long QUARTER_TICKS = 6000L;
    public static final long DAWN_START = 21000L;

    private final float soundPitch;
    private final int dustColorArgb;

    CairnHour(float soundPitch, int dustColorArgb) {
        this.soundPitch = soundPitch;
        this.dustColorArgb = dustColorArgb;
    }

    public static CairnHour forDayTime(long dayTime) {
        // Shift so DAWN starts at zero, which lets the wrap at the day boundary fall out of floorMod.
        long shifted = Math.floorMod(dayTime - DAWN_START, TICKS_PER_DAY);
        return values()[(int) (shifted / QUARTER_TICKS)];
    }

    public String messageKey() {
        return "message.chronodawn.time_cairn." + name().toLowerCase(Locale.ROOT);
    }

    public String stonePath() {
        return "cairn_stone_" + name().toLowerCase(Locale.ROOT);
    }

    public float soundPitch() { return soundPitch; }

    public int dustColorArgb() { return dustColorArgb; }
}
