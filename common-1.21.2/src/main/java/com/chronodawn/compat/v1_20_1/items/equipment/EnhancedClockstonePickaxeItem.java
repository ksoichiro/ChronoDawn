package com.chronodawn.compat.v1_20_1.items.equipment;

import net.minecraft.world.item.PickaxeItem;

/**
 * Enhanced Clockstone Pickaxe - Tier 2 time-themed mining tool.
 *
 * Advanced tier tool crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-equivalent mining performance with faster mining speed.
 *
 * Properties:
 * - Mining Speed: 7.5f (EnhancedClockstoneTier)
 * - Attack Damage: 1.0 (base) + 3.0 (tier bonus) = 4.0
 * - Attack Speed: -2.8
 * - Durability: 1200 uses (EnhancedClockstoneTier)
 * - Enchantability: 16
 *
 * Crafting Recipe:
 * - Enhanced Clockstone x3
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: T251 - Create Enhanced Clockstone tools (Tier 2)
 */
public class EnhancedClockstonePickaxeItem extends PickaxeItem {
    public EnhancedClockstonePickaxeItem(Properties properties) {
        super(EnhancedClockstoneTier.INSTANCE, 1, -2.8f, properties);
    }

    /**
     * Create default properties for Enhanced Clockstone Pickaxe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1200);
    }
}
