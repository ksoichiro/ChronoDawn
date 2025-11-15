package com.chronosphere.items.equipment;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Enhanced Clockstone Armor Material - Tier 2 armor material for advanced Chronosphere equipment.
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
 * - Complete immunity to Chronosphere time distortion effects (Slowness IV/V)
 * - Implemented in TimeDistortionEffect.java
 *
 * Repair Material: Enhanced Clockstone
 *
 * Reference: T252 - Create Enhanced Clockstone Armor Set with immunity to time distortion
 */
public class EnhancedClockstoneArmorMaterial {
    public static final Holder<ArmorMaterial> ENHANCED_CLOCKSTONE = register(
        "enhanced_clockstone",
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 3);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 7);
            map.put(ArmorItem.Type.HELMET, 3);
            map.put(ArmorItem.Type.BODY, 7); // For horses/llamas
        }),
        16, // Enchantability (better than iron/clockstone and diamond)
        SoundEvents.ARMOR_EQUIP_DIAMOND,
        () -> Ingredient.of(ModItems.ENHANCED_CLOCKSTONE.get()),
        List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "enhanced_clockstone")
            )
        ),
        2.0f, // Toughness (same as diamond)
        0.0f  // Knockback Resistance
    );

    private static Holder<ArmorMaterial> register(
        String name,
        EnumMap<ArmorItem.Type, Integer> defense,
        int enchantmentValue,
        Holder<SoundEvent> equipSound,
        Supplier<Ingredient> repairIngredient,
        List<ArmorMaterial.Layer> layers,
        float toughness,
        float knockbackResistance
    ) {
        EnumMap<ArmorItem.Type, Integer> defenseMap = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defenseMap.put(type, defense.get(type));
        }

        return Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, name),
            new ArmorMaterial(
                defenseMap,
                enchantmentValue,
                equipSound,
                repairIngredient,
                layers,
                toughness,
                knockbackResistance
            )
        );
    }
}
