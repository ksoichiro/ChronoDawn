package com.chronodawn.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Abstracts ItemStack custom data operations across Minecraft versions.
 *
 * Version Differences:
 * - 1.20.1: Uses NBT tags (stack.getOrCreateTag())
 * - 1.21.1: Uses Data Components (DataComponents.CUSTOM_DATA)
 *
 * Error Handling:
 * - Returns default values (empty string, 0, false) instead of throwing exceptions
 * - Never returns null from interface methods
 * - Handles null ItemStack gracefully (returns defaults)
 *
 * Usage Example:
 * <pre>{@code
 * // Write
 * CompatHandlers.ITEM_DATA.setString(stack, "TargetStructure", "ancient_ruins");
 *
 * // Read
 * String target = CompatHandlers.ITEM_DATA.getString(stack, "TargetStructure");
 * }</pre>
 */
public interface ItemDataHandler {
    /**
     * Set a string value in the ItemStack's custom data.
     *
     * @param stack ItemStack to modify (null-safe)
     * @param key Data key
     * @param value String value
     */
    void setString(ItemStack stack, String key, String value);

    /**
     * Get a string value from the ItemStack's custom data.
     *
     * @param stack ItemStack to read from (null-safe)
     * @param key Data key
     * @return String value, or empty string if not found or stack is null
     */
    String getString(ItemStack stack, String key);

    /**
     * Set an integer value in the ItemStack's custom data.
     *
     * @param stack ItemStack to modify (null-safe)
     * @param key Data key
     * @param value Integer value
     */
    void setInt(ItemStack stack, String key, int value);

    /**
     * Get an integer value from the ItemStack's custom data.
     *
     * @param stack ItemStack to read from (null-safe)
     * @param key Data key
     * @return Integer value, or 0 if not found or stack is null
     */
    int getInt(ItemStack stack, String key);

    /**
     * Check if the ItemStack has a specific key in its custom data.
     *
     * @param stack ItemStack to check (null-safe)
     * @param key Data key
     * @return true if key exists, false if not found or stack is null
     */
    boolean contains(ItemStack stack, String key);

    /**
     * Get the raw NBT tag from the ItemStack's custom data.
     * This is provided for complex operations that need direct NBT access.
     *
     * @param stack ItemStack to read from (null-safe)
     * @return CompoundTag (never null - returns empty tag if no data or stack is null)
     */
    CompoundTag getCustomData(ItemStack stack);

    /**
     * Update the ItemStack's custom data using a Consumer.
     * This allows batch updates without multiple copies.
     *
     * @param stack ItemStack to modify (null-safe)
     * @param updater Consumer that modifies the CompoundTag (receives empty tag if stack is null)
     */
    void updateCustomData(ItemStack stack, Consumer<CompoundTag> updater);
}
