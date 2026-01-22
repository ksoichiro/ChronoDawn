package com.chronodawn.compat.v1_20_1;

import com.chronodawn.compat.ItemDataHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * ItemStack data handler for Minecraft 1.20.1 (NBT-based API).
 *
 * Uses ItemStack.getOrCreateTag() and ItemStack.getTag() for storing ItemStack data.
 */
public class ItemDataHandler120 implements ItemDataHandler {
    @Override
    public void setString(ItemStack stack, String key, String value) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        stack.getOrCreateTag().putString(key, value);
    }

    @Override
    public String getString(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }

        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getString(key) : "";
    }

    @Override
    public void setInt(ItemStack stack, String key, int value) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        stack.getOrCreateTag().putInt(key, value);
    }

    @Override
    public int getInt(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(key) : 0;
    }

    @Override
    public boolean contains(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key);
    }

    @Override
    public CompoundTag getCustomData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }

        CompoundTag tag = stack.getTag();
        return tag != null ? tag : new CompoundTag();
    }

    @Override
    public void updateCustomData(ItemStack stack, Consumer<CompoundTag> updater) {
        if (stack == null || stack.isEmpty()) {
            // Still invoke updater with empty tag for consistency
            updater.accept(new CompoundTag());
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        updater.accept(tag);
    }
}
