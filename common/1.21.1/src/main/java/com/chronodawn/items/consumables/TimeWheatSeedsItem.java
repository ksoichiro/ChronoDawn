package com.chronodawn.items.consumables;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.item.ItemNameBlockItem;

/**
 * Time Wheat Seeds - Seeds for planting Time Wheat crops.
 *
 * Properties:
 * - Can be planted on farmland
 * - Grows into Time Wheat crop (8 stages)
 * - Dropped from breaking Time Wheat crops at any stage
 *
 * Acquisition:
 * - Breaking Time Wheat crops (any stage)
 * - Breaking mature Time Wheat crops (stage 7)
 * - Found naturally in ChronoDawn dimension (plains/forest biomes)
 *
 * Note:
 * - This is a plantable item (ItemNameBlockItem)
 * - Places TimeWheatBlock when used on farmland
 *
 * Reference: spec.md (User Story 1 Enhancement, FR-035)
 * Task: T223 [US1] Create Time Wheat Seeds item
 */
public class TimeWheatSeedsItem extends ItemNameBlockItem {
    public TimeWheatSeedsItem(Properties properties) {
        super(ModBlocks.TIME_WHEAT.get(), properties);
    }

    /**
     * Create default properties for Time Wheat Seeds.
     *
     * @return Item properties
     */
    public static Properties createProperties() {
        return new Properties();
    }
}
