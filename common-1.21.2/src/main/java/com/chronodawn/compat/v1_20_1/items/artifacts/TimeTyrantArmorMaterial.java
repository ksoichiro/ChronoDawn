package com.chronodawn.compat.v1_20_1.items.artifacts;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public class TimeTyrantArmorMaterial {
    // 1.20.1: ArmorItem.Type.BODY does not exist
    private static final EnumMap<ArmorItem.Type, Integer> DEFENSE_VALUES = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 3);
        map.put(ArmorItem.Type.LEGGINGS, 6);
        map.put(ArmorItem.Type.CHESTPLATE, 8);
        map.put(ArmorItem.Type.HELMET, 3);
    });

    // 1.20.1: BuiltInRegistries.ARMOR_MATERIAL does not exist, use Holder.direct() instead
    public static final Holder<ArmorMaterial> MATERIAL = Holder.direct(new ArmorMaterial() {
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return DEFENSE_VALUES.get(type);
            }

            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return DEFENSE_VALUES.get(type);
            }

            @Override
            public int getEnchantmentValue() {
                return 18;
            }

            @Override
            public net.minecraft.sounds.SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_NETHERITE;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(ModItems.TIME_CRYSTAL.get());
            }

            @Override
            public String getName() {
                return ChronoDawn.MOD_ID + ":time_tyrant";
            }

            @Override
            public float getToughness() {
                return 2.5f;
            }

            @Override
            public float getKnockbackResistance() {
                return 0.1f;
            }
        });
}
