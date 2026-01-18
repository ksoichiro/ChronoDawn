package com.chronodawn.compat.v1_21_2;

import com.chronodawn.compat.ItemDataHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

/**
 * ItemStack data handler for Minecraft 1.21.1 (Data Components API).
 *
 * Uses DataComponents.CUSTOM_DATA and CustomData for storing ItemStack data.
 */
public class ItemDataHandler121 implements ItemDataHandler {
    @Override
    public void setString(ItemStack stack, String key, String value) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(key, value);
        });
    }

    @Override
    public String getString(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getString(key);
        }
        return "";
    }

    @Override
    public void setInt(ItemStack stack, String key, int value) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(key, value);
        });
    }

    @Override
    public int getInt(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().getInt(key);
        }
        return 0;
    }

    @Override
    public boolean contains(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag().contains(key);
        }
        return false;
    }

    @Override
    public CompoundTag getCustomData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new CompoundTag();
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag();
        }
        return new CompoundTag();
    }

    @Override
    public void updateCustomData(ItemStack stack, Consumer<CompoundTag> updater) {
        if (stack == null || stack.isEmpty()) {
            // Still invoke updater with empty tag for consistency
            updater.accept(new CompoundTag());
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }
}
