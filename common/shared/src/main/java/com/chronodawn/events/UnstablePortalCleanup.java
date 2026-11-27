package com.chronodawn.events;

/**
 * Scheduling for the cleanup of portal blocks while portals are unstable.
 *
 * <p>The cleanup scans a 21x21x21 volume per registered portal. Running it every tick cost
 * about 9,000 block lookups per portal for as long as the gate stayed on, so it runs once per
 * interval instead. Portal blocks that reappear are removed at most one interval later.
 */
public final class UnstablePortalCleanup {
    /** Ticks between scans (1 second). */
    public static final int INTERVAL_TICKS = 20;

    /** Scan radius around each registered portal position. */
    public static final int RADIUS = 10;

    private UnstablePortalCleanup() {
    }

    /**
     * @param tickCount the server tick counter
     * @return true when the cleanup scan should run on this tick
     */
    public static boolean shouldScan(int tickCount) {
        return Math.floorMod(tickCount, INTERVAL_TICKS) == 0;
    }
}
