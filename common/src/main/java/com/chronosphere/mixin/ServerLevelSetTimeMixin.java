package com.chronosphere.mixin;

import com.chronosphere.Chronosphere;
import com.chronosphere.api.ChronosphereTimeHolder;
import com.chronosphere.data.ChronosphereTimeData;
import com.chronosphere.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept setDayTime() for independent time management in Chronosphere.
 * Targets ServerLevel class where setDayTime() is defined.
 * Uses ChronosphereTimeHolder interface to access shared time field from LevelGetTimeMixin.
 *
 * Task: T301 [P] Fix bed sleeping mechanic in Chronosphere
 */
@Mixin(value = ServerLevel.class, priority = 1100)
public abstract class ServerLevelSetTimeMixin {

    /**
     * Intercept setDayTime() and store to independent time for Chronosphere.
     * Saves to both memory (via ChronosphereTimeHolder) and disk (via SavedData).
     * Cancels the call to prevent modifying shared time (which affects Overworld).
     */
    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    private void interceptSetDayTime(long time, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;

        if (level.dimension().equals(ModDimensions.CHRONOSPHERE_DIMENSION)) {
            // Store in memory field (for fast access)
            ChronosphereTimeHolder holder = (ChronosphereTimeHolder) level;
            holder.chronosphere$setIndependentTime(time);

            // Save to disk (for persistence)
            ChronosphereTimeData data = ChronosphereTimeData.get(level);
            data.setIndependentTime(time);

            // Cancel to prevent shared time modification
            ci.cancel();
        }
    }
}
