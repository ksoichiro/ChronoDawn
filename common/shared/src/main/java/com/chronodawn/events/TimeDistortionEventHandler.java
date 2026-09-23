package com.chronodawn.events;

import com.chronodawn.ChronoDawn;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.TimeFlowSettings;
import com.chronodawn.registry.ModDimensions;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

    // Interval for sending time sync packets to clients (in ticks)
    private static final int TIME_SYNC_INTERVAL = 20;

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
        TimeFlowSettings timeFlow = ChronoDawnConfig.get().gameplay().timeFlow();

        // Initialize if first tick
        if (!timeSpeedMap.containsKey(dimensionKey)) {
            timeSpeedMap.put(dimensionKey, 1.0f); // Start at normal speed
            timeUntilChangeMap.put(dimensionKey, getRandomDuration(level.getRandom(), timeFlow));
            ChronoDawn.LOGGER.debug("TimeDistortionEventHandler: Initialized for ChronoDawn with speed 1.0x");
        }

        // Get current values. When time flow variation is disabled, time always
        // advances at normal speed regardless of what was last stored.
        float currentSpeed = timeFlow.enabled() ? timeSpeedMap.get(dimensionKey) : 1.0f;
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

        // Sync time to clients periodically
        // Vanilla may not send per-dimension time packets for custom dimensions,
        // so we explicitly send the independent time to players in ChronoDawn.
        syncTimeToClients(level);

        // Countdown to next speed change
        timeUntilChange--;
        timeUntilChangeMap.put(dimensionKey, timeUntilChange);

        // Time to change speed? (skipped entirely when time flow variation is disabled)
        if (timeFlow.enabled() && timeUntilChange <= 0) {
            float newSpeed = getRandomSpeed(level.getRandom(), timeFlow);
            int newDuration = getRandomDuration(level.getRandom(), timeFlow);

            ChronoDawn.LOGGER.debug("TimeDistortionEventHandler: Speed changed from {}x to {}x (duration: {} ticks)",
                currentSpeed, newSpeed, newDuration);

            timeSpeedMap.put(dimensionKey, newSpeed);
            timeUntilChangeMap.put(dimensionKey, newDuration);
        }
    }

    /**
     * Get random time speed multiplier
     * @param random Random source
     * @param timeFlow Configured speed range
     * @return Speed multiplier between the configured min and max speed
     */
    private static float getRandomSpeed(RandomSource random, TimeFlowSettings timeFlow) {
        return timeFlow.minSpeed() + random.nextFloat() * (timeFlow.maxSpeed() - timeFlow.minSpeed());
    }

    /**
     * Get random duration until next speed change
     * @param random Random source
     * @param timeFlow Configured duration range
     * @return Duration in ticks between the configured min and max duration
     */
    private static int getRandomDuration(RandomSource random, TimeFlowSettings timeFlow) {
        int range = timeFlow.maxDurationTicks() - timeFlow.minDurationTicks();
        return range <= 0 ? timeFlow.minDurationTicks() : timeFlow.minDurationTicks() + random.nextInt(range);
    }

    /**
     * Send time sync packet to all players in ChronoDawn dimension.
     * Vanilla time sync may not send the correct independent time for custom dimensions,
     * so we explicitly broadcast it here.
     */
    private static void syncTimeToClients(ServerLevel level) {
        if (level.getServer().getTickCount() % TIME_SYNC_INTERVAL != 0) {
            return;
        }

        // Always pass true for doDaylightCycle: ChronoDawn manages its own time cycle
        // via TimeDistortionEventHandler, independent of the vanilla gamerule.
        ClientboundSetTimePacket packet = new ClientboundSetTimePacket(
            level.getGameTime(),
            level.getDayTime(),
            true
        );
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
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
