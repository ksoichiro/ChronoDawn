package com.chronosphere.items;

import net.minecraft.world.item.Item;

/**
 * Portal Stabilizer - Portal utility item.
 *
 * Used to stabilize deactivated Chronosphere portals, enabling bidirectional travel.
 * Single-use item that is consumed upon successful portal stabilization.
 *
 * Properties:
 * - Max Stack Size: 1
 * - Durability: 1 (consumed after use)
 *
 * Crafting:
 * - Recipe blueprint found in Forgotten Library (Chronosphere dimension)
 * - Requires Clockstone and other materials (recipe defined in data/chronosphere/recipes/)
 *
 * Usage:
 * 1. Find blueprint in Forgotten Library
 * 2. Craft Portal Stabilizer
 * 3. Right-click deactivated portal with Portal Stabilizer
 * 4. Portal becomes stabilized, allowing bidirectional travel
 * 5. Portal Stabilizer is consumed
 *
 * Portal States:
 * - Activated: One-way travel to Chronosphere (created by Time Hourglass)
 * - Deactivated: Portal stops working after player entry
 * - Stabilized: Bidirectional travel enabled (created by Portal Stabilizer)
 *
 * Reference: data-model.md (Items → Tools & Utilities → Portal Stabilizer)
 * Task: T063 [US1] Create Portal Stabilizer item
 *
 * TODO: Implement portal stabilization logic when Portal System is ready (T045-T049)
 */
public class PortalStabilizerItem extends Item {
    public PortalStabilizerItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Portal Stabilizer.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1); // Single-use item
    }

    // TODO: Override useOn() method to implement portal stabilization logic
    // This will be implemented in Portal System phase (T045-T049)
}
