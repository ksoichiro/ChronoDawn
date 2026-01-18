package com.chronodawn.compat.v1_21_1.items.equipment;

import net.minecraft.world.item.AxeItem;

/**
 * Enhanced Clockstone Axe - Tier 2 time-themed woodcutting tool.
 *
 * Advanced tier tool crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-equivalent chopping performance with faster mining speed.
 *
 * Properties:
 * - Mining Speed: 7.5f (EnhancedClockstoneTier)
 * - Attack Damage: 6.0 (base) + 3.0 (tier bonus) = 9.0
 * - Attack Speed: -3.0 (slightly faster than standard axe)
 * - Durability: 1200 uses (EnhancedClockstoneTier)
 * - Enchantability: 16
 *
 * Crafting Recipe:
 * - Enhanced Clockstone x3
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: T251 - Create Enhanced Clockstone Axe (Tier 2 tool)
 */
public class EnhancedClockstoneAxeItem extends AxeItem {
    public EnhancedClockstoneAxeItem(Properties properties) {
        // 1.21.1: Uses createAttributes() for attribute building
        super(EnhancedClockstoneTier.INSTANCE, properties.attributes(AxeItem.createAttributes(EnhancedClockstoneTier.INSTANCE, 6.0f, -3.0f)));
    }

    /**
     * Create default properties for Enhanced Clockstone Axe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1200);
    }
}
