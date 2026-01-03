package com.chronodawn.compat;

import com.chronodawn.compat.SavedDataHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Base class for SavedData that abstracts version differences (Minecraft 1.20.1).
 *
 * This class handles the 1.20.1 save() signature (without HolderLookup.Provider)
 * and delegates to version-independent saveData() and loadData() methods.
 *
 * Subclasses should implement SavedDataHandler instead of overriding save() directly.
 *
 * Usage Example:
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
public abstract class CompatSavedData extends SavedData implements SavedDataHandler {
    /**
     * Save data to NBT (Minecraft 1.20.1 signature).
     * This method delegates to version-independent saveData().
     *
     * @param tag CompoundTag to write to
     * @return The same CompoundTag
     */
    @Override
    public CompoundTag save(CompoundTag tag) {
        // Call version-independent saveData()
        return saveData(tag);
    }

    /**
     * Version-independent save method.
     * Subclasses must implement this.
     *
     * @param tag CompoundTag to write to
     * @return The same CompoundTag (for chaining)
     */
    @Override
    public abstract CompoundTag saveData(CompoundTag tag);

    /**
     * Version-independent load method.
     * Subclasses must implement this.
     *
     * @param tag CompoundTag to read from
     */
    @Override
    public abstract void loadData(CompoundTag tag);

    /**
     * Version-compatible wrapper for DimensionDataStorage.computeIfAbsent().
     * Handles API differences between 1.20.1 and 1.21.1.
     *
     * 1.20.1: computeIfAbsent(Function<CompoundTag, T>, Supplier<T>, String)
     * 1.21.1: computeIfAbsent(SavedData.Factory<T>, String)
     *
     * @param storage DimensionDataStorage instance
     * @param constructor Supplier that creates new instance
     * @param deserializer BiFunction that loads from NBT (tag, provider) -> T
     * @param id SavedData ID
     * @param <T> SavedData type
     * @return Loaded or newly created SavedData instance
     */
    public static <T extends SavedData> T computeIfAbsent(
        DimensionDataStorage storage,
        Supplier<T> constructor,
        BiFunction<CompoundTag, HolderLookup.Provider, T> deserializer,
        String id
    ) {
        // 1.20.1: computeIfAbsent(Function<CompoundTag, T>, Supplier<T>, String)
        return storage.computeIfAbsent(
            tag -> deserializer.apply(tag, null), // HolderLookup.Provider is null in 1.20.1
            constructor,
            id
        );
    }
}
