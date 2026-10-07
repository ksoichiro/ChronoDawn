package com.chronodawn.items.equipment;

import net.minecraft.world.item.SwordItem;

/**
 * Clockstone Sword - Tier 1 time-themed weapon.
 *
 * Basic tier weapon crafted from Clockstone.
 * Provides better performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Attack Damage: 6.5 (1.0 player base + 3.0 + tier bonus 2.5, slightly above iron's 6.0)
 * - Attack Speed: -2.4 (standard sword speed)
 * - Durability: 450 uses (ClockstoneTier)
 * - Enchantability: 14
 *
 * Crafting Recipe:
 * - Clockstone x2
 * - Stick x1
 *
 * Reference: tasks.md (T213)
 */
public class ClockstoneSwordItem extends SwordItem {
    public ClockstoneSwordItem(Properties properties) {
        // 1.21.1: Uses createAttributes() for attribute building
        super(ClockstoneTier.INSTANCE, properties.attributes(SwordItem.createAttributes(ClockstoneTier.INSTANCE, 3, -2.4f)));
    }

    /**
     * Create default properties for Clockstone Sword item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(450);
    }
}
