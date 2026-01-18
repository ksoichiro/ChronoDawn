package com.chronodawn.items.equipment;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Clockstone Armor Item - Tier 1 time-themed armor pieces.
 *
 * Basic tier armor crafted from Clockstone and Time Crystal.
 * Provides better protection than iron equipment but below diamond tier.
 *
 * Armor Set:
 * - Helmet: Defense 2, Durability 165
 * - Chestplate: Defense 6, Durability 240
 * - Leggings: Defense 5, Durability 225
 * - Boots: Defense 2, Durability 195
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
 * - All pieces require Time Crystal x1 for enhanced durability
 *
 * Reference: tasks.md (T215)
 */
public class ClockstoneArmorItem extends ArmorItem {
    public ClockstoneArmorItem(ArmorType type, Properties properties) {
        // In 1.21.2, ArmorItem constructor expects ArmorMaterial, not Holder<ArmorMaterial>
        super(ClockstoneArmorMaterial.CLOCKSTONE.value(), type, properties);
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
                .durability(type.getDurability(20)); // Durability multiplier: 20
    }
}
