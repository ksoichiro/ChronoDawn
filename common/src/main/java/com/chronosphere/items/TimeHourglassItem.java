package com.chronosphere.items;

import net.minecraft.world.item.Item;

/**
 * Time Hourglass - Portal ignition item.
 *
 * Used to activate Chronosphere portals by right-clicking on a valid portal frame.
 * Ignition is handled by Custom Portal API. Restrictions on DEACTIVATED portals
 * are enforced by BlockEventHandler.
 *
 * Properties:
 * - Max Stack Size: 1
 * - Durability: 1 (consumed after use by Custom Portal API)
 *
 * Crafting:
 * - Requires Clockstone and other materials (recipe defined in data/chronosphere/recipes/)
 *
 * Usage:
 * 1. Build valid portal frame (4x5 to 23x23 rectangle of Clockstone blocks)
 * 2. Right-click frame with Time Hourglass
 * 3. Portal activates (INACTIVE → ACTIVATED) - handled by Custom Portal API
 * 4. Time Hourglass is consumed by Custom Portal API
 *
 * Portal State Logic (enforced by BlockEventHandler):
 * - INACTIVE: Can be ignited with Time Hourglass → ACTIVATED
 * - DEACTIVATED (in Chronosphere only): Cannot be ignited with Time Hourglass
 * - STABILIZED: Already active, no action needed
 *
 * Reference: data-model.md (Items → Tools & Utilities → Time Hourglass)
 * Task: T059 [US1] Create Time Hourglass item
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
                .durability(1); // Consumed by Custom Portal API after use
    }

    // Portal ignition is handled by Custom Portal API
    // Restrictions on DEACTIVATED portals are enforced by BlockEventHandler
}
