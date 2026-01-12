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
 * Time Distortion Effect logic for ChronoDawn dimension.
 *
 * Applies Slowness IV effect to hostile mobs in the ChronoDawn dimension,
 * creating a time-distorted environment where mobs move extremely slowly
 * while players maintain normal speed.
 *
 * Effect Properties:
 * - Effect: Slowness IV (60% movement speed reduction) - base effect
 * - Effect: Slowness V (75% movement speed reduction) - enhanced with Eye of Chronos
 * - Duration: 100 ticks (5 seconds, reapplied continuously)
 * - Target: Hostile mobs (Monster class) in ChronoDawn dimension
 * - Exclusion: Players are not affected
 * - Exclusion: Players wearing full Enhanced Clockstone armor are immune (T254)
 * - Exclusion: All boss entities (Time Guardian, Time Tyrant, Chronos Warden, Clockwork Colossus, Entropy Keeper, Temporal Phantom)
 * - Exclusion: Time Keeper (friendly trader NPC)
 *
 * Eye of Chronos Enhancement:
 * - When Eye of Chronos is obtained for the first time, the dimension is permanently enhanced
 * - Enhancement is stored in DimensionStateData (persistent, world-wide, multiplayer-safe)
 * - All hostile mobs receive Slowness V instead of Slowness IV
 * - Slowness IV: 60% movement speed reduction
 * - Slowness V: 75% movement speed reduction
 *
 * Reference: data-model.md (Time Distortion Effects)
 * Task: T073 [US1] Implement time distortion effect logic
 * Task: T147 [US3] Implement enhanced time distortion effect (Slowness V) when Eye of Chronos is obtained
 * Task: T229a [US3] Exclude Time Tyrant from time distortion effect to allow boss abilities to function
 * Task: T254 [US2] Implement time-manipulation effects for Tier 2 equipment (Enhanced Clockstone armor immunity)
 * Task: T714 [Playtest Feedback] Exclude all boss entities from Time Distortion Effect
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
     * This method should be called every tick for entities in the ChronoDawn dimension.
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

        // Check dimension state for Eye of Chronos enhancement (permanent, world-wide)
        boolean isEnhanced = isDimensionEnhanced(entity);

        // Apply Slowness effect (IV or V depending on dimension enhancement state)
        int amplifier = isEnhanced ? ENHANCED_SLOWNESS_AMPLIFIER : SLOWNESS_AMPLIFIER;
        entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, // Slowness effect
                EFFECT_DURATION,               // Duration in ticks
                amplifier,                     // Amplifier (Slowness IV or V)
                false,                         // Ambient (particle visibility)
                false                          // Show particles
        ));
    }

    /**
     * Check if entity is in ChronoDawn dimension.
     *
     * @param entity The entity to check
     * @return true if entity is in ChronoDawn dimension
     */
    private static boolean isInChronoDawnDimension(LivingEntity entity) {
        // FIXED: Use location() to compare ResourceLocation instead of ResourceKey
        return entity.level().dimension().location().equals(ModDimensions.CHRONO_DAWN_DIMENSION.location());
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

        // Exclude all boss entities (bosses should move at normal speed)
        if (entity instanceof TimeGuardianEntity) {
            ChronoDawn.LOGGER.debug("[TimeDistortion] Excluding Time Guardian from time distortion");
            return false;
        }
        if (entity instanceof com.chronodawn.entities.bosses.TimeTyrantEntity) {
            ChronoDawn.LOGGER.debug("[TimeDistortion] Excluding Time Tyrant from time distortion");
            return false;
        }
        if (entity instanceof ChronosWardenEntity) {
            ChronoDawn.LOGGER.debug("[TimeDistortion] Excluding Chronos Warden from time distortion");
            return false;
        }
        if (entity instanceof ClockworkColossusEntity) {
            ChronoDawn.LOGGER.debug("[TimeDistortion] Excluding Clockwork Colossus from time distortion");
            return false;
        }
        if (entity instanceof EntropyKeeperEntity) {
            ChronoDawn.LOGGER.debug("[TimeDistortion] Excluding Entropy Keeper from time distortion");
            return false;
        }
        if (entity instanceof TemporalPhantomEntity) {
            ChronoDawn.LOGGER.debug("[TimeDistortion] Excluding Temporal Phantom from time distortion");
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
     * Check if ChronoDawn dimension has been enhanced with Eye of Chronos.
     * This checks the persistent dimension state, not player inventory.
     *
     * @param entity The entity (used to get the dimension)
     * @return true if dimension is enhanced (Slowness V active)
     */
    private static boolean isDimensionEnhanced(LivingEntity entity) {
        // Only check on server side
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Get dimension state data
        DimensionStateData dimensionState = DimensionStateData.get(serverLevel);

        // Check if time distortion level is enhanced (Slowness V)
        return dimensionState.getTimeDistortionLevel() == DimensionStateData.TimeDistortionLevel.SLOWNESS_V;
    }
}
