package com.chronodawn.items.equipment;

import net.minecraft.world.item.ArmorItem;

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
 */
public class EnhancedClockstoneArmorItem extends ArmorItem {
    public EnhancedClockstoneArmorItem(Type type, Properties properties) {
        // 1.20.1: ArmorItem constructor takes ArmorMaterial directly (not Holder)
        super(EnhancedClockstoneArmorMaterial.ENHANCED_CLOCKSTONE.value(), type, properties);
    }

    /**
     * Create default properties for Enhanced Clockstone Armor item.
     *
     * @param type Armor type (helmet, chestplate, leggings, boots)
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties(Type type) {
        // 1.20.1: ArmorItem.Type.getDurability() does not exist, use fixed values
        int durability = switch (type) {
            case HELMET -> 308;      // Multiplier 28 (between clockstone 20 and diamond 33)
            case CHESTPLATE -> 448;
            case LEGGINGS -> 420;
            case BOOTS -> 364;
        };

        return new Properties()
                .stacksTo(1)
                .durability(durability);
    }

    /**
     * Check if player is wearing full Enhanced Clockstone armor set.
     * Used by TimeDistortionEffect to grant immunity to time distortion.
     *
     * @param player The player to check
     * @return true if player is wearing full Enhanced Clockstone armor set
     */
    public static boolean isWearingFullSet(net.minecraft.world.entity.player.Player player) {
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof EnhancedClockstoneArmorItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof EnhancedClockstoneArmorItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() instanceof EnhancedClockstoneArmorItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof EnhancedClockstoneArmorItem;
    }
}
