package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Saved data for ChronoDawn independent time.
 * Persists the independent time value across world saves/loads.
 *
 * Task: T301 [P] Fix bed sleeping mechanic in ChronoDawn
 */
public class ChronoDawnTimeData extends SavedData {

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
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(
                ChronoDawnTimeData::new,
                ChronoDawnTimeData::load,
                null // No data fixer type needed
            ),
            DATA_NAME
        );
    }

    /**
     * Load data from NBT.
     */
    public static ChronoDawnTimeData load(CompoundTag tag, HolderLookup.Provider provider) {
        long time = tag.getLong(TIME_KEY);
        ChronoDawn.LOGGER.info("ChronoDawnTimeData: Loaded independent time = {}", time);
        return new ChronoDawnTimeData(time);
    }

    /**
     * Save data to NBT.
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong(TIME_KEY, independentTime);
        ChronoDawn.LOGGER.info("ChronoDawnTimeData: Saved independent time = {}", independentTime);
        return tag;
    }

    public long getIndependentTime() {
        return independentTime;
    }

    public void setIndependentTime(long time) {
        this.independentTime = time;
        setDirty(); // Mark as dirty to trigger save
    }
}
