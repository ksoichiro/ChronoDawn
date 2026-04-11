package com.chronodawn.items.equipment;

import net.minecraft.world.item.ArmorItem;

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
public class TemporalAmberArmorItem extends ArmorItem {
    public TemporalAmberArmorItem(Type type, Properties properties) {
        // 1.20.1: ArmorItem constructor takes ArmorMaterial directly (not Holder)
        super(TemporalAmberArmorMaterial.TEMPORAL_AMBER.value(), type, properties);
    }

    /**
     * Create default properties for Temporal Amber Armor item.
     *
     * @param type Armor type (helmet, chestplate, leggings, boots)
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties(Type type) {
        // 1.20.1: ArmorItem.Type.getDurability() does not exist, use fixed values
        int durability = switch (type) {
            case HELMET -> 385;      // Multiplier 35
            case CHESTPLATE -> 560;
            case LEGGINGS -> 525;
            case BOOTS -> 455;
        };

        return new Properties()
                .stacksTo(1)
                .durability(durability);
    }

    /**
     * Check if player is wearing full Temporal Amber armor set.
     *
     * @param player The player to check
     * @return true if player is wearing full Temporal Amber armor set
     */
    public static boolean isWearingFullSet(net.minecraft.world.entity.player.Player player) {
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).getItem() instanceof TemporalAmberArmorItem &&
               player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof TemporalAmberArmorItem;
    }
}
