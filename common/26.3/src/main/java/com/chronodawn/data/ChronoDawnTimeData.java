package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/**
 * Saved data for ChronoDawn independent time.
 * Persists the independent time value across world saves/loads.
 *
 * This is the 1.21.5-specific version with single-parameter load method
 * and updated NBT API (getLongOr instead of getLong).
 */
public class ChronoDawnTimeData extends CompatSavedData {

    private static final String DATA_NAME = "chronodawn_time";
    private static final String TIME_KEY = "IndependentTime";

    private long independentTime;

    public ChronoDawnTimeData() {
        this.independentTime = 0L;
    }

    public ChronoDawnTimeData(long independentTime) {
        this.independentTime = independentTime;
    }

    /**
     * Get or create the ChronoDawnTimeData for the given level.
     */
    public static ChronoDawnTimeData get(ServerLevel level) {
        return CompatSavedData.computeIfAbsent(
            level.getDataStorage(),
            ChronoDawnTimeData::new,
            ChronoDawnTimeData::load,
            DATA_NAME
        );
    }

    /**
     * Load data from NBT.
     * In 1.21.5, the load method no longer needs HolderLookup.Provider.
     */
    public static ChronoDawnTimeData load(CompoundTag tag) {
        ChronoDawnTimeData data = new ChronoDawnTimeData();
        data.loadData(tag);
        ChronoDawn.LOGGER.debug("ChronoDawnTimeData: Loaded independent time = {}", data.independentTime);
        return data;
    }

    /**
     * Save data to NBT.
     */
    @Override
    public CompoundTag saveData(CompoundTag tag) {
        tag.putLong(TIME_KEY, independentTime);
        ChronoDawn.LOGGER.debug("ChronoDawnTimeData: Saved independent time = {}", independentTime);
        return tag;
    }

    /**
     * Load data from NBT.
     */
    @Override
    public void loadData(CompoundTag tag) {
        independentTime = tag.getLongOr(TIME_KEY, 0L);
    }

    public long getIndependentTime() {
        return independentTime;
    }

    public void setIndependentTime(long time) {
        this.independentTime = time;
        setDirty();
    }
}
