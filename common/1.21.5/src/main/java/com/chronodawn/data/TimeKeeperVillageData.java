package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Saved data for Time Keeper Village placement state in ChronoDawn dimension.
 *
 * This is the 1.21.5-specific version with single-parameter load method
 * and updated NBT API (getBooleanOr/getIntOr instead of getBoolean/getInt).
 *
 * Tracks:
 * - Whether the village has been placed
 * - The position where the village was placed
 */
public class TimeKeeperVillageData extends CompatSavedData {
    private static final String DATA_NAME = "chronodawn_time_keeper_village";

    private boolean placed = false;
    private BlockPos position = BlockPos.ZERO;

    public TimeKeeperVillageData() {
        super();
    }

    /**
     * Get or create the Time Keeper Village data for a ChronoDawn level.
     *
     * @param level ServerLevel (should be ChronoDawn dimension)
     * @return Time Keeper Village data instance
     */
    public static TimeKeeperVillageData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return CompatSavedData.computeIfAbsent(
            storage,
            TimeKeeperVillageData::new,
            TimeKeeperVillageData::load,
            DATA_NAME
        );
    }

    /**
     * Load data from NBT.
     * In 1.21.5, the load method no longer needs HolderLookup.Provider.
     *
     * @param tag NBT tag
     * @return Loaded data
     */
    public static TimeKeeperVillageData load(CompoundTag tag) {
        TimeKeeperVillageData data = new TimeKeeperVillageData();
        data.loadData(tag);
        ChronoDawn.LOGGER.debug("Loaded TimeKeeperVillageData: placed={}, position={}",
            data.placed, data.position);
        return data;
    }

    @Override
    public CompoundTag saveData(CompoundTag tag) {
        tag.putBoolean("Placed", placed);
        tag.putInt("PosX", position.getX());
        tag.putInt("PosY", position.getY());
        tag.putInt("PosZ", position.getZ());
        return tag;
    }

    @Override
    public void loadData(CompoundTag tag) {
        placed = tag.getBooleanOr("Placed", false);

        // Check for position data existence using contains
        CompoundTag posTag = tag.getCompoundOrEmpty("Position");
        if (!posTag.isEmpty()) {
            position = new BlockPos(
                posTag.getIntOr("X", 0),
                posTag.getIntOr("Y", 0),
                posTag.getIntOr("Z", 0)
            );
        } else {
            // Fallback to legacy format
            int x = tag.getIntOr("PosX", 0);
            int y = tag.getIntOr("PosY", 0);
            int z = tag.getIntOr("PosZ", 0);
            if (x != 0 || y != 0 || z != 0 || placed) {
                position = new BlockPos(x, y, z);
            }
        }
    }

    /**
     * Check if the village has been placed.
     *
     * @return true if village has been placed
     */
    public boolean isPlaced() {
        return placed;
    }

    /**
     * Get the position where the village was placed.
     *
     * @return Village position, or BlockPos.ZERO if not placed
     */
    public BlockPos getPosition() {
        return position;
    }

    /**
     * Mark the village as placed at the specified position.
     *
     * @param pos Position where the village was placed
     */
    public void setPlaced(BlockPos pos) {
        this.placed = true;
        this.position = pos.immutable();
        setDirty();
        ChronoDawn.LOGGER.debug("Time Keeper Village marked as placed at {}", pos);
    }
}
