package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;

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
 *
 * Note: In 1.21.5, ArmorItem has been removed. Items now use data components
 * and Item.Properties#humanoidArmor() instead of inheritance.
 */
public class TimeTyrantMailItem extends Item {
    public TimeTyrantMailItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_tyrant_mail")))
                // Use humanoidArmor() to set up armor components
                .humanoidArmor(TimeTyrantArmorMaterial.MATERIAL.value(), ArmorType.CHESTPLATE);
    }
}
