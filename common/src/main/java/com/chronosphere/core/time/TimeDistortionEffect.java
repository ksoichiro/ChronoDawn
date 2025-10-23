package com.chronosphere.core.time;

import com.chronosphere.registry.ModDimensions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/**
 * Time Distortion Effect logic for Chronosphere dimension.
 *
 * Applies Slowness IV effect to hostile mobs in the Chronosphere dimension,
 * creating a time-distorted environment where mobs move extremely slowly
 * while players maintain normal speed.
 *
 * Effect Properties:
 * - Effect: Slowness IV (60% movement speed reduction)
 * - Duration: 100 ticks (5 seconds, reapplied continuously)
 * - Target: Hostile mobs (Monster class) in Chronosphere dimension
 * - Exclusion: Players are not affected
 *
 * Reference: data-model.md (Time Distortion Effects)
 * Task: T073 [US1] Implement time distortion effect logic
 */
public class TimeDistortionEffect {
    /**
     * Duration of time distortion effect in ticks (100 ticks = 5 seconds).
     * Effect is continuously reapplied every tick.
     */
    private static final int EFFECT_DURATION = 100;

    /**
     * Slowness effect amplifier (IV = level 3 in code, displayed as IV in-game).
     * Level 3 = 60% movement speed reduction.
     */
    private static final int SLOWNESS_AMPLIFIER = 3; // Slowness IV

    /**
     * Apply time distortion effect to a living entity if applicable.
     *
     * This method should be called every tick for entities in the Chronosphere dimension.
     *
     * @param entity The living entity to potentially apply the effect to
     */
    public static void applyTimeDistortion(LivingEntity entity) {
        // Check if entity is in Chronosphere dimension
        if (!isInChronosphereDimension(entity)) {
            return;
        }

        // Check if entity is a hostile mob (not a player)
        if (!isHostileMob(entity)) {
            return;
        }

        // Apply Slowness IV effect
        entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, // Slowness effect
                EFFECT_DURATION,               // Duration in ticks
                SLOWNESS_AMPLIFIER,            // Amplifier (Slowness IV)
                false,                         // Ambient (particle visibility)
                false                          // Show particles
        ));
    }

    /**
     * Check if entity is in Chronosphere dimension.
     *
     * @param entity The entity to check
     * @return true if entity is in Chronosphere dimension
     */
    private static boolean isInChronosphereDimension(LivingEntity entity) {
        // FIXED: Use location() to compare ResourceLocation instead of ResourceKey
        return entity.level().dimension().location().equals(ModDimensions.CHRONOSPHERE_DIMENSION.location());
    }

    /**
     * Check if entity is a hostile mob (not a player).
     *
     * @param entity The entity to check
     * @return true if entity is a hostile mob
     */
    private static boolean isHostileMob(LivingEntity entity) {
        // Exclude players
        if (entity instanceof Player) {
            return false;
        }

        // Include hostile mobs (Monster class and subclasses)
        return entity instanceof Monster;
    }
}
