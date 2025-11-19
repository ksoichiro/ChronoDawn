package com.chronosphere.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Phantom Essence - Boss drop material from Temporal Phantom
 *
 * A spectral essence dropped by Temporal Phantom.
 * Used for crafting Chrono Aegis (ultimate shield).
 *
 * Properties:
 * - Rarity: RARE (Aqua color)
 * - Fire resistant
 * - Stack size: 16
 *
 * Crafting Recipe (Chrono Aegis):
 * Guardian Stone (G) + Phantom Essence (P) + Colossus Gear (C) + Entropy Core (E)
 * Pattern:
 *   G P
 *   C E
 *
 * Reference: research.md (Boss 3: Temporal Phantom)
 * Task: T236b [Phase 2] Create PhantomEssenceItem
 */
public class PhantomEssenceItem extends Item {
    public PhantomEssenceItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Properties()
            .rarity(Rarity.RARE)
            .fireResistant()
            .stacksTo(16);
    }
}
