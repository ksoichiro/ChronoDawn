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
 * Saved data for Time Guardian spawning state.
 * Persists across server restarts to prevent duplicate spawning.
 */
public class TimeGuardianSpawnData extends SavedData {
    private static final String DATA_NAME = "time_guardian_spawns";

    private final Set<BlockPos> spawnedStructures = new HashSet<>();

    public TimeGuardianSpawnData() {
        super();
    }

    /**
     * Factory method for SavedData system.
     */
    public static Factory<TimeGuardianSpawnData> factory() {
        return new Factory<>(
            TimeGuardianSpawnData::new,
            TimeGuardianSpawnData::load,
            null
        );
    }

    /**
     * Load saved data from NBT.
     */
    public static TimeGuardianSpawnData load(CompoundTag tag, HolderLookup.Provider provider) {
        TimeGuardianSpawnData data = new TimeGuardianSpawnData();

        // Load spawned structure positions
        ListTag structuresList = tag.getList("SpawnedStructures", Tag.TAG_COMPOUND);
        for (int i = 0; i < structuresList.size(); i++) {
            CompoundTag structureTag = structuresList.getCompound(i);
            BlockPos pos = BlockPos.of(structureTag.getLong("Pos"));
            data.spawnedStructures.add(pos);
        }

        return data;
    }

    /**
     * Save data to NBT.
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // Save spawned structure positions
        ListTag structuresList = new ListTag();
        for (BlockPos pos : spawnedStructures) {
            CompoundTag structureTag = new CompoundTag();
            structureTag.putLong("Pos", pos.asLong());
            structuresList.add(structureTag);
        }
        tag.put("SpawnedStructures", structuresList);

        return tag;
    }

    /**
     * Check if a structure has already spawned a Time Guardian.
     */
    public boolean hasStructureSpawned(BlockPos pos) {
        return spawnedStructures.contains(pos);
    }

    /**
     * Mark a structure as having spawned a Time Guardian.
     */
    public void markStructureSpawned(BlockPos pos) {
        spawnedStructures.add(pos);
        setDirty();
    }

    /**
     * Reset all spawn tracking (for testing/debugging).
     */
    public void reset() {
        spawnedStructures.clear();
        setDirty();
    }

    /**
     * Get the data name for this SavedData.
     */
    public static String getDataName() {
        return DATA_NAME;
    }
}
