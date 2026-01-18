package com.chronodawn.compat.v1_21_2.items.combat;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.projectiles.TimeArrowEntity;
import com.chronodawn.registry.ModEntities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Time Arrow Item - Special arrow for fighting Time Tyrant.
 *
 * Crafting Recipe:
 * - Top: Clockstone
 * - Center: Fruit of Time
 * - Bottom: Arrow
 * - Output: 4x Time Arrow
 *
 * Effect (when hitting Time Tyrant):
 * - Slowness III (5 seconds) - Reduces boss movement speed
 * - Weakness II (5 seconds) - Reduces boss attack damage
 * - Glowing (10 seconds) - Makes boss easier to track
 *
 * Properties:
 * - Stack Size: 64
 * - Can be shot from any bow
 *
 * Reference: T171g - Boss Battle Balance Enhancement
 */
public class TimeArrowItem extends ArrowItem {
    public TimeArrowItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Arrow item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties();
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter, ItemStack weapon) {
        ChronoDawn.LOGGER.info("TimeArrowItem.createArrow called - creating TimeArrowEntity (weapon: {})", weapon.getItem());
        TimeArrowEntity arrow = new TimeArrowEntity(ModEntities.TIME_ARROW.get(), shooter, level, stack.copyWithCount(1));
        ChronoDawn.LOGGER.info("Created TimeArrowEntity: {}", arrow.getClass().getName());
        return arrow;
    }
}
