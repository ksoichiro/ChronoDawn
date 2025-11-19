package com.chronosphere.items.boss;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Colossus Gear - Boss drop from Clockwork Colossus
 *
 * A massive gear from an ancient mechanical colossus.
 * Still warm with residual temporal energy.
 *
 * Used to craft Chrono Aegis (along with Guardian Stone, Phantom Essence, Entropy Core).
 *
 * Properties:
 * - Rarity: RARE (yellow text)
 * - Stack size: 16
 * - Fireproof: Yes
 *
 * Reference: research.md (Boss 2: Clockwork Colossus - Items)
 * Task: T235e [P] Create ColossusGearItem
 */
public class ColossusGearItem extends Item {
    public ColossusGearItem(Properties properties) {
        super(properties);
    }

    /**
     * Create item properties for Colossus Gear.
     *
     * @return Item properties with correct settings
     */
    public static Properties createProperties() {
        return new Properties()
            .rarity(Rarity.RARE)
            .fireResistant()
            .stacksTo(16);
    }
}
