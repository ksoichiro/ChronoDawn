package com.chronodawn.items.equipment;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Clockstone Armor Item - Tier 1 time-themed armor pieces.
 *
 * Basic tier armor crafted from Clockstone.
 * Provides better protection than iron equipment but below diamond tier.
 *
 * Armor Set:
 * - Helmet: Defense 2, Durability 220
 * - Chestplate: Defense 6, Durability 320
 * - Leggings: Defense 5, Durability 300
 * - Boots: Defense 2, Durability 260
 *
 * Total Set Defense: 15 (same as iron)
 * Toughness: 1.0f (better than iron's 0.0f)
 * Enchantability: 14 (same as iron)
 *
 * Crafting Recipes:
 * - Helmet: Clockstone x5
 * - Chestplate: Clockstone x8
 * - Leggings: Clockstone x7
 * - Boots: Clockstone x4
 *
 * Reference: tasks.md (T215)
 *
 * Note: In 1.21.5, ArmorItem has been removed. Items now use data components
 * and Item.Properties#humanoidArmor() instead of inheritance.
 */
public class ClockstoneArmorItem extends Item {
    public ClockstoneArmorItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Armor item.
     *
     * @param type Armor type (helmet, chestplate, leggings, boots)
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties(ArmorType type) {
        return new Properties()
                .stacksTo(1)
                // Use humanoidArmor() to set up armor components
                .humanoidArmor(ClockstoneArmorMaterial.CLOCKSTONE.value(), type);
    }
}
