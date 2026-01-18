package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.EnumMap;

public class TimeTyrantArmorMaterial {
    // Time Crystal repair tag (custom tag for Time Crystal as repair material)
    private static final TagKey<Item> TIME_CRYSTAL_TAG = TagKey.create(
        net.minecraft.core.registries.Registries.ITEM,
        CompatResourceLocation.create(ChronoDawn.MOD_ID, "repairs_time_tyrant_armor")
    );

    private static final EnumMap<ArmorType, Integer> DEFENSE_VALUES = Util.make(new EnumMap<>(ArmorType.class), map -> {
        map.put(ArmorType.BOOTS, 3);
        map.put(ArmorType.LEGGINGS, 6);
        map.put(ArmorType.CHESTPLATE, 8);
        map.put(ArmorType.HELMET, 3);
        map.put(ArmorType.BODY, 8);
    });

    public static final Holder<ArmorMaterial> MATERIAL = createMaterial();

    private static Holder<ArmorMaterial> createMaterial() {
        ResourceLocation id = CompatResourceLocation.create(ChronoDawn.MOD_ID, "time_tyrant");

        // In 1.21.2, ArmorMaterial constructor signature:
        // (int durabilityMultiplier, Map<ArmorType,Integer> defense, int enchantmentValue,
        //  Holder<SoundEvent> equipSound, float toughness, float knockbackResistance,
        //  TagKey<Item> repairIngredient, ResourceLocation equipmentAsset)
        ArmorMaterial material = new ArmorMaterial(
            37,  // durability multiplier (netherite-level)
            DEFENSE_VALUES,  // defense values
            18,  // enchantment value
            SoundEvents.ARMOR_EQUIP_NETHERITE,  // equip sound (Holder<SoundEvent>)
            2.5f,  // toughness
            0.1f,  // knockback resistance
            TIME_CRYSTAL_TAG,  // repair ingredient as TagKey
            id  // ResourceLocation for equipment asset
        );

        return Holder.direct(material);
    }
}
