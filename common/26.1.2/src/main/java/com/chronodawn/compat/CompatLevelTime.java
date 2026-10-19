package com.chronodawn.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;

/**
 * Compatibility wrapper for reading and setting the day time (Minecraft 26.x).
 *
 * Day time lives in world clocks since 26.1, so the setter resolves the dimension's default clock.
 */
public final class CompatLevelTime {
    private CompatLevelTime() {
    }

    /**
     * Get the current day time of the level.
     */
    public static long dayTime(Level level) {
        return level.getDefaultClockTime();
    }

    /**
     * Set the day time of the level (used by GameTests).
     */
    public static void setDayTime(ServerLevel level, long time) {
        Holder<WorldClock> clock = level.dimensionType().defaultClock().orElseGet(() ->
            level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD));
        level.clockManager().setTotalTicks(clock, time);
    }
}
