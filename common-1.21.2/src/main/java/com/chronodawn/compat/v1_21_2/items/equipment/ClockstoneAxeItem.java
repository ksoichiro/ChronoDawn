package com.chronodawn.compat.v1_21_2.items.equipment;

import net.minecraft.world.item.AxeItem;

/**
 * Clockstone Axe - Tier 1 time-themed woodcutting tool.
 *
 * Basic tier tool crafted from Clockstone and Time Crystal.
 * Provides better chopping performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Mining Speed: 6.5f (ClockstoneTier)
 * - Attack Damage: 6.0 (base) + 2.5 (tier bonus) = 8.5
 * - Attack Speed: -3.1 (standard axe speed)
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
public class ClockstoneAxeItem extends AxeItem {
    public ClockstoneAxeItem(Properties properties) {
        // 1.21.1: Uses createAttributes() for attribute building
        super(ClockstoneTier.INSTANCE, properties.attributes(AxeItem.createAttributes(ClockstoneTier.INSTANCE, 6.0f, -3.1f)));
    }

    /**
     * Create default properties for Clockstone Axe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(450);
    }
}
