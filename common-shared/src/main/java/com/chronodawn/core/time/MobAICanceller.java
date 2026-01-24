package com.chronodawn.core.time;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Utility class for cancelling mob attack AI.
 *
 * Used by Time Clock item to forcibly cancel the next attack AI routine
 * of all mobs within a specified radius.
 *
 * Implementation Strategy:
 * 1. Find all hostile mobs within radius
 * 2. Clear their current attack target
 * 3. Apply brief Weakness effect to prevent immediate retaliation
 *
 * Reference: data-model.md (Items → Tools → Time Clock)
 */
public class MobAICanceller {
    /**
     * Duration of Weakness effect in ticks (2 seconds = 40 ticks).
     * This prevents mobs from immediately retaliating after AI cancellation.
     */
    private static final int WEAKNESS_DURATION_TICKS = 40;

    /**
     * Cancel attack AI for all hostile mobs within radius.
     *
     * This method:
     * 1. Finds all Mob entities within the specified radius
     * 2. Clears their current attack target
     * 3. Applies brief Weakness I effect
     *
     * @param level The world level
     * @param center The center position (usually player position)
     * @param radius The effect radius in blocks
     */
    public static void cancelAttackAI(Level level, Vec3 center, double radius) {
        // Create bounding box for area of effect
        AABB boundingBox = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        // Find all mobs within radius
        List<Mob> mobs = level.getEntitiesOfClass(
                Mob.class,
                boundingBox,
                mob -> mob.distanceToSqr(center) <= radius * radius
        );

        // Cancel AI for each mob
        for (Mob mob : mobs) {
            // Clear current attack target
            mob.setTarget(null);

            // Apply Weakness I effect to prevent immediate retaliation
            MobEffectInstance weakness = new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    WEAKNESS_DURATION_TICKS,
                    0, // Amplifier 0 = Weakness I
                    false, // Not ambient
                    false // Do not show particles (subtle effect)
            );
            mob.addEffect(weakness);
        }
    }
}
