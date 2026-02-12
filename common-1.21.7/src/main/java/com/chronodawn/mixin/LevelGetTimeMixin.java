package com.chronodawn.mixin;

import com.chronodawn.ChronoDawn;
import com.chronodawn.api.ChronoDawnTimeHolder;
import com.chronodawn.data.ChronoDawnTimeData;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept getDayTime() for independent time management in ChronoDawn.
 * Targets Level class where getDayTime() is defined.
 * Implements ChronoDawnTimeHolder to share time field with ServerLevelSetTimeMixin.
 *
 * 1.21.5 version: Does not include isDay() injection as that method was removed.
 */
@Mixin(value = Level.class, priority = 1100)
public abstract class LevelGetTimeMixin implements ChronoDawnTimeHolder {

    @Unique
    private Long chronodawn$independentTime = null;

    @Override
    public Long chronodawn$getIndependentTime() {
        return chronodawn$independentTime;
    }

    @Override
    public void chronodawn$setIndependentTime(Long time) {
        chronodawn$independentTime = time;
    }

    /**
     * Intercept getDayTime() and return independent time for ChronoDawn.
     */
    @Inject(method = "getDayTime", at = @At("RETURN"), cancellable = true)
    private void interceptGetDayTime(CallbackInfoReturnable<Long> cir) {
        Level level = (Level) (Object) this;

        // Check if this is ChronoDawn dimension (works for both ServerLevel and ClientLevel)
        if (level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            // Initialize with saved time if not set yet
            if (chronodawn$independentTime == null) {
                if (level instanceof ServerLevel serverLevel) {
                    // Load from SavedData on server side
                    ChronoDawnTimeData data = ChronoDawnTimeData.get(serverLevel);
                    chronodawn$independentTime = data.getIndependentTime();

                    // If SavedData is empty (first time), use current shared time
                    if (chronodawn$independentTime == 0L) {
                        chronodawn$independentTime = cir.getReturnValue();
                        data.setIndependentTime(chronodawn$independentTime);
                        ChronoDawn.LOGGER.debug("LevelGetTimeMixin [SERVER]: Initialized independent time to {} (from shared time)",
                            chronodawn$independentTime);
                    } else {
                        ChronoDawn.LOGGER.debug("LevelGetTimeMixin [SERVER]: Loaded independent time = {} (from SavedData)",
                            chronodawn$independentTime);
                    }
                } else {
                    // Client side: initialize with shared time (will be synced from server)
                    chronodawn$independentTime = cir.getReturnValue();
                }
            }

            // Return independent time (both server and client)
            cir.setReturnValue(chronodawn$independentTime);
        }
    }

    // Note: isDay() method was removed in 1.21.5, so we don't intercept it here.
    // Day/night checks in ChronoDawn dimension should use getDayTime() % 24000L < 12000L

    /**
     * Intercept getSkyDarken() to use independent time for sky brightness calculations.
     * This affects mob burning and other daylight-dependent mechanics.
     */
    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void interceptGetSkyDarken(CallbackInfoReturnable<Integer> cir) {
        Level level = (Level) (Object) this;

        if (level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            if (chronodawn$independentTime != null) {
                // Calculate sky darken based on independent time
                long timeOfDay = chronodawn$independentTime % 24000L;
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
