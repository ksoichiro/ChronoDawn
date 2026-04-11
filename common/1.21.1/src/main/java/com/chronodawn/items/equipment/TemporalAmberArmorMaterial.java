package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
 * Repair Material: Temporal Amber Dust
 *
 * Reference: T301 - Temporal Amber Armor Material
 */
public class TemporalAmberArmorMaterial {
    // Base durability multiplier (28 * 1.25 = 35)
    public static final int BASE_DURABILITY = 35;

    public static final Holder<ArmorMaterial> TEMPORAL_AMBER = register(
        "temporal_amber",
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 3);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 7);
            map.put(ArmorItem.Type.HELMET, 3);
            map.put(ArmorItem.Type.BODY, 7); // For horses/llamas
        }),
        10, // Enchantability
        SoundEvents.ARMOR_EQUIP_DIAMOND,
        () -> Ingredient.of(ModItems.TEMPORAL_AMBER_DUST.get()),
        1.5f, // Toughness
        0.0f  // Knockback Resistance
    );

    private static Holder<ArmorMaterial> register(
        String name,
        EnumMap<ArmorItem.Type, Integer> defense,
        int enchantmentValue,
        Holder<SoundEvent> equipSound,
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
