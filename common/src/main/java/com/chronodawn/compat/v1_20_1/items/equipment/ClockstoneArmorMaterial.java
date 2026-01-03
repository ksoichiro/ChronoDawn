package com.chronodawn.items.equipment;

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
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
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
    public static final Holder<ArmorMaterial> CLOCKSTONE = register(
        "clockstone",
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 2);
            map.put(ArmorItem.Type.LEGGINGS, 5);
            map.put(ArmorItem.Type.CHESTPLATE, 6);
            map.put(ArmorItem.Type.HELMET, 2);
            map.put(ArmorItem.Type.BODY, 6); // For horses/llamas
        }),
        14, // Enchantability (same as iron)
        SoundEvents.ARMOR_EQUIP_IRON,
        () -> Ingredient.of(ModItems.TIME_CRYSTAL.get()),
        1.0f, // Toughness (between iron 0.0f and diamond 2.0f)
        0.0f  // Knockback Resistance
    );

    private static Holder<ArmorMaterial> register(
        String name,
        EnumMap<ArmorItem.Type, Integer> defense,
        int enchantmentValue,
        SoundEvent equipSound,
        Supplier<Ingredient> repairIngredient,
        float toughness,
        float knockbackResistance
    ) {
        EnumMap<ArmorItem.Type, Integer> defenseMap = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            defenseMap.put(type, defense.get(type));
        }

        return Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            CompatResourceLocation.create(ChronoDawn.MOD_ID, name),
            new ArmorMaterial() {
                @Override
                public int getDurabilityForType(ArmorItem.Type type) {
                    return defenseMap.getOrDefault(type, 0);
                }

                @Override
                public int getDefenseForType(ArmorItem.Type type) {
                    return defenseMap.getOrDefault(type, 0);
                }

                @Override
                public int getEnchantmentValue() {
                    return enchantmentValue;
                }

                @Override
                public SoundEvent getEquipSound() {
                    return equipSound;
                }

                @Override
                public Ingredient getRepairIngredient() {
                    return repairIngredient.get();
                }

                @Override
                public String getName() {
                    return ChronoDawn.MOD_ID + ":" + name;
                }

                @Override
                public float getToughness() {
                    return toughness;
                }

                @Override
                public float getKnockbackResistance() {
                    return knockbackResistance;
                }
            }
        );
    }
}
