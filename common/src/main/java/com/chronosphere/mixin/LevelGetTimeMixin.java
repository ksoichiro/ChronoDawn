package com.chronosphere.mixin;

import com.chronosphere.Chronosphere;
import com.chronosphere.api.ChronosphereTimeHolder;
import com.chronosphere.data.ChronosphereTimeData;
import com.chronosphere.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept getDayTime() for independent time management in Chronosphere.
 * Targets Level class where getDayTime() is defined.
 * Implements ChronosphereTimeHolder to share time field with ServerLevelSetTimeMixin.
 *
 * Task: T301 [P] Fix bed sleeping mechanic in Chronosphere
 */
@Mixin(value = Level.class, priority = 1100)
public abstract class LevelGetTimeMixin implements ChronosphereTimeHolder {

    @Unique
    private Long chronosphere$independentTime = null;

    @Override
    public Long chronosphere$getIndependentTime() {
        return chronosphere$independentTime;
    }

    @Override
    public void chronosphere$setIndependentTime(Long time) {
        chronosphere$independentTime = time;
    }

    /**
     * Intercept getDayTime() and return independent time for Chronosphere.
     */
    @Inject(method = "getDayTime", at = @At("RETURN"), cancellable = true)
    private void interceptGetDayTime(CallbackInfoReturnable<Long> cir) {
        Level level = (Level) (Object) this;

        // Check if this is Chronosphere dimension (works for both ServerLevel and ClientLevel)
        if (level.dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
            // Initialize with saved time if not set yet
            if (chronosphere$independentTime == null) {
                if (level instanceof ServerLevel serverLevel) {
                    // Load from SavedData on server side
                    ChronosphereTimeData data = ChronosphereTimeData.get(serverLevel);
                    chronosphere$independentTime = data.getIndependentTime();

                    // If SavedData is empty (first time), use current shared time
                    if (chronosphere$independentTime == 0L) {
                        chronosphere$independentTime = cir.getReturnValue();
                        data.setIndependentTime(chronosphere$independentTime);
                        Chronosphere.LOGGER.info("LevelGetTimeMixin [SERVER]: Initialized independent time to {} (from shared time)",
                            chronosphere$independentTime);
                    } else {
                        Chronosphere.LOGGER.info("LevelGetTimeMixin [SERVER]: Loaded independent time = {} (from SavedData)",
                            chronosphere$independentTime);
                    }
                } else {
                    // Client side: initialize with shared time (will be synced from server)
                    chronosphere$independentTime = cir.getReturnValue();
                }
            }

            // Return independent time (both server and client)
            cir.setReturnValue(chronosphere$independentTime);
        }
    }

    /**
     * Intercept isDay() to use independent time for day/night checks.
     * Only applies to ServerLevel for now (client sync issue to be resolved later).
     */
    @Inject(method = "isDay", at = @At("RETURN"), cancellable = true)
    private void interceptIsDay(CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;

        // Only process ServerLevel for now (client-server sync needs custom packet)
        if (level instanceof ServerLevel && level.dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
            if (chronosphere$independentTime != null) {
                // Day is 0-12000, Night is 12000-24000 (within 24000 cycle)
                long timeOfDay = chronosphere$independentTime % 24000L;
                boolean isDay = timeOfDay < 12000L;
                cir.setReturnValue(isDay);
            }
        }
    }

    /**
     * Intercept getSkyDarken() to use independent time for sky brightness calculations.
     * This affects mob burning and other daylight-dependent mechanics.
     */
    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void interceptGetSkyDarken(CallbackInfoReturnable<Integer> cir) {
        Level level = (Level) (Object) this;

        if (level.dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
            if (chronosphere$independentTime != null) {
                // Calculate sky darken based on independent time
                long timeOfDay = chronosphere$independentTime % 24000L;
                int skyDarken = calculateSkyDarken(timeOfDay);
                cir.setReturnValue(skyDarken);
            }
        }
    }

    /**
     * Calculate sky darken value based on time of day.
     * Returns 0-11, where 0 is brightest (day) and 11 is darkest (night).
     *
     * Simplified calculation:
     * - Day (0-12000): 0 (bright)
     * - Night (12000-24000): 11 (dark)
     */
    @Unique
    private int calculateSkyDarken(long timeOfDay) {
        // Simple day/night calculation
        // Day: 0-12000 → skyDarken = 0
        // Night: 12000-24000 → skyDarken = 11
        return timeOfDay < 12000L ? 0 : 11;
    }
}
