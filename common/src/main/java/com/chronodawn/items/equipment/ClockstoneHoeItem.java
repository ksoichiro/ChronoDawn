package com.chronodawn.items.equipment;

import net.minecraft.world.item.HoeItem;

/**
 * Clockstone Hoe - Tier 1 time-themed farming tool.
 *
 * Basic tier tool crafted from Clockstone and Time Crystal.
 * Provides better farming performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Mining Speed: 6.5f (ClockstoneTier)
 * - Attack Damage: -2.0 (base) + 2.5 (tier bonus) = 0.5
 * - Attack Speed: -1.0 (fast hoe speed)
 * - Durability: 450 uses (ClockstoneTier)
 * - Enchantability: 14
 *
 * Crafting Recipe:
 * - Clockstone x2
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: tasks.md (T214)
 */
public class ClockstoneHoeItem extends HoeItem {
    public ClockstoneHoeItem(Properties properties) {
        super(ClockstoneTier.INSTANCE, properties.attributes(HoeItem.createAttributes(ClockstoneTier.INSTANCE, -2.0f, -1.0f)));
    }

    /**
     * Create default properties for Clockstone Hoe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(450);
    }
}
