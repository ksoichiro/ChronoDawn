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

        // Load time distortion level with default fallback
        String levelString = tag.getString("time_distortion_level");
        if (levelString.isEmpty()) {
            data.timeDistortionLevel = TimeDistortionLevel.SLOWNESS_IV;
            ChronoDawn.LOGGER.debug("DimensionStateData loaded with default time distortion level (SLOWNESS_IV)");
        } else {
            data.timeDistortionLevel = TimeDistortionLevel.valueOf(levelString);
            ChronoDawn.LOGGER.debug("DimensionStateData loaded: isStabilized={}, timeDistortionLevel={}",
                data.isStabilized, data.timeDistortionLevel);
        }

        return data;
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        tag.putBoolean("is_stabilized", isStabilized);
        tag.putString("time_distortion_level", timeDistortionLevel.name());
        ChronoDawn.LOGGER.debug("DimensionStateData saved: isStabilized={}, timeDistortionLevel={}",
            isStabilized, timeDistortionLevel);
        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
        isStabilized = tag.getBoolean("is_stabilized");

        // Load time distortion level with default fallback
        String levelString = tag.getString("time_distortion_level");
        if (levelString.isEmpty()) {
            timeDistortionLevel = TimeDistortionLevel.SLOWNESS_IV;
            ChronoDawn.LOGGER.debug("DimensionStateData loadData() with default time distortion level (SLOWNESS_IV)");
        } else {
            timeDistortionLevel = TimeDistortionLevel.valueOf(levelString);
            ChronoDawn.LOGGER.debug("DimensionStateData loadData(): isStabilized={}, timeDistortionLevel={}",
                isStabilized, timeDistortionLevel);
        }
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
            ChronoDawn.LOGGER.debug("Dimension stabilization state changed to: {}", stabilized);
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
            ChronoDawn.LOGGER.debug("Time distortion level changed to: {}", level);
        }
    }

    /**
     * Enhance time distortion when Eye of Chronos is obtained.
     * This is a convenience method that sets both stabilization and enhanced distortion.
     */
    public void enhanceTimeDistortion() {
        setStabilized(true);
        setTimeDistortionLevel(TimeDistortionLevel.SLOWNESS_V);
        ChronoDawn.LOGGER.debug("Dimension enhanced with Eye of Chronos - Slowness V activated");
    }
}
