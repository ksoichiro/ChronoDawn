package com.chronodawn.mixin;

import com.chronodawn.ChronoDawn;
import com.chronodawn.api.ChronoDawnTimeHolder;
import com.chronodawn.data.ChronoDawnTimeData;
import com.chronodawn.registry.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept setDayTime() for independent time management in ChronoDawn.
 * Targets ServerLevel class where setDayTime() is defined.
 * Uses ChronoDawnTimeHolder interface to access shared time field from LevelGetTimeMixin.
 *
 * Task: T301 [P] Fix bed sleeping mechanic in ChronoDawn
 */
@Mixin(value = ServerLevel.class, priority = 1100)
public abstract class ServerLevelSetTimeMixin {

    /**
     * Intercept setDayTime() and store to independent time for ChronoDawn.
     * Saves to both memory (via ChronoDawnTimeHolder) and disk (via SavedData).
     * Cancels the call to prevent modifying shared time (which affects Overworld).
     */
    @Inject(method = "setDayTime", at = @At("HEAD"), cancellable = true)
    private void interceptSetDayTime(long time, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;

        if (level.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            // Store in memory field (for fast access)
            ChronoDawnTimeHolder holder = (ChronoDawnTimeHolder) level;
            holder.chronodawn$setIndependentTime(time);

            // Save to disk (for persistence)
            ChronoDawnTimeData data = ChronoDawnTimeData.get(level);
            data.setIndependentTime(time);

            // Cancel to prevent shared time modification
            ci.cancel();
        }
    }
}
