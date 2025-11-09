package com.chronosphere.items.consumables;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Time Bread - Basic crafted food item from Time Wheat.
 *
 * Provides:
 * - Nutrition: Restores 5 hunger points (2.5 drumsticks)
 * - No special effects
 *
 * Properties:
 * - Nutrition: 5 hunger points (2.5 drumsticks)
 * - Saturation: 0.6 (total 3.0)
 * - Effect: None
 * - Eating Speed: Normal
 *
 * Crafting:
 * - Recipe: 3x Time Wheat → 1x Time Bread
 *
 * Note:
 * - This is a basic food item (similar to vanilla bread)
 * - Provides moderate nutrition without special effects
 * - Easy to craft for early-game sustenance
 *
 * Reference: spec.md (User Story 1 Enhancement, FR-035)
 * Task: T224 [US1] Create Time Bread item
 */
public class TimeBreadItem extends Item {
    public TimeBreadItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Bread.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(5)        // 5 hunger points (2.5 drumsticks)
                .saturationModifier(0.6f)  // Saturation modifier (total: 5 * 0.6 = 3.0)
                .build();

        return new Properties()
                .food(foodProperties);
    }
}
