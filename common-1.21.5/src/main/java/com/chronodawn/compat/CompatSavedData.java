package com.chronodawn.compat;

import com.chronodawn.compat.SavedDataHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for SavedData that abstracts version differences (Minecraft 1.21.5).
 *
 * This class handles the 1.21.5 save() signature and SavedDataType system,
 * and delegates to version-independent saveData() and loadData() methods.
 *
 * Subclasses should implement SavedDataHandler instead of overriding save() directly.
 *
 * Note: In 1.21.5, SavedData.Factory was replaced with SavedDataType.
 * The save() method no longer takes HolderLookup.Provider parameter.
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
 *         myFlag = tag.getBooleanOr("myFlag", false);
 *     }
 * }
 * }</pre>
 */
public abstract class CompatSavedData extends SavedData implements SavedDataHandler {
    /**
     * Save data to NBT (Minecraft 1.21.5 signature).
     * This method delegates to version-independent saveData().
     *
     * Note: In 1.21.5, SavedData.save() may have different signature.
     * We provide our own implementation without @Override.
     *
     * @param tag CompoundTag to write to
     * @return The same CompoundTag
     */
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
     * Handles API differences between 1.21.4 and 1.21.5.
     *
     * In 1.21.5, SavedData.Factory was replaced with SavedDataType.
     * The id is now passed to SavedDataType constructor, not computeIfAbsent.
     *
     * @param storage DimensionDataStorage instance
     * @param constructor Supplier that creates new instance
     * @param loader Function that loads from NBT (tag) -> T
     * @param id SavedData ID
     * @param <T> SavedData type
     * @return Loaded or newly created SavedData instance
     */
    public static <T extends SavedData> T computeIfAbsent(
        DimensionDataStorage storage,
        Supplier<T> constructor,
        Function<CompoundTag, T> loader,
        String id
    ) {
        // 1.21.5: SavedDataType takes id in constructor
        // computeIfAbsent only takes SavedDataType
        SavedDataType<T> type = new SavedDataType<>(
            id,
            constructor,
            loader
        );
        return storage.computeIfAbsent(type);
    }
}
