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
 * Saved data for Chronos Warden spawning state.
 * Persists across server restarts to prevent duplicate spawning.
 */
public class ChronosWardenSpawnData extends SavedData {
    private static final String DATA_NAME = "chronos_warden_spawns";

    private final Set<BlockPos> spawnedDoors = new HashSet<>();

    public ChronosWardenSpawnData() {
        super();
    }

    /**
     * Factory method for SavedData system.
     */
    public static Factory<ChronosWardenSpawnData> factory() {
        return new Factory<>(
            ChronosWardenSpawnData::new,
            ChronosWardenSpawnData::load,
            null
        );
    }

    /**
     * Load saved data from NBT.
     */
    public static ChronosWardenSpawnData load(CompoundTag tag, HolderLookup.Provider provider) {
        ChronosWardenSpawnData data = new ChronosWardenSpawnData();

        // Load spawned door positions
        ListTag doorsList = tag.getList("SpawnedDoors", Tag.TAG_COMPOUND);
        for (int i = 0; i < doorsList.size(); i++) {
            CompoundTag doorTag = doorsList.getCompound(i);
            BlockPos pos = BlockPos.of(doorTag.getLong("Pos"));
            data.spawnedDoors.add(pos);
        }

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

        return tag;
    }

    /**
     * Check if a door has already spawned a Chronos Warden.
     */
    public boolean hasDoorSpawned(BlockPos pos) {
        return spawnedDoors.contains(pos);
    }

    /**
     * Mark a door as having spawned a Chronos Warden.
     */
    public void markDoorSpawned(BlockPos pos) {
        spawnedDoors.add(pos);
        setDirty();
    }

    /**
     * Reset all spawn tracking (for testing/debugging).
     */
    public void reset() {
        spawnedDoors.clear();
        setDirty();
    }

    /**
     * Get the data name for this SavedData.
     */
    public static String getDataName() {
        return DATA_NAME;
    }
}
