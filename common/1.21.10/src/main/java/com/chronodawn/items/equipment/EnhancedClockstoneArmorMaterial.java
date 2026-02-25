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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Enhanced Clockstone Armor Material - Tier 2 armor material for advanced ChronoDawn equipment.
 *
 * Tier 2 armor material, comparable to diamond with superior enchantability.
 * Crafted from Enhanced Clockstone + Time Crystal for maximum protection.
 *
 * Defense Values:
 * - Helmet: 3 (iron: 2, diamond: 3, clockstone: 2)
 * - Chestplate: 7 (iron: 6, diamond: 8, clockstone: 6)
 * - Leggings: 6 (iron: 5, diamond: 6, clockstone: 5)
 * - Boots: 3 (iron: 2, diamond: 3, clockstone: 2)
 * - Total: 19 (iron: 15, diamond: 20, clockstone: 15)
 *
 * Other Properties:
 * - Durability Multiplier: 28 (iron: 15, diamond: 33, clockstone: 20)
 * - Enchantability: 16 (better than iron/clockstone: 14, diamond: 10)
 * - Toughness: 2.0f (iron: 0.0f, diamond: 2.0f, clockstone: 1.0f)
 * - Knockback Resistance: 0.0f
 *
 * Special Ability (Full Set Bonus):
 * - Complete immunity to ChronoDawn time distortion effects (Slowness IV/V)
 * - Implemented in TimeDistortionEffect.java
 *
 * Repair Material: Time Crystal
 *
 * Reference: T252 - Create Enhanced Clockstone Armor Set with immunity to time distortion
 */
public class EnhancedClockstoneArmorMaterial {
    // Base durability multiplier
    public static final int BASE_DURABILITY = 28;

    // Time Crystal repair tag
    // TEMPORARY: Using vanilla tag to test if tag mechanism works
    // TODO: Find proper solution for custom mod tags in NeoForge 1.21.2
    private static final TagKey<Item> TIME_CRYSTAL_TAG = ItemTags.REPAIRS_DIAMOND_ARMOR;

    public static final Holder<ArmorMaterial> ENHANCED_CLOCKSTONE = Holder.direct(
        new ArmorMaterial(
            BASE_DURABILITY,  // durability multiplier
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 3);
                map.put(ArmorType.LEGGINGS, 6);
                map.put(ArmorType.CHESTPLATE, 7);
                map.put(ArmorType.HELMET, 3);
                map.put(ArmorType.BODY, 7); // For horses/llamas
            }),
            16, // Enchantability (better than iron/clockstone and diamond)
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0f, // Toughness (same as diamond)
            0.0f,  // Knockback Resistance
            TIME_CRYSTAL_TAG, // Repair ingredient as TagKey
            CompatResourceLocation.createEquipmentAssetKey(ChronoDawn.MOD_ID, "enhanced_clockstone")  // Equipment asset key (1.21.4+)
        )
    );
}
