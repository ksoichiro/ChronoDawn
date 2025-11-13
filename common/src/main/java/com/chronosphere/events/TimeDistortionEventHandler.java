package com.chronosphere.events;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModDimensions;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;

/**
 * Time Distortion Event Handler
 *
 * Implements variable day/night cycle speed for Chronosphere dimension.
 * The time flow speed changes randomly to create "time distortion" effect.
 *
 * Design Philosophy:
 * - Day/night cycle exists (enables hostile mob spawning)
 * - Cycle speed varies randomly (0.5x to 2.0x normal speed)
 * - Creates unpredictable time flow matching "time manipulation" theme
 * - Players experience time speeding up, slowing down, or moving normally
 *
 * Reference: research.md (Decision 14: Variable Time Cycle)
 * Task: T200 [US1] Implement variable time cycle for Chronosphere
 */
public class TimeDistortionEventHandler {

    // Time speed multiplier per dimension
    private static final Map<ResourceKey<net.minecraft.world.level.Level>, Float> timeSpeedMap = new HashMap<>();

    // Current time until next speed change per dimension
    private static final Map<ResourceKey<net.minecraft.world.level.Level>, Integer> timeUntilChangeMap = new HashMap<>();

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
        Chronosphere.LOGGER.info("Registered TimeDistortionEventHandler");
    }

    /**
     * Server level tick event handler
     * Adjusts time flow speed for Chronosphere dimension
     */
    private static void onServerLevelTick(ServerLevel level) {
        // Only process Chronosphere dimension
        if (!level.dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
            return;
        }

        ResourceKey<net.minecraft.world.level.Level> dimensionKey = level.dimension();

        // Initialize if first tick
        if (!timeSpeedMap.containsKey(dimensionKey)) {
            timeSpeedMap.put(dimensionKey, 1.0f); // Start at normal speed
            timeUntilChangeMap.put(dimensionKey, getRandomDuration(level.getRandom()));
        }

        // Get current values
        float currentSpeed = timeSpeedMap.get(dimensionKey);
        int timeUntilChange = timeUntilChangeMap.get(dimensionKey);

        // Apply time speed adjustment
        if (currentSpeed != 1.0f) {
            long currentDayTime = level.getDayTime();

            // Calculate additional ticks to add/subtract
            // currentSpeed = 2.0 means time moves 2x faster (add 1 extra tick)
            // currentSpeed = 0.5 means time moves 2x slower (subtract 0.5 ticks)
            float additionalTicks = currentSpeed - 1.0f;

            if (additionalTicks != 0) {
                long newDayTime = currentDayTime + Math.round(additionalTicks);
                level.setDayTime(newDayTime);
            }
        }

        // Countdown to next speed change
        timeUntilChange--;
        timeUntilChangeMap.put(dimensionKey, timeUntilChange);

        // Time to change speed?
        if (timeUntilChange <= 0) {
            float newSpeed = getRandomSpeed(level.getRandom());
            int newDuration = getRandomDuration(level.getRandom());

            timeSpeedMap.put(dimensionKey, newSpeed);
            timeUntilChangeMap.put(dimensionKey, newDuration);

            // Log speed change for debugging
            Chronosphere.LOGGER.debug(
                "Chronosphere time speed changed: {}x (duration: {} ticks / {} seconds)",
                String.format("%.2f", newSpeed),
                newDuration,
                newDuration / 20
            );
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
}
