package com.chronosphere.items;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

/**
 * Decorative Water Bucket Item
 *
 * Places chronosphere:decorative_water instead of minecraft:water.
 * Used for creating decorative water features in structure NBT files.
 *
 * Creative mode only - not obtainable in survival.
 */
public class DecorativeWaterBucketItem extends BucketItem {
    public DecorativeWaterBucketItem(Fluid fluid, Properties properties) {
        // Fluid is passed from ModItems registration
        super(fluid, properties);
    }
}
