package com.chronodawn.items.base;

import net.minecraft.world.item.Item;

/**
 * Fragment of Stasis Core - Boss Material Item
 *
 * Crafting Material:
 * - Dropped by Time Tyrant (3-5 fragments per kill, affected by Looting)
 * - Used for crafting ultimate artifacts (Chronoblade, Time Guardian's Mail, etc.)
 *
 * Properties:
 * - Stack Size: 64
 * - Rarity: Rare (yellow text)
 *
 * Lore:
 * A fragment of the Stasis Core that powered Time Tyrant. Contains residual temporal energy
 * and can be used to forge powerful time-manipulating artifacts.
 *
 * Reference: T141 - Create Fragment of Stasis Core item
 */
public class FragmentOfStasisCoreItem extends Item {
    public FragmentOfStasisCoreItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Fragment of Stasis Core item.
     *
     * @return Item properties with rare rarity
     */
    public static Properties createProperties() {
        return new Properties()
            .rarity(net.minecraft.world.item.Rarity.RARE);
    }
}
