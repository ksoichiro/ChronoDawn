package com.chronodawn.core.time;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.DimensionStateData;
import com.chronodawn.entities.bosses.ChronosWardenEntity;
import com.chronodawn.entities.bosses.ClockworkColossusEntity;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import com.chronodawn.entities.bosses.TemporalPhantomEntity;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.entities.mobs.FloqEntity;
import com.chronodawn.entities.mobs.TimeKeeperEntity;
import com.chronodawn.items.equipment.EnhancedClockstoneArmorItem;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/**
 * Time Distortion Effect logic for ChronoDawn dimension (1.21.5 version).
 *
 * Applies Slowness IV effect to hostile mobs in the ChronoDawn dimension,
 * creating a time-distorted environment where mobs move extremely slowly
 * while players maintain normal speed.
 *
 * Note: In 1.21.5, MobEffects.MOVEMENT_SLOWDOWN was renamed to MobEffects.SLOWNESS.
 */
public class TimeDistortionEffect {
    /**
     * Duration of time distortion effect in ticks (100 ticks = 5 seconds).
     */
    private static final int EFFECT_DURATION = 100;

    /**
     * Slowness effect amplifier (IV = level 3 in code).
     */
    private static final int SLOWNESS_AMPLIFIER = 3; // Slowness IV

    /**
     * Enhanced slowness effect amplifier with Eye of Chronos (V = level 4).
     */
    private static final int ENHANCED_SLOWNESS_AMPLIFIER = 4; // Slowness V

    /**
     * Apply time distortion effect to a living entity if applicable.
     *
     * @param entity The living entity to potentially apply the effect to
     */
    public static void applyTimeDistortion(LivingEntity entity) {
        // Check if entity is in ChronoDawn dimension
        if (!isInChronoDawnDimension(entity)) {
            return;
        }

        // Check if entity is a hostile mob (not a player)
        if (!isHostileMob(entity)) {
            return;
        }

        // Check dimension state for Eye of Chronos enhancement
        boolean isEnhanced = isDimensionEnhanced(entity);

        // Apply Slowness effect (IV or V depending on dimension enhancement state)
        // In 1.21.5, MobEffects.SLOWNESS is the correct constant (renamed from MOVEMENT_SLOWDOWN)
        int amplifier = isEnhanced ? ENHANCED_SLOWNESS_AMPLIFIER : SLOWNESS_AMPLIFIER;
        entity.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,    // Slowness effect (renamed in 1.21.5)
                EFFECT_DURATION,         // Duration in ticks
                amplifier,               // Amplifier (Slowness IV or V)
                false,                   // Ambient
                false                    // Show particles
        ));
    }

    /**
     * Check if entity is in ChronoDawn dimension.
     */
    private static boolean isInChronoDawnDimension(LivingEntity entity) {
        return entity.level().dimension().location().equals(ModDimensions.CHRONO_DAWN_DIMENSION.location());
    }

    /**
     * Check if entity is a hostile mob (not a player, boss, or friendly NPC).
     */
    private static boolean isHostileMob(LivingEntity entity) {
        // Exclude players
        if (entity instanceof Player) {
            return false;
        }

        // Exclude all boss entities
        if (entity instanceof TimeGuardianEntity) {
            return false;
        }
        if (entity instanceof com.chronodawn.entities.bosses.TimeTyrantEntity) {
            return false;
        }
        if (entity instanceof ChronosWardenEntity) {
            return false;
        }
        if (entity instanceof ClockworkColossusEntity) {
            return false;
        }
        if (entity instanceof EntropyKeeperEntity) {
            return false;
        }
        if (entity instanceof TemporalPhantomEntity) {
            return false;
        }

        // Exclude Time Keeper (friendly trader NPC)
        if (entity instanceof TimeKeeperEntity) {
            return false;
        }

        // Exclude Floq
        if (entity instanceof FloqEntity) {
            return false;
        }

        // Include hostile mobs
        return entity instanceof Monster;
    }

    /**
     * Check if ChronoDawn dimension has been enhanced with Eye of Chronos.
     */
    private static boolean isDimensionEnhanced(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        DimensionStateData dimensionState = DimensionStateData.get(serverLevel);
        return dimensionState.getTimeDistortionLevel() == DimensionStateData.TimeDistortionLevel.SLOWNESS_V;
    }
}
