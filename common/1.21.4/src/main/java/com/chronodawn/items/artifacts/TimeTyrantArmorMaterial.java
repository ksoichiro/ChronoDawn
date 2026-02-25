package com.chronodawn.items.artifacts;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.EnumMap;

public class TimeTyrantArmorMaterial {
    // Base durability multiplier
    public static final int BASE_DURABILITY = 37;

    // Time Crystal repair tag
    // TEMPORARY: Using vanilla tag to test if tag mechanism works
    // TODO: Find proper solution for custom mod tags in NeoForge 1.21.2
    private static final TagKey<Item> TIME_CRYSTAL_TAG = ItemTags.REPAIRS_NETHERITE_ARMOR;

    public static final Holder<ArmorMaterial> MATERIAL = Holder.direct(
        new ArmorMaterial(
            BASE_DURABILITY,  // durability multiplier
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 3);
                map.put(ArmorType.LEGGINGS, 6);
                map.put(ArmorType.CHESTPLATE, 8);
                map.put(ArmorType.HELMET, 3);
                map.put(ArmorType.BODY, 8);
            }),
            18,  // enchantment value
            SoundEvents.ARMOR_EQUIP_NETHERITE,  // equip sound
            2.5f,  // toughness
            0.1f,  // knockback resistance
            TIME_CRYSTAL_TAG,  // repair ingredient
            CompatResourceLocation.createEquipmentAssetKey(ChronoDawn.MOD_ID, "time_tyrant")  // equipment asset key (1.21.4+)
        )
    );
}
