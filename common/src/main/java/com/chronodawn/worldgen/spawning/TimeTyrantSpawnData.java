package com.chronodawn.worldgen.spawning;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * Saved data for Time Tyrant spawning state.
 * Persists across server restarts to prevent duplicate spawning.
 */
public class TimeTyrantSpawnData extends SavedData {
    private static final String DATA_NAME = "time_tyrant_spawns";

    private final Set<BlockPos> spawnedDoors = new HashSet<>();
    private int spawnCount = 0;

    public TimeTyrantSpawnData() {
        super();
    }

    /**
     * Factory method for SavedData system.
     */
    public static Factory<TimeTyrantSpawnData> factory() {
        return new Factory<>(
            TimeTyrantSpawnData::new,
            TimeTyrantSpawnData::load,
            null
        );
    }

    /**
     * Load saved data from NBT.
     */
    public static TimeTyrantSpawnData load(CompoundTag tag, HolderLookup.Provider provider) {
        TimeTyrantSpawnData data = new TimeTyrantSpawnData();

        // Load spawned door positions
        ListTag doorsList = tag.getList("SpawnedDoors", Tag.TAG_COMPOUND);
        for (int i = 0; i < doorsList.size(); i++) {
            CompoundTag doorTag = doorsList.getCompound(i);
            BlockPos pos = BlockPos.of(doorTag.getLong("Pos"));
            data.spawnedDoors.add(pos);
        }

        // Load spawn count
        data.spawnCount = tag.getInt("SpawnCount");

        return data;
    }

    /**
     * Save data to NBT.
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // Save spawned door positions
        ListTag doorsList = new ListTag();
        for (BlockPos pos : spawnedDoors) {
            CompoundTag doorTag = new CompoundTag();
            doorTag.putLong("Pos", pos.asLong());
            doorsList.add(doorTag);
        }
        tag.put("SpawnedDoors", doorsList);

        // Save spawn count
        tag.putInt("SpawnCount", spawnCount);

        return tag;
    }

    /**
     * Check if a door has already spawned a Time Tyrant.
     */
    public boolean hasDoorSpawned(BlockPos pos) {
        return spawnedDoors.contains(pos);
    }

    /**
     * Mark a door as having spawned a Time Tyrant.
     */
    public void markDoorSpawned(BlockPos pos) {
        spawnedDoors.add(pos);
        setDirty();
    }

    /**
     * Get the current spawn count.
     */
    public int getSpawnCount() {
        return spawnCount;
    }

    /**
     * Increment the spawn count.
     */
    public void incrementSpawnCount() {
        spawnCount++;
        setDirty();
    }

    /**
     * Reset all spawn tracking (for testing/debugging).
     */
    public void reset() {
        spawnedDoors.clear();
        spawnCount = 0;
        setDirty();
    }

    /**
     * Get the data name for this SavedData.
     */
    public static String getDataName() {
        return DATA_NAME;
    }
}
