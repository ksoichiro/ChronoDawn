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
package com.chronodawn.core.time;

import com.chronodawn.config.TimeDistortionSettings;

/**
 * Rules for the ambient Time Distortion effect, free of Minecraft types so every
 * version module shares one implementation and it can be tested without bootstrap.
 */
public final class TimeDistortionPolicy {
    /** Ambient instances last this long and are reapplied every tick. */
    public static final int AMBIENT_DURATION_TICKS = 100;

    private static final int RESISTANT_LEVEL_REDUCTION = 2;
    private static final int MAX_SLOWNESS_LEVEL = 5;

    public enum Band { SLOW, NORMAL, FAST, VERY_FAST }

    public enum Tier { IMMUNE, RESISTANT, NORMAL }

    public enum Kind { NONE, SLOWNESS, SPEED }

    /** The effect to apply. {@code level} is the player-visible potion level. */
    public record Outcome(Kind kind, int level) {
        public static final Outcome NONE = new Outcome(Kind.NONE, 0);

        public int amplifier() {
            return level - 1;
        }
    }

    private TimeDistortionPolicy() {
    }

    public static Band band(boolean timeFlowEnabled, float timeSpeed, TimeDistortionSettings settings) {
        if (!timeFlowEnabled || !settings.linkToTimeFlow()) {
            return Band.NORMAL;
        }
        if (timeSpeed < settings.slowBelow()) {
            return Band.SLOW;
        }
        if (timeSpeed < settings.fastAbove()) {
            return Band.NORMAL;
        }
        // A disabled Very fast band falls back to Fast, which also keeps the bell silent.
        if (timeSpeed < settings.veryFastAbove() || settings.veryFastSpeedLevel() == 0) {
            return Band.FAST;
        }
        return Band.VERY_FAST;
    }

    /** Called only when time flow is running, so the flow is treated as enabled. */
    public static boolean entersVeryFast(float previousSpeed, float nextSpeed, TimeDistortionSettings settings) {
        return band(true, previousSpeed, settings) != Band.VERY_FAST
            && band(true, nextSpeed, settings) == Band.VERY_FAST;
    }

    public static Outcome outcome(Band band, boolean enhanced, Tier tier, TimeDistortionSettings settings) {
        if (tier == Tier.IMMUNE) {
            return Outcome.NONE;
        }
        if (band == Band.VERY_FAST) {
            return new Outcome(Kind.SPEED, settings.veryFastSpeedLevel());
        }
        int base = enhanced ? settings.enhancedSlownessLevel() : settings.normalSlownessLevel();
        int level = switch (band) {
            case SLOW -> base + 1;
            case FAST -> base - 1;
            default -> base;
        };
        if (tier == Tier.RESISTANT) {
            level -= RESISTANT_LEVEL_REDUCTION;
        }
        level = Math.min(level, MAX_SLOWNESS_LEVEL);
        return level < 1 ? Outcome.NONE : new Outcome(Kind.SLOWNESS, level);
    }

    /**
     * Whether an active instance is a stale one this system applied and must be removed
     * before applying {@code desiredAmplifier} ({@code -1} for none). Vanilla keeps a
     * stronger instance over a weaker one, so without removal a band change would lag by
     * up to {@link #AMBIENT_DURATION_TICKS}. Non-ambient or longer effects come from other
     * sources, such as Resonance Reversal or potions, and are left alone.
     */
    public static boolean isReplaceableAmbient(
        boolean ambient, int remainingTicks, int currentAmplifier, int desiredAmplifier
    ) {
        return ambient && remainingTicks <= AMBIENT_DURATION_TICKS && currentAmplifier != desiredAmplifier;
    }
}
