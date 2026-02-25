package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ArmorItem;
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
 */
public class EchoingTimeBootsItem extends ArmorItem {
    public EchoingTimeBootsItem(Properties properties) {
        // In 1.21.2, ArmorItem constructor expects ArmorMaterial, not Holder<ArmorMaterial>
        super(TimeTyrantArmorMaterial.MATERIAL.value(), ArmorType.BOOTS, properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(500)
                .rarity(net.minecraft.world.item.Rarity.EPIC)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "echoing_time_boots")));
    }
}
