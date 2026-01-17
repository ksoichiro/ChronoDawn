package com.chronodawn.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Minecraft 1.20.1 implementation of ItemDurabilityHandler.
 */
public class ItemDurabilityHandler120 implements ItemDurabilityHandler {
    @Override
    public void damageItem(ItemStack stack, int amount, Player player, EquipmentSlot slot) {
        // 1.20.1: hurtAndBreak(int, LivingEntity, Consumer<LivingEntity>)
        stack.hurtAndBreak(amount, player, p -> p.broadcastBreakEvent(slot));
    }
}
