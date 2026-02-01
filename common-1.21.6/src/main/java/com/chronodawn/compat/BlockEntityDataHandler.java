package com.chronodawn.compat;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Interface for version-independent BlockEntity data serialization.
 *
 * Version Differences:
 * - 1.20.1: saveAdditional(CompoundTag) / load(CompoundTag)
 * - 1.21.1-1.21.5: saveAdditional(CompoundTag, HolderLookup.Provider) / loadAdditional(CompoundTag, HolderLookup.Provider)
 * - 1.21.6+: saveAdditional(ValueOutput) / loadAdditional(ValueInput)
 *
 * Usage:
 * Subclasses should extend CompatBlockEntity (version-specific base class)
 * and implement only saveData() and loadData() methods.
 */
public interface BlockEntityDataHandler {
    /**
     * Save BlockEntity data (version-independent).
     *
     * @param output ValueOutput to write to (never null)
     */
    void saveData(ValueOutput output);

    /**
     * Load BlockEntity data (version-independent).
     *
     * @param input ValueInput to read from (never null)
     */
    void loadData(ValueInput input);
}
