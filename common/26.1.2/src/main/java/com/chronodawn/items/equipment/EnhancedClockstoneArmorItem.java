package com.chronodawn.items.equipment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Enhanced Clockstone Armor Item - Tier 2 time-themed armor pieces.
 *
 * Advanced tier armor crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-comparable protection with superior enchantability.
 *
 * Armor Set:
 * - Helmet: Defense 3, Durability 308
 * - Chestplate: Defense 7, Durability 448
 * - Leggings: Defense 6, Durability 420
 * - Boots: Defense 3, Durability 364
 *
 * Total Set Defense: 19 (iron: 15, diamond: 20, clockstone: 15)
 * Toughness: 2.0f (same as diamond)
 * Enchantability: 16 (better than iron/clockstone/diamond)
 *
 * Special Ability (Full Set Bonus):
 * - Complete immunity to ChronoDawn time distortion effects (Slowness IV/V)
 * - Players wearing full Enhanced Clockstone armor are not affected by time distortion
 * - Implemented in TimeDistortionEffect.java
 *
 * Crafting Recipes:
 * - Helmet: Enhanced Clockstone x5 + Time Crystal x1
 * - Chestplate: Enhanced Clockstone x8 + Time Crystal x1
 * - Leggings: Enhanced Clockstone x7 + Time Crystal x1
 * - Boots: Enhanced Clockstone x4 + Time Crystal x1
 *
 * Reference: T252, T254 - Create Enhanced Clockstone Armor with time distortion immunity
 *
 * Note: In 1.21.5, ArmorItem has been removed. Items now use data components
 * and Item.Properties#humanoidArmor() instead of inheritance.
 */
public class EnhancedClockstoneArmorItem extends Item {
    public EnhancedClockstoneArmorItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Enhanced Clockstone Armor item.
     *
     * @param type Armor type (helmet, chestplate, leggings, boots)
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties(ArmorType type) {
        return new Properties()
                .stacksTo(1)
                // Use humanoidArmor() to set up armor components
                .humanoidArmor(EnhancedClockstoneArmorMaterial.ENHANCED_CLOCKSTONE.value(), type);
    }

    /**
     * Check if player is wearing full Enhanced Clockstone armor set.
     * Used by TimeDistortionEffect to grant immunity to time distortion.
     *
     * @param player The player to check
     * @return true if player is wearing full Enhanced Clockstone armor set
     */
    public static boolean isWearingFullSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof EnhancedClockstoneArmorItem &&
               player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof EnhancedClockstoneArmorItem &&
               player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof EnhancedClockstoneArmorItem &&
               player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof EnhancedClockstoneArmorItem;
    }
}
