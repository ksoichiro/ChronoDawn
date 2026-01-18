package com.chronodawn.compat.v1_21_2;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/**
 * Minecraft 1.21.2 implementation of CompatInteractionResult.
 *
 * In 1.21.2, InteractionResult.sidedSuccess no longer requires an ItemStack parameter.
 */
public class CompatInteractionResult {
    /**
     * Returns a success result that differs based on the logical side.
     *
     * @param stack The ItemStack being used (ignored in 1.21.2)
     * @param isClientSide Whether this is the client side
     * @return SUCCESS on server side, CONSUME on client side
     */
    public static InteractionResult sidedSuccess(ItemStack stack, boolean isClientSide) {
        // In 1.21.2, sidedSuccess was removed; use SUCCESS on server, CONSUME on client
        return isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }
}
