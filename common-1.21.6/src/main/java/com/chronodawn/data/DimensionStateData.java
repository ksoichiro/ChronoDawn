package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

/**
 * World saved data for dimension state.
 *
 * This is the 1.21.5-specific version with single-parameter load method
 * and updated NBT API (getBooleanOr/getStringOr instead of getBoolean/getString).
 *
 * This class manages the persistent state of the ChronoDawn dimension:
 * - Whether the dimension is stabilized (after Time Tyrant defeat)
 * - Current time distortion level (Slowness IV or V)
 */
public class DimensionStateData extends ChronoDawnWorldData {
    private static final String DATA_NAME = ChronoDawn.MOD_ID + "_dimension_state";

    /**
     * Time distortion level enum.
     */
    public enum TimeDistortionLevel {
        SLOWNESS_IV(4),
        SLOWNESS_V(5);

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
     * Default constructor.
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
     * In 1.21.5, the load method no longer needs HolderLookup.Provider.
     *
     * @param tag CompoundTag to read from
     * @return Loaded dimension state data instance
     */
    private static DimensionStateData load(CompoundTag tag) {
        DimensionStateData data = new DimensionStateData();
        data.loadData(tag);
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
        isStabilized = tag.getBooleanOr("is_stabilized", false);

        String levelString = tag.getStringOr("time_distortion_level", "");
        if (levelString.isEmpty()) {
            timeDistortionLevel = TimeDistortionLevel.SLOWNESS_IV;
            ChronoDawn.LOGGER.debug("DimensionStateData loaded with default time distortion level (SLOWNESS_IV)");
        } else {
            try {
                timeDistortionLevel = TimeDistortionLevel.valueOf(levelString);
                ChronoDawn.LOGGER.debug("DimensionStateData loaded: isStabilized={}, timeDistortionLevel={}",
                    isStabilized, timeDistortionLevel);
            } catch (IllegalArgumentException e) {
                timeDistortionLevel = TimeDistortionLevel.SLOWNESS_IV;
                ChronoDawn.LOGGER.warn("Invalid time distortion level '{}', defaulting to SLOWNESS_IV", levelString);
            }
        }
    }

    /**
     * Check if the dimension is stabilized.
     *
     * @return true if dimension is stabilized
     */
    public boolean isStabilized() {
        return isStabilized;
    }

    /**
     * Set dimension stabilization state.
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
     */
    public void enhanceTimeDistortion() {
        setStabilized(true);
        setTimeDistortionLevel(TimeDistortionLevel.SLOWNESS_V);
        ChronoDawn.LOGGER.debug("Dimension enhanced with Eye of Chronos - Slowness V activated");
    }
}
