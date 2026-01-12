package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World saved data for dimension state.
 *
 * This class manages the persistent state of the ChronoDawn dimension:
 * - Whether the dimension is stabilized (after Time Tyrant defeat)
 * - Current time distortion level (Slowness IV or V)
 *
 * Reference: data-model.md (Data Persistence - Dimension State)
 */
public class DimensionStateData extends ChronoDawnWorldData {
    private static final String DATA_NAME = ChronoDawn.MOD_ID + "_dimension_state";

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
        return CompatSavedData.computeIfAbsent(
            level.getDataStorage(),
            DimensionStateData::new,
            DimensionStateData::load,
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
    public CompoundTag saveData(CompoundTag tag) {
        tag.putBoolean("is_stabilized", isStabilized);
        tag.putString("time_distortion_level", timeDistortionLevel.name());
        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
        isStabilized = tag.getBoolean("is_stabilized");
        timeDistortionLevel = TimeDistortionLevel.valueOf(
            tag.getString("time_distortion_level")
        );
    }

    /**
     * Check if the dimension is stabilized (Time Tyrant defeated).
     *
     * @return true if dimension is stabilized
     */
    public boolean isStabilized() {
        return isStabilized;
    }

    /**
     * Set dimension stabilization state.
     * This should be called when Time Tyrant is defeated and Eye of Chronos is obtained.
     *
     * @param stabilized Whether the dimension is stabilized
     */
    public void setStabilized(boolean stabilized) {
        if (this.isStabilized != stabilized) {
            this.isStabilized = stabilized;
            this.setDirty();
            ChronoDawn.LOGGER.info("Dimension stabilization state changed to: {}", stabilized);
        }
    }

    /**
     * Get current time distortion level.
     *
     * @return Current time distortion level
     */
    public TimeDistortionLevel getTimeDistortionLevel() {
        return timeDistortionLevel;
    }

    /**
     * Set time distortion level.
     * This should be called when Eye of Chronos is obtained (upgrade to Slowness V).
     *
     * @param level Time distortion level to set
     */
    public void setTimeDistortionLevel(TimeDistortionLevel level) {
        if (this.timeDistortionLevel != level) {
            this.timeDistortionLevel = level;
            this.setDirty();
            ChronoDawn.LOGGER.info("Time distortion level changed to: {}", level);
        }
    }

    /**
     * Enhance time distortion when Eye of Chronos is obtained.
     * This is a convenience method that sets both stabilization and enhanced distortion.
     */
    public void enhanceTimeDistortion() {
        setStabilized(true);
        setTimeDistortionLevel(TimeDistortionLevel.SLOWNESS_V);
        ChronoDawn.LOGGER.info("Dimension enhanced with Eye of Chronos - Slowness V activated");
    }
}
