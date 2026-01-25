package com.chronodawn.events;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModDimensions;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceKey;

/**
 * Time Distortion Event Handler
 *
 * Implements variable day/night cycle speed for ChronoDawn dimension.
 * The time flow speed changes randomly to create "time distortion" effect.
 *
 * Design Philosophy:
 * - Day/night cycle exists (enables hostile mob spawning)
 * - Cycle speed varies randomly (0.5x to 2.0x normal speed)
 * - Creates unpredictable time flow matching "time manipulation" theme
 * - Players experience time speeding up, slowing down, or moving normally
 *
 * Reference: research.md (Decision 14: Variable Time Cycle)
 * Task: T200 [US1] Implement variable time cycle for ChronoDawn
 */
public class TimeDistortionEventHandler {

    // Time speed multiplier per dimension
    // Thread-safe: ConcurrentHashMap prevents time corruption in multiplayer
    private static final Map<ResourceKey<net.minecraft.world.level.Level>, Float> timeSpeedMap = new ConcurrentHashMap<>();

    // Current time until next speed change per dimension
    // Thread-safe: ConcurrentHashMap prevents race conditions in multiplayer
    private static final Map<ResourceKey<net.minecraft.world.level.Level>, Integer> timeUntilChangeMap = new ConcurrentHashMap<>();

    // Target time for sleep skip (null = no sleep skip in progress)
    // Thread-safe: ConcurrentHashMap prevents sleep skip failures in multiplayer
    private static final Map<ResourceKey<net.minecraft.world.level.Level>, Long> sleepSkipTargetTimeMap = new ConcurrentHashMap<>();

    // Accumulated fractional ticks per dimension (for sub-tick time advancement)
    // Thread-safe: ConcurrentHashMap prevents lost time advancement in multiplayer
    private static final Map<ResourceKey<net.minecraft.world.level.Level>, Float> accumulatedTicksMap = new ConcurrentHashMap<>();

    // How many ticks to add per game tick during sleep skip (gradual advancement)
    private static final long SLEEP_SKIP_TICKS_PER_TICK = 500;

    // Configuration
    private static final float MIN_SPEED = 0.67f; // Slowest: 67% speed (day lasts ~15 minutes)
    private static final float MAX_SPEED = 5.0f;  // Fastest: 5x speed (day lasts 2 minutes)
    private static final int MIN_DURATION = 1200; // Minimum duration: 60 seconds (1200 ticks)
    private static final int MAX_DURATION = 6000; // Maximum duration: 5 minutes (6000 ticks)

    /**
     * Register time distortion event
     * Called from mod initialization
     */
    public static void register() {
        TickEvent.SERVER_LEVEL_POST.register(TimeDistortionEventHandler::onServerLevelTick);
        ChronoDawn.LOGGER.debug("Registered TimeDistortionEventHandler");
    }

