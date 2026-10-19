package com.chronodawn.compat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Compatibility wrapper for reading and setting the day time (pre-26.x versions).
 *
 * Minecraft 26.x moved day time to world clocks, so each version module provides its own copy.
 */
public final class CompatLevelTime {
    private CompatLevelTime() {
    }

    /**
     * Get the current day time of the level.
     */
    public static long dayTime(Level level) {
        return level.getDayTime();
    }

    /**
     * Set the day time of the level (used by GameTests).
     */
    public static void setDayTime(ServerLevel level, long time) {
        level.setDayTime(time);
    }
}
