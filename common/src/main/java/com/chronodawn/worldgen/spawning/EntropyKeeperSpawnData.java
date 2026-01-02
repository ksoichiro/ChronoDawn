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
 * Saved data for Entropy Keeper spawning state.
 * Persists across server restarts to prevent duplicate spawning.
 */
public class EntropyKeeperSpawnData extends SavedData {
    private static final String DATA_NAME = "entropy_keeper_spawns";

    private final Set<BlockPos> processedStructures = new HashSet<>();
    private final Set<BlockPos> spawnedMarkers = new HashSet<>();

    public EntropyKeeperSpawnData() {
        super();
    }

    /**
     * Factory method for SavedData system.
     */
    public static Factory<EntropyKeeperSpawnData> factory() {
        return new Factory<>(
            EntropyKeeperSpawnData::new,
            EntropyKeeperSpawnData::load,
            null
        );
    }

    /**
     * Load saved data from NBT.
     */
    public static EntropyKeeperSpawnData load(CompoundTag tag, HolderLookup.Provider provider) {
        EntropyKeeperSpawnData data = new EntropyKeeperSpawnData();

        // Load processed structure positions
        ListTag structuresList = tag.getList("ProcessedStructures", Tag.TAG_COMPOUND);
        for (int i = 0; i < structuresList.size(); i++) {
            CompoundTag structureTag = structuresList.getCompound(i);
            BlockPos pos = BlockPos.of(structureTag.getLong("Pos"));
            data.processedStructures.add(pos);
        }

        // Load spawned marker positions
        ListTag markersList = tag.getList("SpawnedMarkers", Tag.TAG_COMPOUND);
        for (int i = 0; i < markersList.size(); i++) {
            CompoundTag markerTag = markersList.getCompound(i);
            BlockPos pos = BlockPos.of(markerTag.getLong("Pos"));
            data.spawnedMarkers.add(pos);
        }

        return data;
    }

    /**
     * Save data to NBT.
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // Save processed structure positions
        ListTag structuresList = new ListTag();
        for (BlockPos pos : processedStructures) {
            CompoundTag structureTag = new CompoundTag();
            structureTag.putLong("Pos", pos.asLong());
            structuresList.add(structureTag);
        }
        tag.put("ProcessedStructures", structuresList);

        // Save spawned marker positions
        ListTag markersList = new ListTag();
        for (BlockPos pos : spawnedMarkers) {
            CompoundTag markerTag = new CompoundTag();
            markerTag.putLong("Pos", pos.asLong());
            markersList.add(markerTag);
        }
        tag.put("SpawnedMarkers", markersList);

        return tag;
    }

    /**
     * Check if a structure has been processed.
     */
    public boolean isStructureProcessed(BlockPos pos) {
        return processedStructures.contains(pos);
    }

    /**
     * Mark a structure as processed.
     */
    public void markStructureProcessed(BlockPos pos) {
        processedStructures.add(pos);
        setDirty();
    }

    /**
     * Check if a marker has spawned an Entropy Keeper.
     */
    public boolean hasMarkerSpawned(BlockPos pos) {
        return spawnedMarkers.contains(pos);
    }

    /**
     * Mark a marker as having spawned an Entropy Keeper.
     */
    public void markMarkerSpawned(BlockPos pos) {
        spawnedMarkers.add(pos);
        setDirty();
    }

    /**
     * Reset all spawn tracking (for testing/debugging).
     */
    public void reset() {
        processedStructures.clear();
        spawnedMarkers.clear();
        setDirty();
    }

    /**
     * Get the data name for this SavedData.
     */
    public static String getDataName() {
        return DATA_NAME;
    }
}
