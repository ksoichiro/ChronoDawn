package com.chronodawn.compat.v1_21_2.items.equipment;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
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
    public static final Holder<ArmorMaterial> ENHANCED_CLOCKSTONE = register(
        "enhanced_clockstone",
        Util.make(new EnumMap<>(ArmorType.class), map -> {
            map.put(ArmorType.BOOTS, 3);
            map.put(ArmorType.LEGGINGS, 6);
            map.put(ArmorType.CHESTPLATE, 7);
            map.put(ArmorType.HELMET, 3);
            map.put(ArmorType.BODY, 7); // For horses/llamas
        }),
        16, // Enchantability (better than iron/clockstone and diamond)
        SoundEvents.ARMOR_EQUIP_DIAMOND,
        () -> Ingredient.of(ModItems.TIME_CRYSTAL.get()),
        2.0f, // Toughness (same as diamond)
        0.0f  // Knockback Resistance
    );

    private static Holder<ArmorMaterial> register(
        String name,
        EnumMap<ArmorType, Integer> defense,
        int enchantmentValue,
        Holder<SoundEvent> equipSound,
        Supplier<Ingredient> repairIngredient,
        float toughness,
        float knockbackResistance
    ) {
        EnumMap<ArmorType, Integer> defenseMap = new EnumMap<>(ArmorType.class);
        for (ArmorType type : ArmorType.values()) {
            defenseMap.put(type, defense.get(type));
        }

        return Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            CompatResourceLocation.create(ChronoDawn.MOD_ID, name),
            new ArmorMaterial(
                defenseMap,  // defense values
                enchantmentValue,  // enchantment value
                equipSound,  // equip sound (Holder<SoundEvent>)
                repairIngredient,  // repair ingredient supplier
                List.of(
                    new ArmorMaterial.Layer(
                        CompatResourceLocation.create(ChronoDawn.MOD_ID, name)
                    )
                ),  // layers
                toughness,  // toughness
                knockbackResistance  // knockback resistance
            )
        );
    }
}
