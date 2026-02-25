package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ArmorItem;
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
 */
public class TimeTyrantMailItem extends ArmorItem {
    public TimeTyrantMailItem(Properties properties) {
        // In 1.21.2, ArmorItem constructor expects ArmorMaterial, not Holder<ArmorMaterial>
        super(TimeTyrantArmorMaterial.MATERIAL.value(), ArmorType.CHESTPLATE, properties);
    }

    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(600)
                .rarity(net.minecraft.world.item.Rarity.EPIC)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_tyrant_mail")));
    }
}
