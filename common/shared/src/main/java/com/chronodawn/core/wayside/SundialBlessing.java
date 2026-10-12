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

/**
 * What an Old Sundial grants, chosen from the dimension's time of day.
 *
 * <p>Version-independent so phase boundaries can be unit tested. Each version's
 * {@code SundialGnomonBlock} maps {@link Effect} to the matching vanilla effect.
 */
public enum SundialBlessing {
    DAY(Effect.SPEED, 0, 12, 1.0f, "message.chronodawn.sundial_gnomon.day"),
    NOON(Effect.SPEED, 1, 24, 1.2f, "message.chronodawn.sundial_gnomon.noon"),
    NIGHT(Effect.NIGHT_VISION, 0, 8, 0.6f, "message.chronodawn.sundial_gnomon.night");

    public enum Effect { SPEED, NIGHT_VISION }

    private static final long TICKS_PER_DAY = 24000L;
    private static final long NOON_START = 5000L;
    private static final long NOON_END = 7000L;
    private static final long NIGHT_START = 13000L;
    private static final long SUNSET = 12000L;

    private final Effect effect;
    private final int amplifier;
    private final int particleCount;
    private final float soundPitch;
    private final String messageKey;

    SundialBlessing(Effect effect, int amplifier, int particleCount, float soundPitch, String messageKey) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.particleCount = particleCount;
        this.soundPitch = soundPitch;
        this.messageKey = messageKey;
    }

    public Effect effect() { return effect; }
    public int amplifier() { return amplifier; }
    public int particleCount() { return particleCount; }
    public float soundPitch() { return soundPitch; }
    public String messageKey() { return messageKey; }

    public static SundialBlessing forDayTime(long dayTime) {
        long time = Math.floorMod(dayTime, TICKS_PER_DAY);
        if (time >= NIGHT_START) {
            return NIGHT;
        }
        if (time >= NOON_START && time < NOON_END) {
            return NOON;
        }
        return DAY;
    }

    /**
     * X offset of the gnomon's shadow, from -1 (west) to 1 (east).
     * The sun crosses from east to west between dayTime 0 and 12000, and the
     * shadow points away from it. Night has no shadow.
     */
    public static double shadowOffsetX(long dayTime) {
        long time = Math.floorMod(dayTime, TICKS_PER_DAY);
        if (time >= NIGHT_START) {
            return 0.0;
        }
        double sunProgress = Math.min(time, SUNSET) / (double) SUNSET;
        double offset = -Math.cos(Math.PI * sunProgress);
        // Avoid -0.0 and 6.1e-17 style noise at noon so callers can compare against 0.
        return Math.abs(offset) < 1e-9 ? 0.0 : offset;
    }
}
