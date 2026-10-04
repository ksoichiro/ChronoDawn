package com.chronodawn.items.consumables;

/**
 * Wind-up duration logic for the Clockwork Cookie.
 *
 * Each cookie "winds the spring" by adding a fixed amount of time to the remaining
 * Resistance I duration, up to a cap. Kept version-agnostic so it can be unit tested
 * without bootstrapping Minecraft registries.
 */
public final class ClockworkWindUp {

    /** Duration added per cookie: 30 seconds. */
    public static final int WIND_TICKS = 30 * 20;

    /** Maximum wound-up duration: 3 minutes. */
    public static final int MAX_TICKS = 3 * 60 * 20;

    private ClockworkWindUp() {
    }

    /**
     * Compute the Resistance I duration after eating one more cookie.
     *
     * @param currentTicks remaining duration of the current Resistance I effect, or 0 if none
     * @return the new duration in ticks, clamped to {@link #MAX_TICKS}
     */
    public static int nextDuration(int currentTicks) {
        return Math.min(Math.max(currentTicks, 0) + WIND_TICKS, MAX_TICKS);
    }
}
