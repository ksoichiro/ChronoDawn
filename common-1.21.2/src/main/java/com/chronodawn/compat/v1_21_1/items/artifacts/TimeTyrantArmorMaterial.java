package com.chronodawn.compat.v1_21_1.items.artifacts;

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
import java.util.Map;
import java.util.function.Supplier;

public class TimeTyrantArmorMaterial {
    private static final EnumMap<ArmorItem.Type, Integer> DEFENSE_VALUES = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 3);
        map.put(ArmorItem.Type.LEGGINGS, 6);
        map.put(ArmorItem.Type.CHESTPLATE, 8);
        map.put(ArmorItem.Type.HELMET, 3);
        map.put(ArmorItem.Type.BODY, 8);
    });

    public static final Holder<ArmorMaterial> MATERIAL = Registry.registerForHolder(
        BuiltInRegistries.ARMOR_MATERIAL,
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "time_tyrant"),
        new ArmorMaterial(
            DEFENSE_VALUES,  // defense values
            18,  // enchantment value
            SoundEvents.ARMOR_EQUIP_NETHERITE,  // equip sound (now Holder<SoundEvent>)
            () -> Ingredient.of(ModItems.TIME_CRYSTAL.get()),  // repair ingredient supplier
            List.of(
                new ArmorMaterial.Layer(
                    CompatResourceLocation.create(ChronoDawn.MOD_ID, "time_tyrant")
                )
            ),  // layers
            2.5f,  // toughness
            0.1f  // knockback resistance
        )
    );
}
