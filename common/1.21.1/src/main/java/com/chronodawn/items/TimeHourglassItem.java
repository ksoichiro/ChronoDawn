package com.chronodawn.items;

import net.minecraft.world.item.Item;

/**
 * Time Hourglass - Consumable portal ignition item.
 *
 * Right-clicking a valid Chrono Dawn portal frame with this item (or any other
 * item in the {@code chronodawn:portal_igniters} tag) ignites the portal; see
 * {@link PortalIgnitionHandler} and {@link com.chronodawn.events.BlockEventHandler}
 * for the actual ignition logic, which is tag-driven rather than tied to this class.
 *
 * Properties:
 * - Max Stack Size: 64 (consumable item)
 * - Consumed on use: 1 item per portal ignition (except in creative mode)
 *
 * Reference: docs/portal_implementation_plan.md (Phase 2)
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
}
