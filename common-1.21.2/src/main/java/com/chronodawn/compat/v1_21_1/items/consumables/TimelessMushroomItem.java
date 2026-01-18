package com.chronodawn.compat.v1_21_1.items.consumables;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;

/**
 * Timeless Mushroom - Edible mushroom that can be planted or eaten.
 *
 * Provides:
 * - Nutrition: Restores 2 hunger points (1 drumstick)
 * - No special effects when eaten raw
 *
 * Properties:
 * - Nutrition: 2 hunger points (1 drumstick)
 * - Saturation: 0.3 (total 0.6)
 * - Effect: None (raw state)
 * - Eating Speed: Normal
 * - Plantable: Yes (can be planted in dark areas)
 *
 * Acquisition:
 * - Breaking Timeless Mushroom blocks
 * - Finding in world generation (dark forest areas)
 * - Mushroom spreading in darkness
 *
 * Usage:
 * - Can be eaten raw for basic nutrition
 * - Can be planted in dark areas (light level 12 or less)
 * - Can be used in Timeless Mushroom Soup recipe
 * - Can be used in Temporal Root Stew recipe
 *
 * Visual Theme:
 * - Silver/white color with faint glow
 * - Distinct from Unstable Fungus (purple/blue)
 * - "Timeless" theme = pale, ethereal, ghost-like
 *
 * Note:
 * - This item serves both as food and plantable mushroom
 * - Extends ItemNameBlockItem to allow planting
 * - Different from Unstable Fungus (which is not edible)
 *
 * Reference: WORK_NOTES.md (Crop 3: Timeless Mushroom)
 * Task: T212 [US1] Create Timeless Mushroom item
 */
public class TimelessMushroomItem extends ItemNameBlockItem {
    public TimelessMushroomItem(Properties properties) {
        super(ModBlocks.TIMELESS_MUSHROOM.get(), properties);
    }

    /**
     * Create default properties for Timeless Mushroom.
     *
     * @return Item properties with food configuration
     */
    public static Properties createProperties() {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .nutrition(2)        // 2 hunger points (1 drumstick)
                .saturationModifier(0.3f)  // Saturation modifier (total: 2 * 0.3 = 0.6)
                .build();

        return new Properties()
                .food(foodProperties);
    }
}
