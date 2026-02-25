package com.chronodawn.data;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import com.chronodawn.compat.CompatSavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Saved data for Time Keeper Village placement state in ChronoDawn dimension.
 *
 * Tracks:
 * - Whether the village has been placed
 * - The position where the village was placed
 *
 * This ensures the village is placed only once per world, near the spawn point.
 *
 * Task: T276 [US2] Implement TimeKeeperVillagePlacer.java
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
     *
     * @param tag NBT tag
     * @param provider Holder lookup provider
     * @return Loaded data
     */
    public static TimeKeeperVillageData load(CompoundTag tag, HolderLookup.Provider provider) {
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
        placed = tag.getBoolean("Placed");

        if (tag.contains("PosX") && tag.contains("PosY") && tag.contains("PosZ")) {
            position = new BlockPos(
                tag.getInt("PosX"),
                tag.getInt("PosY"),
                tag.getInt("PosZ")
            );
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
