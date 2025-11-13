package com.chronosphere.items.equipment;

import net.minecraft.world.item.ShovelItem;

/**
 * Clockstone Shovel - Tier 1 time-themed digging tool.
 *
 * Basic tier tool crafted from Clockstone and Time Crystal.
 * Provides better digging performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Mining Speed: 6.5f (ClockstoneTier)
 * - Attack Damage: 1.5 (base) + 2.5 (tier bonus) = 4.0
 * - Attack Speed: -3.0 (standard shovel speed)
 * - Durability: 450 uses (ClockstoneTier)
 * - Enchantability: 14
 *
 * Crafting Recipe:
 * - Clockstone x1
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: tasks.md (T214)
 */
public class ClockstoneShovelItem extends ShovelItem {
    public ClockstoneShovelItem(Properties properties) {
        super(ClockstoneTier.INSTANCE, properties.attributes(ShovelItem.createAttributes(ClockstoneTier.INSTANCE, 1.5f, -3.0f)));
    }

    /**
     * Create default properties for Clockstone Shovel item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(450);
    }
}
