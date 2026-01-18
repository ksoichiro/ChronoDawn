package com.chronodawn.items.boss;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Guardian Stone - Boss Material Item
 *
 * A fragment of an ancient guardian's essence. Radiates temporal stability.
 *
 * Drop Source:
 * - Chronos Warden (1x, 100% chance)
 *
 * Usage:
 * - Crafting material for Chrono Aegis (Time Tyrant preparation item)
 *
 * Properties:
 * - Rarity: RARE (Aqua color)
 * - Fireproof: Yes
 * - Max Stack: 16
 *
 * Task: T234d [Phase 1] Create Guardian Stone item
 */
public class GuardianStoneItem extends Item {
    /**
     * Create a new Guardian Stone item instance.
     *
     * @param properties Item properties (rarity, stack size, etc.)
     */
    public GuardianStoneItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Guardian Stone.
     *
     * @return Item properties with RARE rarity, fireproof, stack size 16
     */
    public static Properties createProperties() {
        return new Properties()
            .rarity(Rarity.RARE)
            .fireResistant()
            .stacksTo(16);
    }
}
