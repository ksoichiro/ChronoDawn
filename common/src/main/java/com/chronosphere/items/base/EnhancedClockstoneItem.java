package com.chronosphere.items.base;

import net.minecraft.world.item.Item;

/**
 * Enhanced Clockstone item - Advanced material for time manipulation items.
 *
 * Obtained from Desert Clock Tower structure in the Chronosphere dimension.
 * Used as crafting material for:
 * - Time Clock (mob AI cancellation utility)
 * - Spatially Linked Pickaxe (drop doubling tool)
 * - Unstable Hourglass (reversed resonance trigger)
 *
 * Properties:
 * - Max Stack Size: 64
 *
 * Reference: data-model.md (Items → Base Materials → Enhanced Clockstone)
 */
public class EnhancedClockstoneItem extends Item {
    public EnhancedClockstoneItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Enhanced Clockstone item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(64);
    }
}
