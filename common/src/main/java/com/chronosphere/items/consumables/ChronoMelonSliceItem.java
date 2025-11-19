package com.chronosphere.items.consumables;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Chrono Melon Slice - Basic food item from Chrono Melon.
 *
 * Provides:
 * - Nutrition: Restores 2 hunger points (1 drumstick)
 * - No special effects
 *
 * Properties:
 * - Nutrition: 2 hunger points (1 drumstick)
 * - Saturation: 0.3 (total 0.6)
 * - Effect: None
 * - Eating Speed: Fast (1.6 seconds, same as melon slice)
 *
 * Acquisition:
 * - Breaking Chrono Melon blocks (3-7 slices)
 * - Crafting: 1x Chrono Melon Block → 9x Chrono Melon Slices
 * - Fortune enchantment increases drop count
 *
 * Usage:
 * - Quick snack food (fast eating speed)
 * - Crafting ingredient for Chrono Melon Juice and other foods
 * - Can be crafted back into Chrono Melon Block (9 slices → 1 block)
 * - Can be crafted into Chrono Melon Seeds (1 slice → 1 seeds)
 *
 * Note:
 * - Similar to vanilla melon slice but with time theme
 * - Fast eating speed makes it good for emergency food
 *
 * Reference: WORK_NOTES.md (Crop 2: Chrono Melon)
 * Task: T212 [US1] Create Chrono Melon Slice item
 */
public class ChronoMelonSliceItem extends Item {
    public ChronoMelonSliceItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Chrono Melon Slice.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(2)        // 2 hunger points (1 drumstick)
                .saturationModifier(0.3f)  // Saturation modifier (total: 2 * 0.3 = 0.6)
                .fast()              // Fast eating speed (1.6 seconds instead of 3.2)
                .build();

        return new Properties()
                .food(foodProperties);
    }
}
