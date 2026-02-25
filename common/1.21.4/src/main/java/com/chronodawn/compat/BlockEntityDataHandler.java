package com.chronodawn.compat;

import net.minecraft.nbt.CompoundTag;

/**
 * Interface for version-independent BlockEntity data serialization.
 *
 * Version Differences:
 * - 1.20.1: saveAdditional(CompoundTag) / load(CompoundTag)
 * - 1.21.1: saveAdditional(CompoundTag, HolderLookup.Provider) / loadAdditional(CompoundTag, HolderLookup.Provider)
 *
 * Usage:
 * Subclasses should extend CompatBlockEntity (version-specific base class)
 * and implement only saveData() and loadData() methods.
 *
 * Example:
 * <pre>{@code
 * public class MyBlockEntity extends CompatBlockEntity {
 *     @Override
 *     public void saveData(CompoundTag tag) {
 *         tag.putString("DoorType", doorType);
 *     }
 *
 *     @Override
 *     public void loadData(CompoundTag tag) {
 *         if (tag.contains("DoorType")) {
 *             doorType = tag.getString("DoorType");
 *         }
 *     }
 * }
 * }</pre>
 */
public interface BlockEntityDataHandler {
    /**
     * Save BlockEntity data to NBT tag (version-independent).
     *
     * @param tag CompoundTag to write to (never null)
     */
    void saveData(CompoundTag tag);

    /**
     * Load BlockEntity data from NBT tag (version-independent).
     *
     * @param tag CompoundTag to read from (never null)
     */
    void loadData(CompoundTag tag);
}
