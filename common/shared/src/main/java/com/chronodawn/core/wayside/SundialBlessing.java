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
    DAY(Effect.SPEED, 0, 12, 1.0f, 1.0f, "message.chronodawn.sundial_gnomon.day"),
    NOON(Effect.SPEED, 1, 24, 1.2f, 1.0f, "message.chronodawn.sundial_gnomon.noon"),
    NIGHT(Effect.NIGHT_VISION, 0, 8, 0.6f, 0.6f, "message.chronodawn.sundial_gnomon.night");

    public enum Effect { SPEED, NIGHT_VISION }

    private static final long TICKS_PER_DAY = 24000L;
    private static final long NOON_START = 5000L;
    private static final long NOON_END = 7000L;
    private static final long NIGHT_START = 13000L;
    private static final long SUNSET = 12000L;
    private static final double MIN_PARTICLE_RADIUS = 0.6;
    private static final double MAX_SHADOW_EXTRA = 0.9;
    private static final double NOON_RING_RADIUS = 0.8;

    private final Effect effect;
    private final int amplifier;
    private final int particleCount;
    private final float soundPitch;
    private final float soundVolume;
    private final String messageKey;

    SundialBlessing(Effect effect, int amplifier, int particleCount, float soundPitch, float soundVolume, String messageKey) {
        this.effect = effect;
        this.amplifier = amplifier;
        this.particleCount = particleCount;
        this.soundPitch = soundPitch;
        this.soundVolume = soundVolume;
        this.messageKey = messageKey;
    }

    public Effect effect() { return effect; }
    public int amplifier() { return amplifier; }
    public int particleCount() { return particleCount; }
    public float soundPitch() { return soundPitch; }
    public float soundVolume() { return soundVolume; }
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

    /**
     * Horizontal particle offsets {dx, dz} from the gnomon's center for the shadow effect.
     * Every point is at least {@code MIN_PARTICLE_RADIUS} from the center so it lands on the
     * dial plate instead of inside the pedestal block under the gnomon.
     * DAY: a line on the X axis pointing away from the sun, from the pedestal edge outward,
     * longer when the sun is low. NOON: the sun is overhead, so a ring around the pedestal.
     * NIGHT: no shadow, so no points.
     */
    public static double[][] shadowParticleOffsets(long dayTime, int count) {
        if (count <= 0 || forDayTime(dayTime) == NIGHT) {
            return new double[0][];
        }
        double shadow = shadowOffsetX(dayTime);
        double[][] offsets = new double[count][2];
        if (forDayTime(dayTime) == NOON || shadow == 0.0) {
            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                offsets[i][0] = Math.cos(angle) * NOON_RING_RADIUS;
                offsets[i][1] = Math.sin(angle) * NOON_RING_RADIUS;
            }
            return offsets;
        }
        double direction = Math.signum(shadow);
        double end = MIN_PARTICLE_RADIUS + MAX_SHADOW_EXTRA * Math.abs(shadow);
        for (int i = 0; i < count; i++) {
            double t = count == 1 ? 0 : i / (double) (count - 1);
            offsets[i][0] = direction * (MIN_PARTICLE_RADIUS + (end - MIN_PARTICLE_RADIUS) * t);
        }
        return offsets;
    }
}