    /**
     * Server level tick event handler
     * Adjusts time flow speed for ChronoDawn dimension
     */
    private static void onServerLevelTick(ServerLevel level) {
        // Only process ChronoDawn dimension
        if (!level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        ResourceKey<net.minecraft.world.level.Level> dimensionKey = level.dimension();

        // Initialize if first tick
        if (!timeSpeedMap.containsKey(dimensionKey)) {
            timeSpeedMap.put(dimensionKey, 1.0f); // Start at normal speed
            timeUntilChangeMap.put(dimensionKey, getRandomDuration(level.getRandom()));
            ChronoDawn.LOGGER.debug("TimeDistortionEventHandler: Initialized for ChronoDawn with speed 1.0x");
        }

        // Get current values
        float currentSpeed = timeSpeedMap.get(dimensionKey);
        int timeUntilChange = timeUntilChangeMap.get(dimensionKey);

        // Check if we should advance time for sleep skip
        Long targetTime = sleepSkipTargetTimeMap.get(dimensionKey);
        if (targetTime != null) {
            // Sleep skip in progress - advance time gradually
            long currentTime = level.getDayTime();
            long remainingTicks = targetTime - currentTime;

            if (remainingTicks > 0) {
                // Advance time by a small increment
                long ticksThisTick = Math.min(remainingTicks, SLEEP_SKIP_TICKS_PER_TICK);
                long newDayTime = currentTime + ticksThisTick;
                level.setDayTime(newDayTime);

                ChronoDawn.LOGGER.debug("TimeDistortionEventHandler: Sleep skip advancing... {} / {} remaining",
                    remainingTicks - ticksThisTick, remainingTicks);
            } else {
                // Target reached!
                sleepSkipTargetTimeMap.remove(dimensionKey);
                ChronoDawn.LOGGER.debug("TimeDistortionEventHandler: Sleep skip complete! Final time: {}", currentTime);
            }

            return; // Skip normal time adjustment this tick
        }

        // Apply time speed adjustment (normal operation)
        // Note: Mixin cancels automatic time advancement, so we handle ALL time progression here
        long currentDayTime = level.getDayTime();

        // Get accumulated fractional ticks
        float accumulatedTicks = accumulatedTicksMap.getOrDefault(dimensionKey, 0.0f);

        // Add current speed to accumulated ticks
        // currentSpeed = 2.0 means add 2 ticks per game tick
        // currentSpeed = 0.67 means add 0.67 ticks per game tick (3 game ticks = 2 time ticks)
        accumulatedTicks += currentSpeed;

        // Extract whole ticks to advance
        long ticksToAdvance = (long) accumulatedTicks;

        // Keep fractional remainder for next tick
        accumulatedTicks -= ticksToAdvance;
        accumulatedTicksMap.put(dimensionKey, accumulatedTicks);

        // Advance time by the calculated amount
        if (ticksToAdvance > 0) {
            long newDayTime = currentDayTime + ticksToAdvance;
            level.setDayTime(newDayTime);
        }
        // If ticksToAdvance == 0, accumulate the fractional part for next tick

        // Countdown to next speed change
        timeUntilChange--;
        timeUntilChangeMap.put(dimensionKey, timeUntilChange);

        // Time to change speed?
        if (timeUntilChange <= 0) {
            float newSpeed = getRandomSpeed(level.getRandom());
            int newDuration = getRandomDuration(level.getRandom());

            ChronoDawn.LOGGER.debug("TimeDistortionEventHandler: Speed changed from {}x to {}x (duration: {} ticks)",
                currentSpeed, newSpeed, newDuration);

            timeSpeedMap.put(dimensionKey, newSpeed);
            timeUntilChangeMap.put(dimensionKey, newDuration);
        }
    }

    /**
     * Get random time speed multiplier
     * @param random Random source
     * @return Speed multiplier between MIN_SPEED and MAX_SPEED
     */
    private static float getRandomSpeed(RandomSource random) {
        return MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED);
    }

    /**
     * Get random duration until next speed change
     * @param random Random source
     * @return Duration in ticks between MIN_DURATION and MAX_DURATION
     */
    private static int getRandomDuration(RandomSource random) {
        return MIN_DURATION + random.nextInt(MAX_DURATION - MIN_DURATION);
    }

    /**
     * Get current time speed for dimension (for debugging/display)
     * @param level Server level
     * @return Current time speed multiplier
     */
    public static float getCurrentSpeed(ServerLevel level) {
        ResourceKey<net.minecraft.world.level.Level> dimensionKey = level.dimension();
        return timeSpeedMap.getOrDefault(dimensionKey, 1.0f);
    }

    /**
     * Request sleep skip to target time.
     * TimeDistortionEventHandler will set the time on next tick.
     * @param level Server level
     * @param targetTime Target day time (e.g., 1000 for morning)
     */
    public static void requestSleepSkip(ServerLevel level, long targetTime) {
        ResourceKey<net.minecraft.world.level.Level> dimensionKey = level.dimension();
        sleepSkipTargetTimeMap.put(dimensionKey, targetTime);
        ChronoDawn.LOGGER.debug("Sleep skip requested: target time = {}", targetTime);
    }
}
