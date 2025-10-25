package com.chronosphere.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Fruit of Time - Special food item found in the Chronosphere dimension.
 *
 * Provides:
 * - Nutrition: Restores hunger
 * - Haste I effect: Temporary mining speed boost
 *
 * Properties:
 * - Nutrition: 4 hunger points (2 drumsticks)
 * - Saturation: 0.6 (total 2.4)
 * - Effect: Haste I for 30 seconds
 * - Eating Speed: Normal
 *
 * Acquisition:
 * - Grows on special trees in the Chronosphere dimension
 * - Can be found naturally generated in Forgotten Library chests
 *
 * Reference: spec.md (User Story 1, FR-007)
 * Task: T076 [US1] Create Fruit of Time item
 */
public class FruitOfTimeItem extends Item {
    public FruitOfTimeItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Fruit of Time.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(4)        // 4 hunger points (2 drumsticks)
                .saturationModifier(0.6f)  // Saturation modifier (total: 4 * 0.6 = 2.4)
                .effect(
                        new MobEffectInstance(
                                MobEffects.DIG_SPEED,  // Haste I
                                30 * 20,               // 30 seconds (20 ticks/second)
                                0                      // Amplifier 0 = Haste I
                        ),
                        1.0f  // 100% chance to apply effect
                )
                .build();

        return new Properties()
                .food(foodProperties);
    }
}
