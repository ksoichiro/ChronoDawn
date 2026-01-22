package com.chronodawn.items.consumables;

import net.minecraft.world.item.Item;

/**
 * Time Wheat - Harvested crop item from mature Time Wheat crops.
 *
 * Properties:
 * - Non-edible material item (like vanilla wheat)
 * - Used for crafting Time Bread
 *
 * Acquisition:
 * - Harvesting mature Time Wheat crops (stage 7)
 *
 * Crafting Uses:
 * - Time Bread (3x Time Wheat → 1x Time Bread)
 * - Time Fruit Pie (3x Fruit of Time + 1x Wheat → 1x Time Fruit Pie)
 *
 * Note:
 * - This is the harvested result, not the seeds
 * - Similar to vanilla wheat (not edible, crafting material)
 *
 * Reference: spec.md (User Story 1 Enhancement, FR-035)
 * Task: T223 [US1] Create Time Wheat item
 */
public class TimeWheatItem extends Item {
    public TimeWheatItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Wheat.
     *
     * @return Item properties
     */
    public static Properties createProperties() {
        return new Properties();
    }
}
