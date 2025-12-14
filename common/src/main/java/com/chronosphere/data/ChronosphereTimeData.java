package com.chronosphere.data;

import com.chronosphere.Chronosphere;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Saved data for Chronosphere independent time.
 * Persists the independent time value across world saves/loads.
 *
 * Task: T301 [P] Fix bed sleeping mechanic in Chronosphere
 */
public class ChronosphereTimeData extends SavedData {

    private static final String DATA_NAME = "chronosphere_time";
    private static final String TIME_KEY = "IndependentTime";

    private long independentTime;

    public ChronosphereTimeData() {
        this.independentTime = 0L;
    }

    public ChronosphereTimeData(long independentTime) {
        this.independentTime = independentTime;
    }

    /**
     * Get or create the ChronosphereTimeData for the given level.
     */
    public static ChronosphereTimeData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(
                ChronosphereTimeData::new,
                ChronosphereTimeData::load,
                null // No data fixer type needed
            ),
            DATA_NAME
        );
    }

    /**
     * Load data from NBT.
     */
    public static ChronosphereTimeData load(CompoundTag tag, HolderLookup.Provider provider) {
        long time = tag.getLong(TIME_KEY);
        Chronosphere.LOGGER.info("ChronosphereTimeData: Loaded independent time = {}", time);
        return new ChronosphereTimeData(time);
    }

    /**
     * Save data to NBT.
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong(TIME_KEY, independentTime);
        Chronosphere.LOGGER.info("ChronosphereTimeData: Saved independent time = {}", independentTime);
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
