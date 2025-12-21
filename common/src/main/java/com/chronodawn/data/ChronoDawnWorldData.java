package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

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
 * Reference: data-model.md (Data Persistence)
 */
public abstract class ChronoDawnWorldData extends SavedData {
    /**
     * Save data to NBT CompoundTag.
     *
     * This method is called automatically by Minecraft when the world is saved.
     * Subclasses must implement this to serialize their data structures.
     *
     * @param tag CompoundTag to write data to
     * @param registries Registry access for serialization
     * @return The same CompoundTag (for chaining)
     */
    @Override
    public abstract CompoundTag save(CompoundTag tag, HolderLookup.Provider registries);

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
     * @param level ServerLevel to get data from
     * @param factory Factory method to create new data instance
     * @param name Data name (used for file name)
     * @param <T> Type of saved data
     * @return Loaded or newly created saved data instance
     */
    protected static <T extends SavedData> T getOrCreate(
        ServerLevel level,
        SavedData.Factory<T> factory,
        String name
    ) {
        return level.getDataStorage().computeIfAbsent(factory, name);
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
