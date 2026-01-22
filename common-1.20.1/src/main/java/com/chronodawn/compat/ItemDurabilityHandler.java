package com.chronodawn.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Version-independent interface for damaging items.
 */
public interface ItemDurabilityHandler {
    /**
     * Damage an item by the specified amount.
     *
     * @param stack The ItemStack to damage
     * @param amount Amount of durability to remove
     * @param player The player using the item
     * @param slot The equipment slot
     */
    void damageItem(ItemStack stack, int amount, Player player, EquipmentSlot slot);

    /**
     * Get the version-specific handler instance.
     */
    static ItemDurabilityHandler getInstance() {
        // This will be loaded from compat/v1_20_1 or compat/v1_21_1 depending on version
        try {
            // Try 1.21.1 first
            Class<?> handler121 = Class.forName("com.chronodawn.compat.ItemDurabilityHandler121");
            return (ItemDurabilityHandler) handler121.getDeclaredConstructor().newInstance();
        } catch (Exception e1) {
            // Fall back to 1.20.1
            try {
                Class<?> handler120 = Class.forName("com.chronodawn.compat.ItemDurabilityHandler120");
                return (ItemDurabilityHandler) handler120.getDeclaredConstructor().newInstance();
            } catch (Exception e2) {
                throw new RuntimeException("Failed to load ItemDurabilityHandler", e2);
            }
        }
    }
}
