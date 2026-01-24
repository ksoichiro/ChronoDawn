package com.chronodawn.items.equipment;

import net.minecraft.world.item.SwordItem;

/**
 * Clockstone Sword - Tier 1 time-themed weapon.
 *
 * Basic tier weapon crafted from Clockstone and Time Crystal.
 * Provides better performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Attack Damage: 6.0 (base 3.0 + tier bonus 2.5, same as iron sword damage)
 * - Attack Speed: -2.4 (standard sword speed)
 * - Durability: 450 uses (ClockstoneTier)
 * - Enchantability: 14
 *
 * Crafting Recipe:
 * - Clockstone x2
 * - Time Crystal x1
 * - Stick x1
 *
 * Reference: tasks.md (T213)
 */
public class ClockstoneSwordItem extends SwordItem {
    public ClockstoneSwordItem(Properties properties) {
        super(ClockstoneTier.INSTANCE, 3, -2.4f, properties);
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
