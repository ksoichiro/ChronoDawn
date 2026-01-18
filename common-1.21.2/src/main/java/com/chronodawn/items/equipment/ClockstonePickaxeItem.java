package com.chronodawn.items.equipment;

import net.minecraft.world.item.PickaxeItem;

/**
 * Clockstone Pickaxe - Tier 1 time-themed mining tool.
 *
 * Basic tier tool crafted from Clockstone and Time Crystal.
 * Provides better mining performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Mining Speed: 6.5f (ClockstoneTier)
 * - Attack Damage: 1.0 (base) + 2.5 (tier bonus) = 3.5
 * - Attack Speed: -2.8 (standard pickaxe speed)
 * - Durability: 450 uses (ClockstoneTier)
 * - Enchantability: 14
 *
 * Crafting Recipe:
 * - Clockstone x3
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: tasks.md (T214)
 */
public class ClockstonePickaxeItem extends PickaxeItem {
    public ClockstonePickaxeItem(Properties properties) {
        // 1.21.1: Uses createAttributes() for attribute building
        super(ClockstoneTier.INSTANCE, properties.attributes(PickaxeItem.createAttributes(ClockstoneTier.INSTANCE, 1.0f, -2.8f)));
    }

    /**
     * Create default properties for Clockstone Pickaxe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(450);
    }
}
