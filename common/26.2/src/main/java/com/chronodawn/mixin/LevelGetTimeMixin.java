package com.chronodawn.mixin;

import com.chronodawn.registry.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept getSkyDarken() for the ChronoDawn dimension.
 * Targets Level class where getSkyDarken() is defined.
 *
 * 26.2: getDayTime()/setDayTime() were removed in favor of the new world clock
 * system (net.minecraft.world.clock). The ChronoDawn dimension now has its own
 * dedicated clock (data/chronodawn/world_clock/chronodawn.json, referenced via
 * dimension_type's "default_clock"), so it no longer shares a single mutable time
 * field with the Overworld — TimeDistortionEventHandler reads/writes it directly
 * through ServerLevel#clockManager(), and this mixin no longer needs to fake an
 * "independent time" value via ChronoDawnTimeHolder. Only getSkyDarken() still
 * needs interception, since sky darkness isn't derived from the clock automatically
 * for a dimension with has_skylight but a non-default clock rate.
 */
@Mixin(value = Level.class, priority = 1100)
public abstract class LevelGetTimeMixin {

    /**
     * Intercept getSkyDarken() to use ChronoDawn's own clock for sky brightness
     * calculations. This affects mob burning and other daylight-dependent mechanics.
     */
    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void interceptGetSkyDarken(CallbackInfoReturnable<Integer> cir) {
        Level level = (Level) (Object) this;

        if (level instanceof ServerLevel serverLevel
                && serverLevel.dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            long timeOfDay = serverLevel.clockManager().getTotalTicks(chronoDawnClockHolder(serverLevel)) % 24000L;
            cir.setReturnValue(calculateSkyDarken(timeOfDay));
        }
    }

    @Unique
    private static Holder<WorldClock> chronoDawnClockHolder(ServerLevel level) {
        return level.dimensionType().defaultClock().orElseGet(() ->
            level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD));
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
    private static int calculateSkyDarken(long timeOfDay) {
        return timeOfDay < 12000L ? 0 : 11;
    }
}
