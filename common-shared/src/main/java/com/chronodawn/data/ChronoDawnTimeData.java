package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import com.chronodawn.compat.CompatSavedData;

/**
 * Saved data for ChronoDawn independent time.
 * Persists the independent time value across world saves/loads.
 *
 * Task: T301 [P] Fix bed sleeping mechanic in ChronoDawn
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
     */
    public static ChronoDawnTimeData load(CompoundTag tag, HolderLookup.Provider provider) {
        ChronoDawnTimeData data = new ChronoDawnTimeData();
        data.loadData(tag);
        ChronoDawn.LOGGER.debug("ChronoDawnTimeData: Loaded independent time = {}", data.independentTime);
        return data;
    }

    /**
     * Save data to NBT (version-independent).
     */
    @Override
    public CompoundTag saveData(CompoundTag tag) {
        tag.putLong(TIME_KEY, independentTime);
        ChronoDawn.LOGGER.debug("ChronoDawnTimeData: Saved independent time = {}", independentTime);
        return tag;
    }

    /**
     * Load data from NBT (version-independent).
     */
    @Override
    public void loadData(CompoundTag tag) {
        independentTime = tag.getLong(TIME_KEY);
    }

    public long getIndependentTime() {
        return independentTime;
    }

    public void setIndependentTime(long time) {
        this.independentTime = time;
        setDirty(); // Mark as dirty to trigger save
    }
}
