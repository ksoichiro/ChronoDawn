package com.chronodawn.entities.projectiles;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.TimeTyrantEntity;
import com.chronodawn.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Time Arrow Entity - Special arrow that applies time-based debuffs.
 *
 * When this arrow hits any living entity, it applies:
 * - Slowness II (3 seconds) - For normal mobs
 *
 * When hitting Time Tyrant specifically, it applies stronger effects:
 * - Slowness III (5 seconds)
 * - Weakness II (5 seconds)
 * - Glowing (10 seconds)
 *
 * These effects make combat more manageable by reducing movement speed
 * and attack damage, especially useful for boss battles.
 *
 * Reference: T171g - Boss Battle Balance Enhancement, T220-T222 - General mob support
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
        ChronoDawn.LOGGER.info("TimeArrowEntity constructed (no shooter)");
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
        super(entityType, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.pickup = AbstractArrow.Pickup.ALLOWED;
        ChronoDawn.LOGGER.info("TimeArrowEntity constructed with shooter: {}", shooter.getName().getString());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.TIME_ARROW.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        // Only apply effects on server side
        if (this.level().isClientSide()) {
            return;
        }

        ChronoDawn.LOGGER.info("Time Arrow hit entity: {}", result.getEntity().getType());

        // Apply effects to any living entity
        if (result.getEntity() instanceof LivingEntity target) {
            ChronoDawn.LOGGER.info("Time Arrow applying effects to LivingEntity: {}", target.getName().getString());

            // Check if the hit entity is Time Tyrant for stronger effects
            if (target instanceof TimeTyrantEntity) {
                ChronoDawn.LOGGER.info("Time Arrow hit Time Tyrant - applying strong effects");

                // Apply Slowness III for 5 seconds
                target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    100, // 5 seconds
                    2    // Slowness III
                ));

                // Apply Weakness II for 5 seconds
                target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    100, // 5 seconds
                    1    // Weakness II
                ));

                // Apply Glowing for 10 seconds
                target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    200, // 10 seconds
                    0    // Glowing I
                ));
            } else {
                ChronoDawn.LOGGER.info("Time Arrow hit normal mob - applying Slowness II");

                // Apply Slowness II for 3 seconds to normal mobs
                target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    60,  // 3 seconds
                    1    // Slowness II
                ));
            }
        } else {
            ChronoDawn.LOGGER.warn("Time Arrow hit non-living entity: {}", result.getEntity().getType());
        }
    }
}
