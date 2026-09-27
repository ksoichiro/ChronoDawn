package com.chronodawn.compat;

import net.minecraft.nbt.CompoundTag;

/**
 * Interface for version-independent SavedData serialization.
 *
 * Version Differences:
 * - 1.20.1: save(CompoundTag)
 * - 1.21.1: save(CompoundTag, HolderLookup.Provider)
 *
 * Usage:
 * Subclasses should extend CompatSavedData (version-specific base class)
 * and implement only saveData() and loadData() methods.
 *
 * Example:
 * <pre>{@code
 * public class MyData extends CompatSavedData {
 *     @Override
 *     public CompoundTag saveData(CompoundTag tag) {
 *         tag.putBoolean("myFlag", myFlag);
 *         return tag;
 *     }
 *
 *     @Override
 *     public void loadData(CompoundTag tag) {
 *         myFlag = tag.getBoolean("myFlag");
 *     }
 * }
 * }</pre>
 */
public interface SavedDataHandler {
    /**
     * Save data to NBT tag (version-independent).
     *
     * @param tag CompoundTag to write to (never null)
     * @return The same CompoundTag (for chaining)
     */
    CompoundTag saveData(CompoundTag tag);

    /**
     * Load data from NBT tag (version-independent).
     *
     * @param tag CompoundTag to read from (never null)
     */
    void loadData(CompoundTag tag);
}
