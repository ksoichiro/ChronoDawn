package com.chronosphere.items;

import net.minecraft.world.item.Item;

/**
 * Time Hourglass - Consumable portal ignition item.
 *
 * Used to activate Chronosphere portals by right-clicking on a valid portal frame.
 * Ignition is handled by Custom Portal API. Item consumption and restrictions
 * are enforced by BlockEventHandler.
 *
 * Properties:
 * - Max Stack Size: 64 (consumable item)
 * - Consumed on use: 1 item per portal ignition (except in creative mode)
 *
 * Crafting:
 * - Requires Clockstone and other materials (recipe defined in data/chronosphere/recipes/)
 * - Recipe is relatively easy to craft, making it a consumable resource
 *
 * Usage:
 * 1. Build valid portal frame (4x5 to 23x23 rectangle of Clockstone blocks)
 * 2. Right-click frame with Time Hourglass
 * 3. Portal activates (INACTIVE → ACTIVATED) - handled by Custom Portal API
 * 4. Time Hourglass is consumed (stack shrinks by 1) - handled by BlockEventHandler
 *
 * Portal State Logic (enforced by BlockEventHandler):
 * - INACTIVE: Can be ignited with Time Hourglass → ACTIVATED (consumes item)
 * - DEACTIVATED (in Chronosphere only): Cannot be ignited (does NOT consume item)
 * - STABILIZED: Already active, no action needed
 *
 * Item Consumption:
 * - Consumed ONLY when portal ignition is successful (handled by Custom Portal API's registerIgniteEvent)
 * - NOT consumed if ignition fails (invalid frame, insufficient space, etc.)
 * - NOT consumed if portals are unstable (DEACTIVATED state in Chronosphere - blocked by BlockEventHandler)
 * - NOT consumed in creative mode
 * - Consumption triggered by Custom Portal API's ignition event (CustomPortalFabric.registerIgniteEvent)
 *
 * Reference: data-model.md (Items → Tools & Utilities → Time Hourglass)
 * Tasks: T059 [US1] Create Time Hourglass item, T062a [US1] Make Time Hourglass consumable
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
                .stacksTo(64); // Consumable item - can stack for convenience
    }

    // Portal ignition is handled by Custom Portal API
    // Item consumption is handled by CustomPortalFabric.registerIgniteEvent() (shrinks stack by 1 on successful ignition)
    // Restrictions on DEACTIVATED portals are enforced by BlockEventHandler (prevents ignition before Custom Portal API processes)
}
