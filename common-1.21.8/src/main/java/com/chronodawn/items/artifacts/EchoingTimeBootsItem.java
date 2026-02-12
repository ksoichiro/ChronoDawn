package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;

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
 *
 * Note: In 1.21.5, ArmorItem has been removed. Items now use data components
 * and Item.Properties#humanoidArmor() instead of inheritance.
 */
public class EchoingTimeBootsItem extends Item {
    public EchoingTimeBootsItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "echoing_time_boots")))
                // Use humanoidArmor() to set up armor components
                .humanoidArmor(TimeTyrantArmorMaterial.MATERIAL.value(), ArmorType.BOOTS);
    }
}
