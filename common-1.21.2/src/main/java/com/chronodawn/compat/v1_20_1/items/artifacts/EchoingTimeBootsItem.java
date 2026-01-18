package com.chronodawn.compat.v1_20_1.items.artifacts;

import net.minecraft.world.item.ArmorItem;

/**
 * Echoing Time Boots - Ultimate Boots with Decoy Summoning
 *
 * Properties:
 * - Defense: 3 (same as netherite boots)
 * - Durability: 500
 * - Rarity: Epic
 *
 * Special Ability - Temporal Echo:
 * - Summons decoy entity when sprinting
 * - Decoy attracts enemy attention for 10 seconds
 * - 15 second cooldown
 *
 * Reference: T158-164
 */
public class EchoingTimeBootsItem extends ArmorItem {
    public EchoingTimeBootsItem(Properties properties) {
        super(TimeTyrantArmorMaterial.MATERIAL.value(), Type.BOOTS, properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(500)
                .rarity(net.minecraft.world.item.Rarity.EPIC);
    }
}
