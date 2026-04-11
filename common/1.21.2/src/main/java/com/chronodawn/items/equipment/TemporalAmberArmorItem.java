package com.chronodawn.items.equipment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Temporal Amber Armor Item - Tier 2 armor pieces from Temporal Amber.
 *
 * Tier 2 armor crafted from Temporal Amber.
 * Provides diamond-comparable protection with enhanced durability.
 *
 * Armor Set:
 * - Helmet: Defense 3, Durability 385
 * - Chestplate: Defense 7, Durability 560
 * - Leggings: Defense 6, Durability 525
 * - Boots: Defense 3, Durability 455
 *
 * Total Set Defense: 19 (iron: 15, diamond: 20)
 * Toughness: 1.5f
 * Enchantability: 10
 *
 * Repair Material: Temporal Amber Dust
 *
 * Reference: T302 - Temporal Amber Armor Items
 */
public class TemporalAmberArmorItem extends Item {
    public TemporalAmberArmorItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Temporal Amber Armor item.
     *
     * @param type Armor type (helmet, chestplate, leggings, boots)
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties(ArmorType type) {
        return new Properties()
                .stacksTo(1)
                // Use humanoidArmor() to set up armor components
                .humanoidArmor(TemporalAmberArmorMaterial.TEMPORAL_AMBER.value(), type);
    }

    /**
     * Check if player is wearing full Temporal Amber armor set.
     *
     * @param player The player to check
     * @return true if player is wearing full Temporal Amber armor set
     */
    public static boolean isWearingFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof TemporalAmberArmorItem;
    }
}
