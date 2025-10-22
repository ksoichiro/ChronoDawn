package com.chronosphere.items;

import net.minecraft.world.item.Item;

/**
 * Time Hourglass - Portal ignition item.
 *
 * Used to activate Chronosphere portals by right-clicking on a valid portal frame.
 * Single-use item that is consumed upon successful portal activation.
 *
 * Properties:
 * - Max Stack Size: 1
 * - Durability: 1 (consumed after use)
 *
 * Crafting:
 * - Requires Clockstone and other materials (recipe defined in data/chronosphere/recipes/)
 *
 * Usage:
 * 1. Build valid portal frame (4x5 to 23x23 rectangle of Clockstone blocks)
 * 2. Right-click frame with Time Hourglass
 * 3. Portal activates and Time Hourglass is consumed
 *
 * Reference: data-model.md (Items → Tools & Utilities → Time Hourglass)
 * Task: T059 [US1] Create Time Hourglass item
 *
 * TODO: Implement portal activation logic when Portal System is ready (T045-T049)
 */
public class TimeHourglassItem extends Item {
    public TimeHourglassItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Hourglass.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1); // Single-use item
    }

    // TODO: Override useOn() method to implement portal activation logic
    // This will be implemented in Portal System phase (T045-T049)
}
