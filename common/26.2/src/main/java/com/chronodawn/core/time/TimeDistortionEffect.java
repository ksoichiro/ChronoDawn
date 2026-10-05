package com.chronodawn.core.time;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.TimeDistortionSettings;
import com.chronodawn.core.time.TimeDistortionPolicy.Kind;
import com.chronodawn.core.time.TimeDistortionPolicy.Outcome;
import com.chronodawn.core.time.TimeDistortionPolicy.Tier;
import com.chronodawn.data.DimensionStateData;
import com.chronodawn.events.TimeDistortionEventHandler;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.tags.ModEntityTypeTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/**
 * Applies the ambient Time Distortion effect to mobs in the Chrono Dawn dimension.
 *
 * <p>Which effect and level apply is decided by {@link TimeDistortionPolicy} from the
 * current day/night cycle speed, the Eye of Chronos enhancement, and the mob's tier
 * ({@code time_distortion_immune} / {@code time_distortion_resistant} tags). Instances
 * are ambient and short, and are reapplied every tick.
 */
public class TimeDistortionEffect {
    private static final int NO_EFFECT = -1;

    public static void applyTimeDistortion(LivingEntity entity) {
        TimeDistortionSettings settings = ChronoDawnConfig.get().gameplay().timeDistortion();
        if (!settings.enabled()) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!isInChronoDawnDimension(entity) || !isAffectedMob(entity, settings.scope())) {
            return;
        }
        Tier tier = tierOf(entity);
        if (tier == Tier.IMMUNE) {
            return;
        }

        boolean timeFlowEnabled = ChronoDawnConfig.get().gameplay().timeFlow().enabled();
        float timeSpeed = TimeDistortionEventHandler.getCurrentSpeed(serverLevel);
        Outcome outcome = TimeDistortionPolicy.outcome(
            TimeDistortionPolicy.band(timeFlowEnabled, timeSpeed, settings),
            isDimensionEnhanced(serverLevel),
            tier,
            settings
        );

        syncSlowness(entity, outcome.kind() == Kind.SLOWNESS ? outcome.amplifier() : NO_EFFECT);
        syncSpeed(entity, outcome.kind() == Kind.SPEED ? outcome.amplifier() : NO_EFFECT);
    }

    // Slowness and Speed need separate methods because the effect handle type differs
    // between Minecraft eras (MobEffect vs Holder<MobEffect>) and this file is shared.
    private static void syncSlowness(LivingEntity entity, int amplifier) {
        MobEffectInstance current = entity.getEffect(MobEffects.SLOWNESS);
        if (current != null && TimeDistortionPolicy.isReplaceableAmbient(
            current.isAmbient(), current.getDuration(), current.getAmplifier(), amplifier)) {
            entity.removeEffect(MobEffects.SLOWNESS);
        }
        if (amplifier != NO_EFFECT) {
            entity.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS, TimeDistortionPolicy.AMBIENT_DURATION_TICKS, amplifier, true, false
            ));
        }
    }

    private static void syncSpeed(LivingEntity entity, int amplifier) {
        MobEffectInstance current = entity.getEffect(MobEffects.SPEED);
        if (current != null && TimeDistortionPolicy.isReplaceableAmbient(
            current.isAmbient(), current.getDuration(), current.getAmplifier(), amplifier)) {
            entity.removeEffect(MobEffects.SPEED);
        }
        if (amplifier != NO_EFFECT) {
            // Visible particles are one of the cues that mobs have sped up.
            entity.addEffect(new MobEffectInstance(
                MobEffects.SPEED, TimeDistortionPolicy.AMBIENT_DURATION_TICKS, amplifier, true, true
            ));
        }
    }

    private static Tier tierOf(LivingEntity entity) {
        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(ModEntityTypeTags.TIME_DISTORTION_IMMUNE)) {
            return Tier.IMMUNE;
        }
        if (BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType()).is(ModEntityTypeTags.TIME_DISTORTION_RESISTANT)) {
            return Tier.RESISTANT;
        }
        return Tier.NORMAL;
    }

    private static boolean isInChronoDawnDimension(LivingEntity entity) {
        return entity.level().dimension().identifier().equals(ModDimensions.CHRONO_DAWN_DIMENSION.identifier());
    }

    private static boolean isAffectedMob(LivingEntity entity, TimeDistortionSettings.Scope scope) {
        if (entity instanceof Player) {
            return false;
        }
        return scope == TimeDistortionSettings.Scope.HOSTILE_MOBS
            ? entity instanceof Monster
            : entity instanceof net.minecraft.world.entity.Mob;
    }

    private static boolean isDimensionEnhanced(ServerLevel level) {
        return DimensionStateData.get(level).getTimeDistortionLevel()
            == DimensionStateData.TimeDistortionLevel.SLOWNESS_V;
    }
}
