package com.chronodawn.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * Consolidated saved data for all boss spawning states.
 * Persists across server restarts to prevent duplicate spawning.
 *
 * Replaces individual spawn data classes:
 * - TimeTyrantSpawnData
 * - TimeGuardianSpawnData
 * - ChronosWardenSpawnData
 * - EntropyKeeperSpawnData
 * - ClockworkColossusSpawnData
 */
public class BossSpawnData extends SavedData {
    private static final String DATA_NAME = "boss_spawns";

    // Time Tyrant data
    private final Set<BlockPos> timeTyrantSpawnedDoors = new HashSet<>();
    private int timeTyrantSpawnCount = 0;

    // Time Guardian data
    private final Set<BlockPos> timeGuardianSpawnedStructures = new HashSet<>();

    // Chronos Warden data
    private final Set<BlockPos> chronosWardenSpawnedDoors = new HashSet<>();

    // Entropy Keeper data
    private final Set<BlockPos> entropyKeeperProcessedStructures = new HashSet<>();
    private final Set<BlockPos> entropyKeeperSpawnedMarkers = new HashSet<>();

    // Clockwork Colossus data
    private final Set<BlockPos> clockworkColossusSpawnedStructures = new HashSet<>();

    public BossSpawnData() {
        super();
    }

    /**
     * Factory method for SavedData system.
     */
    public static Factory<BossSpawnData> factory() {
        return new Factory<>(
            BossSpawnData::new,
            BossSpawnData::load,
            null
        );
    }

    /**
     * Load saved data from NBT.
     */
    public static BossSpawnData load(CompoundTag tag, HolderLookup.Provider provider) {
        BossSpawnData data = new BossSpawnData();

        // Load Time Tyrant data
        if (tag.contains("TimeTyrant")) {
            CompoundTag tyrantTag = tag.getCompound("TimeTyrant");
            ListTag doorsList = tyrantTag.getList("SpawnedDoors", Tag.TAG_COMPOUND);
            for (int i = 0; i < doorsList.size(); i++) {
                CompoundTag doorTag = doorsList.getCompound(i);
                BlockPos pos = BlockPos.of(doorTag.getLong("Pos"));
                data.timeTyrantSpawnedDoors.add(pos);
            }
            data.timeTyrantSpawnCount = tyrantTag.getInt("SpawnCount");
        }

        // Load Time Guardian data
        if (tag.contains("TimeGuardian")) {
            CompoundTag guardianTag = tag.getCompound("TimeGuardian");
            ListTag structuresList = guardianTag.getList("SpawnedStructures", Tag.TAG_COMPOUND);
            for (int i = 0; i < structuresList.size(); i++) {
                CompoundTag structureTag = structuresList.getCompound(i);
                BlockPos pos = BlockPos.of(structureTag.getLong("Pos"));
                data.timeGuardianSpawnedStructures.add(pos);
            }
        }

        // Load Chronos Warden data
        if (tag.contains("ChronosWarden")) {
            CompoundTag wardenTag = tag.getCompound("ChronosWarden");
            ListTag doorsList = wardenTag.getList("SpawnedDoors", Tag.TAG_COMPOUND);
            for (int i = 0; i < doorsList.size(); i++) {
                CompoundTag doorTag = doorsList.getCompound(i);
                BlockPos pos = BlockPos.of(doorTag.getLong("Pos"));
                data.chronosWardenSpawnedDoors.add(pos);
            }
        }

        // Load Entropy Keeper data
        if (tag.contains("EntropyKeeper")) {
            CompoundTag keeperTag = tag.getCompound("EntropyKeeper");

            ListTag processedList = keeperTag.getList("ProcessedStructures", Tag.TAG_COMPOUND);
            for (int i = 0; i < processedList.size(); i++) {
                CompoundTag structureTag = processedList.getCompound(i);
                BlockPos pos = BlockPos.of(structureTag.getLong("Pos"));
                data.entropyKeeperProcessedStructures.add(pos);
            }

            ListTag markersList = keeperTag.getList("SpawnedMarkers", Tag.TAG_COMPOUND);
            for (int i = 0; i < markersList.size(); i++) {
                CompoundTag markerTag = markersList.getCompound(i);
                BlockPos pos = BlockPos.of(markerTag.getLong("Pos"));
                data.entropyKeeperSpawnedMarkers.add(pos);
            }
        }

        // Load Clockwork Colossus data
        if (tag.contains("ClockworkColossus")) {
            CompoundTag colossusTag = tag.getCompound("ClockworkColossus");
            ListTag structuresList = colossusTag.getList("SpawnedStructures", Tag.TAG_COMPOUND);
            for (int i = 0; i < structuresList.size(); i++) {
                CompoundTag structureTag = structuresList.getCompound(i);
                BlockPos pos = BlockPos.of(structureTag.getLong("Pos"));
                data.clockworkColossusSpawnedStructures.add(pos);
            }
        }

        return data;
    }

    /**
     * Save data to NBT.
     */
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // Save Time Tyrant data
        CompoundTag tyrantTag = new CompoundTag();
        ListTag tyrantDoorsList = new ListTag();
        for (BlockPos pos : timeTyrantSpawnedDoors) {
            CompoundTag doorTag = new CompoundTag();
            doorTag.putLong("Pos", pos.asLong());
            tyrantDoorsList.add(doorTag);
        }
        tyrantTag.put("SpawnedDoors", tyrantDoorsList);
        tyrantTag.putInt("SpawnCount", timeTyrantSpawnCount);
        tag.put("TimeTyrant", tyrantTag);

