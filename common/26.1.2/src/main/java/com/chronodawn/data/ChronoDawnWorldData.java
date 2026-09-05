package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Base class for world saved data in ChronoDawn mod.
 *
 * This class provides a foundation for persisting mod-specific data to the world save file.
 * Subclasses should implement specific data structures (portal registry, player progress, dimension state).
 *
 * Minecraft's SavedData API automatically handles:
 * - Serialization/deserialization (via NBT CompoundTag)
 * - File I/O (saved to <world>/data/<mod_id>_<data_name>.dat)
 * - Lazy loading (data loaded on-demand, not at world load)
 *
 * Version Compatibility:
 * - Extends CompatSavedData to support both Minecraft 1.20.1 and 1.21.1
 * - Subclasses must implement saveData() and loadData() instead of save()
 *
 * Note: In 1.21.5, SavedData.Factory was replaced with SavedDataType.
 *
 * Reference: data-model.md (Data Persistence)
 */
public abstract class ChronoDawnWorldData extends CompatSavedData {
    /**
     * Save data to NBT CompoundTag (version-independent).
     *
     * This method is called automatically by Minecraft when the world is saved.
     * Subclasses must implement this to serialize their data structures.
     *
     * @param tag CompoundTag to write data to
     * @return The same CompoundTag (for chaining)
     */
    @Override
    public abstract CompoundTag saveData(CompoundTag tag);

    /**
     * Load data from NBT CompoundTag (version-independent).
     *
     * This method is called when the data is loaded from disk.
     * Subclasses must implement this to deserialize their data structures.
     *
     * @param tag CompoundTag to read data from
     */
    @Override
    public abstract void loadData(CompoundTag tag);

    /**
     * Mark this data as dirty (modified).
     * This signals Minecraft to save the data on the next save operation.
     */
    protected void markDirty() {
        this.setDirty();
    }

    /**
     * Helper method to get or create world data for a specific type.
     *
     * In 1.21.5, this uses SavedDataType instead of SavedData.Factory.
     *
     * @param level ServerLevel to get data from
     * @param type SavedDataType that defines how to load/save the data
     * @param <T> Type of saved data
     * @return Loaded or newly created saved data instance
     */
    protected static <T extends SavedData> T getOrCreate(
        ServerLevel level,
        SavedDataType<T> type
    ) {
        return level.getDataStorage().computeIfAbsent(type);
    }

    /**
     * Log a debug message (convenience method for subclasses).
     *
     * @param message Message to log
     */
    protected void debug(String message) {
        ChronoDawn.LOGGER.debug("[WorldData] {}", message);
    }
}
