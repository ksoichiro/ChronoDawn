package com.chronodawn.mixin;

import com.chronodawn.ChronoDawn;
import com.chronodawn.events.TimeDistortionEventHandler;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mixin to enable bed sleeping time skip in ChronoDawn dimension.
 *
 * Custom dimensions don't naturally support time skipping when sleeping,
 * so this mixin manually checks if all players in ChronoDawn are sleeping
 * and advances time to morning if so.
 *
 * Design Philosophy:
 * - Only affects ChronoDawn dimension (independent from Overworld)
 * - Requires ALL players in ChronoDawn to be sleeping (multiplayer-friendly)
 * - Does not affect players in other dimensions
 * - Respects ChronoDawn's variable time cycle system
 *
 * Implementation Note:
 * - isSleepingLongEnough() doesn't work in custom dimensions
 * - We track sleep time manually using a per-player counter
 * - Players must sleep for 100 ticks (5 seconds) before time skip
 *
 * Task: T301 [P] Fix bed sleeping mechanic in ChronoDawn
 */
@Mixin(ServerLevel.class)
public abstract class SleepMixin {

    private int tickCounter = 0;
    private boolean hasLoggedSleepStatus = false;

    // Track how long each player has been sleeping (ticks)
    private final Map<UUID, Integer> sleepTimeMap = new HashMap<>();

    // How long players must sleep before time skip (100 ticks = 5 seconds, same as vanilla)
    private static final int REQUIRED_SLEEP_TIME = 100;

    /**
     * Check if all players in ChronoDawn are sleeping and skip to morning if so.
     *
     * This is injected at the end of ServerLevel.tick() to check sleep status
     * every tick and advance time when appropriate.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;

        // Only process ChronoDawn dimension
        if (!level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        // Get all players in ChronoDawn dimension
        List<ServerPlayer> chronodawnPlayers = level.getServer()
            .getPlayerList()
            .getPlayers()
            .stream()
            .filter(p -> p.level().dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION))
            .collect(Collectors.toList());

        // If no players in ChronoDawn, do nothing
        if (chronodawnPlayers.isEmpty()) {
            return;
        }

        // Update sleep time counters for each player
        for (ServerPlayer player : chronodawnPlayers) {
            UUID playerId = player.getUUID();
            if (player.isSleeping()) {
                // Increment sleep counter
                int currentSleepTime = sleepTimeMap.getOrDefault(playerId, 0);
                sleepTimeMap.put(playerId, currentSleepTime + 1);
            } else {
                // Player is not sleeping, reset counter
                sleepTimeMap.put(playerId, 0);
            }
        }

        // Debug logging every 20 ticks (1 second) if any player is sleeping
        tickCounter++;
        boolean anyoneSleeping = chronodawnPlayers.stream().anyMatch(ServerPlayer::isSleeping);
        if (anyoneSleeping && tickCounter % 20 == 0) {
            long currentTime = level.getDayTime();
            long dayOfCycle = currentTime % 24000;
            for (ServerPlayer player : chronodawnPlayers) {
                UUID playerId = player.getUUID();
                int sleepTime = sleepTimeMap.getOrDefault(playerId, 0);
                ChronoDawn.LOGGER.info(
                    "DEBUG Sleep Check - Player: {}, isSleeping: {}, sleepTime: {}/{}, dayTime: {}, dayOfCycle: {}",
                    player.getName().getString(),
                    player.isSleeping(),
                    sleepTime,
                    REQUIRED_SLEEP_TIME,
                    currentTime,
                    dayOfCycle
                );
            }
            hasLoggedSleepStatus = true;
        }

        // Reset logging flag when no one is sleeping
        if (!anyoneSleeping && hasLoggedSleepStatus) {
            hasLoggedSleepStatus = false;
        }

        // Check if all players have slept for the required time
        boolean allSleptLongEnough = chronodawnPlayers.stream()
            .allMatch(player -> sleepTimeMap.getOrDefault(player.getUUID(), 0) >= REQUIRED_SLEEP_TIME);

        if (allSleptLongEnough) {
            // Skip to morning (1000 ticks = early morning)
            long currentDayTime = level.getDayTime();
            long dayOfCycle = currentDayTime % 24000;

            // Calculate ticks until morning
            long ticksToMorning;
            if (dayOfCycle >= 0 && dayOfCycle < 1000) {
                // Already morning, skip to next morning
                ticksToMorning = 1000 - dayOfCycle + 24000;
            } else {
                // Skip to next morning
                ticksToMorning = 24000 - dayOfCycle + 1000;
            }

            long newDayTime = currentDayTime + ticksToMorning;

            // Request TimeDistortionEventHandler to set time on next tick
            // TimeDistortionEventHandler will handle the actual time change
            ChronoDawn.LOGGER.info("SleepMixin: Requesting sleep skip to time {}", newDayTime);
            TimeDistortionEventHandler.requestSleepSkip(level, newDayTime);

            // Reset sleep counters
            sleepTimeMap.clear();

            // Wake up all players in ChronoDawn
            for (ServerPlayer player : chronodawnPlayers) {
                player.stopSleepInBed(false, true);
            }

            ChronoDawn.LOGGER.info(
                "ChronoDawn: All {} players were sleeping, skipped to morning (dayTime: {} -> {})",
                chronodawnPlayers.size(),
                currentDayTime,
                newDayTime
            );
        }
    }
}
