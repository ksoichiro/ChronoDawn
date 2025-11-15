package com.chronosphere.items.artifacts;

import com.chronosphere.Chronosphere;
import com.chronosphere.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public class TimeTyrantArmorMaterial {
    public static final Holder<ArmorMaterial> MATERIAL = Registry.registerForHolder(
        BuiltInRegistries.ARMOR_MATERIAL,
        ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "time_tyrant"),
        new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.BODY, 8);
            }),
            18,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(ModItems.FRAGMENT_OF_STASIS_CORE.get()),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "time_tyrant"))),
            2.5f,
            0.1f
        )
    );
}
