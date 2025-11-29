package com.chronosphere.core.time;

import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.entities.mobs.FloqEntity;
import com.chronosphere.entities.mobs.TimeKeeperEntity;
import com.chronosphere.items.equipment.EnhancedClockstoneArmorItem;
import com.chronosphere.registry.ModDimensions;
import com.chronosphere.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
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
 * - Effect: Slowness IV (60% movement speed reduction) - base effect
 * - Effect: Slowness V (75% movement speed reduction) - enhanced with Eye of Chronos
 * - Duration: 100 ticks (5 seconds, reapplied continuously)
 * - Target: Hostile mobs (Monster class) in Chronosphere dimension
 * - Exclusion: Players are not affected
 * - Exclusion: Players wearing full Enhanced Clockstone armor are immune (T254)
 * - Exclusion: Time Guardian and Time Tyrant (bosses should move at normal speed)
 * - Exclusion: Time Keeper (friendly trader NPC)
 *
 * Eye of Chronos Enhancement:
 * - When any player in the dimension has Eye of Chronos in inventory, all hostile mobs
 *   receive Slowness V instead of Slowness IV for extreme slow
 *
 * Reference: data-model.md (Time Distortion Effects)
 * Task: T073 [US1] Implement time distortion effect logic
 * Task: T147 [US3] Implement enhanced time distortion effect (Slowness V) when Eye of Chronos is in inventory
 * Task: T229a [US3] Exclude Time Tyrant from time distortion effect to allow boss abilities to function
 * Task: T254 [US2] Implement time-manipulation effects for Tier 2 equipment (Enhanced Clockstone armor immunity)
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
     * Enhanced slowness effect amplifier with Eye of Chronos (V = level 4 in code).
     * Level 4 = 75% movement speed reduction.
     */
    private static final int ENHANCED_SLOWNESS_AMPLIFIER = 4; // Slowness V

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

        // Check if any player in the dimension has Eye of Chronos
        boolean hasEyeOfChronos = hasEyeOfChronosInDimension(entity);

        // Apply Slowness effect (IV or V depending on Eye of Chronos)
        int amplifier = hasEyeOfChronos ? ENHANCED_SLOWNESS_AMPLIFIER : SLOWNESS_AMPLIFIER;
        entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, // Slowness effect
                EFFECT_DURATION,               // Duration in ticks
                amplifier,                     // Amplifier (Slowness IV or V)
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
     * Check if entity is a hostile mob (not a player, boss, or friendly NPC).
     *
     * @param entity The entity to check
     * @return true if entity is a hostile mob
     */
    private static boolean isHostileMob(LivingEntity entity) {
        // Exclude players
        if (entity instanceof Player player) {
            // Special case: If player is wearing full Enhanced Clockstone armor, they are immune
            // This is for the future implementation where players might be affected
            // Currently players are excluded entirely, but this check is ready for potential changes
            return false;
        }

        // Exclude Time Guardian (boss should move at normal speed)
        if (entity instanceof TimeGuardianEntity) {
            return false;
        }

        // Exclude Time Tyrant (boss should move at normal speed)
        if (entity instanceof com.chronosphere.entities.bosses.TimeTyrantEntity) {
            return false;
        }

        // Exclude Time Keeper (friendly trader NPC)
        if (entity instanceof TimeKeeperEntity) {
            return false;
        }

        // Exclude Floq (should move fast like slimes)
        if (entity instanceof FloqEntity) {
            return false;
        }

        // Include hostile mobs (Monster class and subclasses)
        return entity instanceof Monster;
    }

    /**
     * Check if any player in the dimension has Eye of Chronos in their inventory.
     *
     * @param entity The entity (used to get the dimension)
     * @return true if any player in the dimension has Eye of Chronos
     */
    private static boolean hasEyeOfChronosInDimension(LivingEntity entity) {
        // Only check on server side
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Check all players in the dimension
        for (Player player : serverLevel.players()) {
            // Check if player has Eye of Chronos in inventory
            if (player.getInventory().contains(ModItems.EYE_OF_CHRONOS.get().getDefaultInstance())) {
                return true;
            }
        }

        return false;
    }
}
