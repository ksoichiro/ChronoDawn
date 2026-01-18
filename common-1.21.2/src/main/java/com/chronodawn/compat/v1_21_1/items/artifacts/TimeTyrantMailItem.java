package com.chronodawn.compat.v1_21_1.items.artifacts;

import net.minecraft.world.item.ArmorItem;

/**
 * Time Tyrant's Mail - Ultimate Chestplate with Rollback Effect
 *
 * Properties:
 * - Defense: 8 (same as netherite chestplate)
 * - Durability: 600
 * - Rarity: Epic
 *
 * Special Ability - Temporal Rollback:
 * - 20% chance to rollback to previous state when receiving lethal damage
 * - Restores HP and position from 3 seconds ago
 * - 60 second cooldown
 *
 * Reference: T153-157
 */
public class TimeTyrantMailItem extends ArmorItem {
    public TimeTyrantMailItem(Properties properties) {
        super(TimeTyrantArmorMaterial.MATERIAL, Type.CHESTPLATE, properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(600)
                .rarity(net.minecraft.world.item.Rarity.EPIC);
    }
}
