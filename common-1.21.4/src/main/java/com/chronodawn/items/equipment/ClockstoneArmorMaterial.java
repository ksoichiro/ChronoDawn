package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Clockstone Armor Material - Tier 1 armor material for basic ChronoDawn equipment.
 *
 * Tier 1 armor material, slightly better than iron but below diamond.
 * Crafted from Clockstone + Time Crystal for enhanced protection.
 *
 * Defense Values:
 * - Helmet: 2 (iron: 2, diamond: 3)
 * - Chestplate: 6 (iron: 6, diamond: 8)
 * - Leggings: 5 (iron: 5, diamond: 6)
 * - Boots: 2 (iron: 2, diamond: 3)
 * - Total: 15 (iron: 15, diamond: 20)
 *
 * Other Properties:
 * - Durability Multiplier: 20 (iron: 15, diamond: 33)
 * - Enchantability: 14 (same as iron)
 * - Toughness: 1.0f (iron: 0.0f, diamond: 2.0f)
 * - Knockback Resistance: 0.0f
 *
 * Repair Material: Time Crystal
 *
 * Reference: tasks.md (T215)
 */
public class ClockstoneArmorMaterial {
    // Base durability multiplier
    public static final int BASE_DURABILITY = 20;

    // Time Crystal repair tag
    // TEMPORARY: Using vanilla tag to test if tag mechanism works
    // TODO: Find proper solution for custom mod tags in NeoForge 1.21.2
    private static final TagKey<Item> TIME_CRYSTAL_TAG = ItemTags.REPAIRS_IRON_ARMOR;

    public static final Holder<ArmorMaterial> CLOCKSTONE = Holder.direct(
        new ArmorMaterial(
            BASE_DURABILITY,  // durability multiplier
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 2);
                map.put(ArmorType.LEGGINGS, 5);
                map.put(ArmorType.CHESTPLATE, 6);
                map.put(ArmorType.HELMET, 2);
                map.put(ArmorType.BODY, 6); // For horses/llamas
            }),
            14, // Enchantability (same as iron)
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0f, // Toughness (between iron 0.0f and diamond 2.0f)
            0.0f,  // Knockback Resistance
            TIME_CRYSTAL_TAG, // Repair ingredient as TagKey
            CompatResourceLocation.createEquipmentAssetKey(ChronoDawn.MOD_ID, "clockstone")  // Equipment asset key (1.21.4+)
        )
    );

}
