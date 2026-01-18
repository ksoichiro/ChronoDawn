package com.chronodawn.items.base;

import net.minecraft.world.item.Item;

/**
 * Clockstone item - Base material for time-related items.
 *
 * Obtained by mining Clockstone Ore in the Overworld (Ancient Ruins) or ChronoDawn dimension.
 * Used as crafting material for:
 * - Time Hourglass (portal ignition item)
 * - Portal frame blocks
 * - Basic time-themed items
 *
 * Properties:
 * - Max Stack Size: 64
 *
 * Reference: data-model.md (Items → Base Materials → Clockstone)
 */
public class ClockstoneItem extends Item {
    public ClockstoneItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(64);
    }
}
