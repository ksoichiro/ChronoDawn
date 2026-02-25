package com.chronodawn.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Time Fruit Pie - Crafted food item from Time Fruits and wheat.
 *
 * Provides:
 * - Nutrition: Restores 8 hunger points (4 drumsticks)
 * - Haste II effect: Strong mining speed boost for 30 seconds
 *
 * Properties:
 * - Nutrition: 8 hunger points (4 drumsticks)
 * - Saturation: 0.8 (total 6.4)
 * - Effect: Haste II for 30 seconds
 * - Eating Speed: Normal
 *
 * Crafting:
 * - Recipe: 3x Fruit of Time + 1x Wheat
 *
 * Note:
 * - This is a more powerful version of Fruit of Time
 * - Provides higher nutrition and stronger Haste effect
 * - Encourages players to gather and craft with Time Fruits
 *
 * Reference: spec.md (User Story 1 Enhancement, FR-034)
 * Task: T220 [US1] Create Time Fruit Pie item
 */
public class TimeFruitPieItem extends Item {
    public TimeFruitPieItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Fruit Pie.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(8)        // 8 hunger points (4 drumsticks)
                .saturationMod(0.8f)  // Saturation modifier (total: 8 * 0.8 = 6.4)
                .effect(
                        new MobEffectInstance(
                                MobEffects.DIG_SPEED,  // Haste II
                                30 * 20,               // 30 seconds (20 ticks/second)
                                1                      // Amplifier 1 = Haste II
                        ),
                        1.0f  // 100% chance to apply effect
                )
                .build();

        return new Properties()
                .food(foodProperties);
    }
}
