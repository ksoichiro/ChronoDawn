package com.chronodawn.compat.v1_21_2;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Minecraft 1.21.1 implementation of ItemDurabilityHandler.
 */
public class ItemDurabilityHandler121 implements ItemDurabilityHandler {
    @Override
    public void damageItem(ItemStack stack, int amount, Player player, EquipmentSlot slot) {
        // 1.21.1: hurtAndBreak(int, LivingEntity, EquipmentSlot)
        stack.hurtAndBreak(amount, player, slot);
    }
}