        // Save Time Guardian data
        CompoundTag guardianTag = new CompoundTag();
        ListTag guardianStructuresList = new ListTag();
        for (BlockPos pos : timeGuardianSpawnedStructures) {
            CompoundTag structureTag = new CompoundTag();
            structureTag.putLong("Pos", pos.asLong());
            guardianStructuresList.add(structureTag);
        }
        guardianTag.put("SpawnedStructures", guardianStructuresList);
        tag.put("TimeGuardian", guardianTag);

        // Save Chronos Warden data
        CompoundTag wardenTag = new CompoundTag();
        ListTag wardenDoorsList = new ListTag();
        for (BlockPos pos : chronosWardenSpawnedDoors) {
            CompoundTag doorTag = new CompoundTag();
            doorTag.putLong("Pos", pos.asLong());
            wardenDoorsList.add(doorTag);
        }
        wardenTag.put("SpawnedDoors", wardenDoorsList);
        tag.put("ChronosWarden", wardenTag);

        // Save Entropy Keeper data
        CompoundTag keeperTag = new CompoundTag();

        ListTag processedList = new ListTag();
        for (BlockPos pos : entropyKeeperProcessedStructures) {
            CompoundTag structureTag = new CompoundTag();
            structureTag.putLong("Pos", pos.asLong());
            processedList.add(structureTag);
        }
        keeperTag.put("ProcessedStructures", processedList);

        ListTag markersList = new ListTag();
        for (BlockPos pos : entropyKeeperSpawnedMarkers) {
            CompoundTag markerTag = new CompoundTag();
            markerTag.putLong("Pos", pos.asLong());
            markersList.add(markerTag);
        }
        keeperTag.put("SpawnedMarkers", markersList);
        tag.put("EntropyKeeper", keeperTag);

        // Save Clockwork Colossus data
        CompoundTag colossusTag = new CompoundTag();
        ListTag colossusStructuresList = new ListTag();
        for (BlockPos pos : clockworkColossusSpawnedStructures) {
            CompoundTag structureTag = new CompoundTag();
            structureTag.putLong("Pos", pos.asLong());
            colossusStructuresList.add(structureTag);
        }
        colossusTag.put("SpawnedStructures", colossusStructuresList);
        tag.put("ClockworkColossus", colossusTag);

        return tag;
    }

    // ========================================
    // Time Tyrant methods
    // ========================================

    public boolean hasTimeTyrantDoorSpawned(BlockPos pos) {
        return timeTyrantSpawnedDoors.contains(pos);
    }

    public void markTimeTyrantDoorSpawned(BlockPos pos) {
        timeTyrantSpawnedDoors.add(pos);
        setDirty();
    }

    public int getTimeTyrantSpawnCount() {
        return timeTyrantSpawnCount;
    }

    public void incrementTimeTyrantSpawnCount() {
        timeTyrantSpawnCount++;
        setDirty();
    }

    // ========================================
    // Time Guardian methods
    // ========================================

    public boolean hasTimeGuardianStructureSpawned(BlockPos pos) {
        return timeGuardianSpawnedStructures.contains(pos);
    }

    public void markTimeGuardianStructureSpawned(BlockPos pos) {
        timeGuardianSpawnedStructures.add(pos);
        setDirty();
    }

    // ========================================
    // Chronos Warden methods
    // ========================================

    public boolean hasChronosWardenDoorSpawned(BlockPos pos) {
        return chronosWardenSpawnedDoors.contains(pos);
    }

    public void markChronosWardenDoorSpawned(BlockPos pos) {
        chronosWardenSpawnedDoors.add(pos);
        setDirty();
    }

    // ========================================
    // Entropy Keeper methods
    // ========================================

    public boolean isEntropyKeeperStructureProcessed(BlockPos pos) {
        return entropyKeeperProcessedStructures.contains(pos);
    }

    public void markEntropyKeeperStructureProcessed(BlockPos pos) {
        entropyKeeperProcessedStructures.add(pos);
        setDirty();
    }

    public boolean hasEntropyKeeperMarkerSpawned(BlockPos pos) {
        return entropyKeeperSpawnedMarkers.contains(pos);
    }

    public void markEntropyKeeperMarkerSpawned(BlockPos pos) {
        entropyKeeperSpawnedMarkers.add(pos);
        setDirty();
    }

    // ========================================
    // Clockwork Colossus methods
    // ========================================

    public boolean hasClockworkColossusStructureSpawned(BlockPos pos) {
        return clockworkColossusSpawnedStructures.contains(pos);
    }

    public void markClockworkColossusStructureSpawned(BlockPos pos) {
        clockworkColossusSpawnedStructures.add(pos);
        setDirty();
    }

    // ========================================
    // Reset methods (for testing/debugging)
    // ========================================

    public void resetTimeTyrant() {
        timeTyrantSpawnedDoors.clear();
        timeTyrantSpawnCount = 0;
        setDirty();
    }

    public void resetTimeGuardian() {
        timeGuardianSpawnedStructures.clear();
        setDirty();
    }

    public void resetChronosWarden() {
        chronosWardenSpawnedDoors.clear();
        setDirty();
    }

    public void resetEntropyKeeper() {
        entropyKeeperProcessedStructures.clear();
        entropyKeeperSpawnedMarkers.clear();
        setDirty();
    }

    public void resetClockworkColossus() {
        clockworkColossusSpawnedStructures.clear();
        setDirty();
    }

    public void resetAll() {
        resetTimeTyrant();
        resetTimeGuardian();
        resetChronosWarden();
        resetEntropyKeeper();
        resetClockworkColossus();
        setDirty();
    }

    /**
     * Get the data name for this SavedData.
     */
    public static String getDataName() {
        return DATA_NAME;
    }
}
