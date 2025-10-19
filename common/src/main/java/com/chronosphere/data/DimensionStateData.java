package com.chronosphere.data;

import com.chronosphere.Chronosphere;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World saved data for dimension state.
 *
 * This class manages the persistent state of the Chronosphere dimension:
 * - Whether the dimension is stabilized (after Time Tyrant defeat)
 * - Current time distortion level (Slowness IV or V)
 *
 * Reference: data-model.md (Data Persistence - Dimension State)
 */
public class DimensionStateData extends ChronosphereWorldData {
    private static final String DATA_NAME = Chronosphere.MOD_ID + "_dimension_state";

    /**
     * Time distortion level enum.
     */
    public enum TimeDistortionLevel {
        SLOWNESS_IV(4),   // Default level (before Eye of Chronos)
        SLOWNESS_V(5);    // Enhanced level (after Eye of Chronos obtained)

        private final int amplifier;

        TimeDistortionLevel(int amplifier) {
            this.amplifier = amplifier;
        }

        public int getAmplifier() {
            return amplifier;
        }
    }

    private boolean isStabilized;
    private TimeDistortionLevel timeDistortionLevel;

    /**
     * Default constructor (called when creating new data).
     */
    public DimensionStateData() {
        this.isStabilized = false;
        this.timeDistortionLevel = TimeDistortionLevel.SLOWNESS_IV;
    }

    /**
     * Get or create dimension state data for the given level.
     *
     * @param level ServerLevel to get data from
     * @return Dimension state data instance
     */
    public static DimensionStateData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(DimensionStateData::new, DimensionStateData::load, null),
            DATA_NAME
        );
    }

    /**
     * Load dimension state data from NBT.
     *
     * @param tag CompoundTag to read from
     * @param registries Registry access for deserialization
     * @return Loaded dimension state data instance
     */
    private static DimensionStateData load(CompoundTag tag, HolderLookup.Provider registries) {
        DimensionStateData data = new DimensionStateData();
        data.isStabilized = tag.getBoolean("is_stabilized");
        data.timeDistortionLevel = TimeDistortionLevel.valueOf(
            tag.getString("time_distortion_level")
        );
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("is_stabilized", isStabilized);
        tag.putString("time_distortion_level", timeDistortionLevel.name());
        return tag;
    }

    // TODO: Add dimension state management methods in future phases:
    // - isStabilized()
    // - setStabilized(boolean stabilized)
    // - getTimeDistortionLevel()
    // - setTimeDistortionLevel(TimeDistortionLevel level)
}
