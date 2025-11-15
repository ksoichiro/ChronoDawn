package com.chronosphere.entities.projectiles;

import com.chronosphere.entities.bosses.TimeTyrantEntity;
import com.chronosphere.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Time Arrow Entity - Special arrow that applies debuffs to Time Tyrant.
 *
 * When this arrow hits Time Tyrant, it applies:
 * - Slowness III (5 seconds)
 * - Weakness II (5 seconds)
 * - Glowing (10 seconds)
 *
 * These effects make the boss battle more manageable by reducing boss movement
 * speed, attack damage, and making the boss easier to track.
 *
 * Reference: T171g - Boss Battle Balance Enhancement
 */
public class TimeArrowEntity extends AbstractArrow {
    /**
     * Constructor for creating Time Arrow entity.
     *
     * @param entityType The entity type
     * @param level The world level
     */
    public TimeArrowEntity(EntityType<? extends TimeArrowEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Constructor for creating Time Arrow entity from a shooter.
     *
     * @param entityType The entity type
     * @param shooter The entity that shot the arrow
     * @param level The world level
     * @param pickupItemStack The item stack for pickup
     */
    public TimeArrowEntity(EntityType<? extends TimeArrowEntity> entityType, LivingEntity shooter, Level level, ItemStack pickupItemStack) {
        super(entityType, shooter, level, pickupItemStack, null);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.TIME_ARROW.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        // Check if the hit entity is Time Tyrant
        if (result.getEntity() instanceof TimeTyrantEntity timeTyrant) {
            // Apply Slowness III for 5 seconds
            timeTyrant.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                100, // 5 seconds
                2    // Slowness III
            ));

            // Apply Weakness II for 5 seconds
            timeTyrant.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                100, // 5 seconds
                1    // Weakness II
            ));

            // Apply Glowing for 10 seconds
            timeTyrant.addEffect(new MobEffectInstance(
                MobEffects.GLOWING,
                200, // 10 seconds
                0    // Glowing I
            ));
        }
    }
}
