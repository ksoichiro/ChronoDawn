package com.chronodawn.blocks;

/**
 * Maps the time of day to a Chrono Bloom flower stage.
 *
 * Deliberately free of Minecraft imports so it compiles unchanged in every version.
 */
public final class ChronoBloomStage {
    public static final int CLOSED = 0;
    public static final int OPENING = 1;
    public static final int OPEN = 2;
    public static final int CLOSING = 3;

    private static final long DAY_LENGTH = 24000L;

    private ChronoBloomStage() {
    }

    /**
     * Returns the stage for the given day time. floorMod makes negative values wrap.
     */
    public static int stageFor(long dayTime) {
        long t = Math.floorMod(dayTime, DAY_LENGTH);
        if (t < 1000L) {
            return OPENING;
        }
        if (t < 11000L) {
            return OPEN;
        }
        if (t < 13000L) {
            return CLOSING;
        }
        if (t < 23000L) {
            return CLOSED;
        }
        return OPENING;
    }

    /**
     * True while the flower is fully closed (night).
     */
    public static boolean isNight(long dayTime) {
        return stageFor(dayTime) == CLOSED;
    }
}
