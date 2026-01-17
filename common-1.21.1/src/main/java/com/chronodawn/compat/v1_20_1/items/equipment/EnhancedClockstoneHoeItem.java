package com.chronodawn.items.equipment;

import net.minecraft.world.item.HoeItem;

/**
 * Enhanced Clockstone Hoe - Tier 2 time-themed farming tool.
 *
 * Advanced tier tool crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-equivalent farming performance with faster tilling speed.
 *
 * Properties:
 * - Mining Speed: 7.5f (EnhancedClockstoneTier)
 * - Attack Damage: 0 (base) + 3.0 (tier bonus) = 3.0
 * - Attack Speed: 0.0 (fastest attack speed)
 * - Durability: 1200 uses (EnhancedClockstoneTier)
 * - Enchantability: 16
 *
 * Crafting Recipe:
 * - Enhanced Clockstone x2
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: T251 - Create Enhanced Clockstone Hoe (Tier 2 tool)
 */
public class EnhancedClockstoneHoeItem extends HoeItem {
    public EnhancedClockstoneHoeItem(Properties properties) {
        super(EnhancedClockstoneTier.INSTANCE, 0, 0.0f, properties);
    }

    /**
     * Create default properties for Enhanced Clockstone Hoe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1200);
    }
}
