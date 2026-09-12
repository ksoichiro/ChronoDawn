package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import com.chronodawn.compat.CompatResourceLocation;

import java.util.EnumMap;

/**
 * Temporal Amber Armor Material - Tier 2 armor material from Temporal Amber.
 *
 * Tier 2 armor material, comparable to diamond with enhanced durability.
 * Crafted from Temporal Amber for superior protection.
 *
 * Defense Values:
 * - Helmet: 3 (iron: 2, diamond: 3)
 * - Chestplate: 7 (iron: 6, diamond: 8)
 * - Leggings: 6 (iron: 5, diamond: 6)
 * - Boots: 3 (iron: 2, diamond: 3)
 * - Total: 19 (iron: 15, diamond: 20)
 *
 * Other Properties:
 * - Durability Multiplier: 35 (iron: 15, diamond: 33, enhanced clockstone: 28)
 * - Enchantability: 10 (iron: 9, diamond: 10)
 * - Toughness: 1.5f (iron: 0.0f, diamond: 2.0f)
 * - Knockback Resistance: 0.0f
 *
 * Repair Material: Temporal Amber Dust (uses REPAIRS_DIAMOND_ARMOR; see the
 * field comment below for why this stays on a vanilla tag on 1.21.2)
 *
 * Reference: T301 - Temporal Amber Armor Material
 */
public class TemporalAmberArmorMaterial {
    // Base durability multiplier (28 * 1.25 = 35)
    public static final int BASE_DURABILITY = 35;

    // Temporal Amber Dust repair tag
    // NeoForge 1.21.2/1.21.3 crashes at startup ("Unbound tags in registry ...
    // c:gems/...", "c:dusts/...") when a custom-namespace TagKey is used as
    // repairItems here. No fixed NeoForge release exists for 1.21.2 (upstream fix
    // neoforged/NeoForge#1651 landed at 21.3.7-beta, MC 1.21.3 only); kept on this
    // vanilla tag for both loaders on 1.21.2 until that changes.
    private static final TagKey<Item> TEMPORAL_AMBER_DUST_TAG = ItemTags.REPAIRS_DIAMOND_ARMOR;

    public static final Holder<ArmorMaterial> TEMPORAL_AMBER = Holder.direct(
        new ArmorMaterial(
            BASE_DURABILITY,  // durability multiplier
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 3);
                map.put(ArmorType.LEGGINGS, 6);
                map.put(ArmorType.CHESTPLATE, 7);
                map.put(ArmorType.HELMET, 3);
                map.put(ArmorType.BODY, 7); // For horses/llamas
            }),
            10, // Enchantability
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            1.5f, // Toughness
            0.0f,  // Knockback Resistance
            TEMPORAL_AMBER_DUST_TAG, // Repair ingredient as TagKey
            CompatResourceLocation.create(ChronoDawn.MOD_ID, "temporal_amber")  // Equipment asset location
        )
    );
}
