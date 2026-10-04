package com.chronodawn.data;

import com.chronodawn.compat.CompatSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consolidated saved data for all boss spawning states.
 * Persists across server restarts to prevent duplicate spawning.
 *
 * This is the 1.21.5-specific version with single-parameter load method.
 *
 * Replaces individual spawn data classes:
 * - TimeTyrantSpawnData
 * - TimeGuardianSpawnData
 * - ChronosWardenSpawnData
 * - EntropyKeeperSpawnData
 * - ClockworkColossusSpawnData
 */
public class BossSpawnData extends CompatSavedData {
    private static final String DATA_NAME = "boss_spawns";

    // Time Tyrant data
    private final Set<BlockPos> timeTyrantSpawnedDoors = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private int timeTyrantSpawnCount = 0;

    // Time Guardian data
    private final Set<BlockPos> timeGuardianSpawnedStructures = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Chronos Warden data
    private final Set<BlockPos> chronosWardenSpawnedDoors = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Entropy Keeper data
    private final Set<BlockPos> entropyKeeperProcessedStructures = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<BlockPos> entropyKeeperSpawnedMarkers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Clockwork Colossus data
    private final Set<BlockPos> clockworkColossusSpawnedStructures = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<EngineRoomBounds> clockworkColossusEngineRooms = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Temporal Phantom data
    // The catacombs placer removes its markers after placing the boss_room, so processed
    // structures must persist or a restart re-processes them and places a second room
    private final Set<BlockPos> temporalPhantomProcessedStructures = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<BlockPos> temporalPhantomBossRooms = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<BlockPos> temporalPhantomSpawnedRooms = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public BossSpawnData() {
        super();
    }

    public static String getDataName() {
        return DATA_NAME;
    }

    /**
     * Static load method for CompatSavedData.computeIfAbsent().
     * In 1.21.5, the load method no longer needs HolderLookup.Provider.
     */
    public static BossSpawnData load(CompoundTag tag) {
        BossSpawnData data = new BossSpawnData();
        data.loadData(tag);
        return data;
    }

    /**
     * Load saved data from NBT.
     * In 1.21.5, NBT getInt/getList/getCompound now return Optional or require *Or methods.
     */
    @Override
    public void loadData(CompoundTag tag) {
        // Load Time Tyrant data
        CompoundTag tyrantTag = tag.getCompoundOrEmpty("TimeTyrant");
        if (!tyrantTag.isEmpty()) {
            ListTag doorsList = tyrantTag.getListOrEmpty("SpawnedDoors");
            for (int i = 0; i < doorsList.size(); i++) {
                CompoundTag doorTag = doorsList.getCompound(i).orElse(new CompoundTag());
                BlockPos pos = BlockPos.of(doorTag.getLongOr("Pos", 0L));
                this.timeTyrantSpawnedDoors.add(pos);
            }
            this.timeTyrantSpawnCount = tyrantTag.getIntOr("SpawnCount", 0);
        }

        // Load Time Guardian data
        CompoundTag guardianTag = tag.getCompoundOrEmpty("TimeGuardian");
        if (!guardianTag.isEmpty()) {
            ListTag structuresList = guardianTag.getListOrEmpty("SpawnedStructures");
            for (int i = 0; i < structuresList.size(); i++) {
                CompoundTag structureTag = structuresList.getCompound(i).orElse(new CompoundTag());
                BlockPos pos = BlockPos.of(structureTag.getLongOr("Pos", 0L));
                this.timeGuardianSpawnedStructures.add(pos);
            }
        }

        // Load Chronos Warden data
        CompoundTag wardenTag = tag.getCompoundOrEmpty("ChronosWarden");
        if (!wardenTag.isEmpty()) {
            ListTag doorsList = wardenTag.getListOrEmpty("SpawnedDoors");
            for (int i = 0; i < doorsList.size(); i++) {
                CompoundTag doorTag = doorsList.getCompound(i).orElse(new CompoundTag());
                BlockPos pos = BlockPos.of(doorTag.getLongOr("Pos", 0L));
                this.chronosWardenSpawnedDoors.add(pos);
            }
        }

        // Load Entropy Keeper data
        CompoundTag keeperTag = tag.getCompoundOrEmpty("EntropyKeeper");
        if (!keeperTag.isEmpty()) {
            ListTag processedList = keeperTag.getListOrEmpty("ProcessedStructures");
            for (int i = 0; i < processedList.size(); i++) {
                CompoundTag structureTag = processedList.getCompound(i).orElse(new CompoundTag());
                BlockPos pos = BlockPos.of(structureTag.getLongOr("Pos", 0L));
                this.entropyKeeperProcessedStructures.add(pos);
            }

            ListTag markersList = keeperTag.getListOrEmpty("SpawnedMarkers");
            for (int i = 0; i < markersList.size(); i++) {
                CompoundTag markerTag = markersList.getCompound(i).orElse(new CompoundTag());
                BlockPos pos = BlockPos.of(markerTag.getLongOr("Pos", 0L));
                this.entropyKeeperSpawnedMarkers.add(pos);
            }
        }

        // Load Temporal Phantom data
        CompoundTag phantomTag = tag.getCompoundOrEmpty("TemporalPhantom");
        if (!phantomTag.isEmpty()) {
            ListTag processedStructuresList = phantomTag.getListOrEmpty("ProcessedStructures");
            for (int i = 0; i < processedStructuresList.size(); i++) {
                CompoundTag structureTag = processedStructuresList.getCompound(i).orElse(new CompoundTag());
                this.temporalPhantomProcessedStructures.add(BlockPos.of(structureTag.getLongOr("Pos", 0L)));
            }
            ListTag bossRoomsList = phantomTag.getListOrEmpty("BossRooms");
            for (int i = 0; i < bossRoomsList.size(); i++) {
                CompoundTag roomTag = bossRoomsList.getCompound(i).orElse(new CompoundTag());
                this.temporalPhantomBossRooms.add(BlockPos.of(roomTag.getLongOr("Pos", 0L)));
            }
            ListTag spawnedRoomsList = phantomTag.getListOrEmpty("SpawnedRooms");
            for (int i = 0; i < spawnedRoomsList.size(); i++) {
                CompoundTag roomTag = spawnedRoomsList.getCompound(i).orElse(new CompoundTag());
                this.temporalPhantomSpawnedRooms.add(BlockPos.of(roomTag.getLongOr("Pos", 0L)));
            }
        }

        // Load Clockwork Colossus data
        CompoundTag colossusTag = tag.getCompoundOrEmpty("ClockworkColossus");
        if (!colossusTag.isEmpty()) {
            ListTag structuresList = colossusTag.getListOrEmpty("SpawnedStructures");
            for (int i = 0; i < structuresList.size(); i++) {
                CompoundTag structureTag = structuresList.getCompound(i).orElse(new CompoundTag());
                BlockPos pos = BlockPos.of(structureTag.getLongOr("Pos", 0L));
                this.clockworkColossusSpawnedStructures.add(pos);
            }
            ListTag engineRoomsList = colossusTag.getListOrEmpty("EngineRooms");
            for (int i = 0; i < engineRoomsList.size(); i++) {
                CompoundTag roomTag = engineRoomsList.getCompound(i).orElse(new CompoundTag());
                this.clockworkColossusEngineRooms.add(new EngineRoomBounds(
                    roomTag.getIntOr("MinX", 0), roomTag.getIntOr("MinY", 0), roomTag.getIntOr("MinZ", 0),
                    roomTag.getIntOr("MaxX", 0), roomTag.getIntOr("MaxY", 0), roomTag.getIntOr("MaxZ", 0)
                ));
            }
        }
    }

    /**
     * Save data to NBT.
     */
    @Override
    public CompoundTag saveData(CompoundTag tag) {
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

        // Save Temporal Phantom data
        CompoundTag phantomTag = new CompoundTag();
        ListTag phantomProcessedStructuresList = new ListTag();
        for (BlockPos pos : temporalPhantomProcessedStructures) {
            CompoundTag structureTag = new CompoundTag();
            structureTag.putLong("Pos", pos.asLong());
            phantomProcessedStructuresList.add(structureTag);
        }
        phantomTag.put("ProcessedStructures", phantomProcessedStructuresList);
        ListTag phantomBossRoomsList = new ListTag();
        for (BlockPos pos : temporalPhantomBossRooms) {
            CompoundTag roomTag = new CompoundTag();
            roomTag.putLong("Pos", pos.asLong());
            phantomBossRoomsList.add(roomTag);
        }
        phantomTag.put("BossRooms", phantomBossRoomsList);
        ListTag phantomSpawnedRoomsList = new ListTag();
        for (BlockPos pos : temporalPhantomSpawnedRooms) {
            CompoundTag roomTag = new CompoundTag();
            roomTag.putLong("Pos", pos.asLong());
            phantomSpawnedRoomsList.add(roomTag);
        }
        phantomTag.put("SpawnedRooms", phantomSpawnedRoomsList);
        tag.put("TemporalPhantom", phantomTag);

        // Save Clockwork Colossus data
        CompoundTag colossusTag = new CompoundTag();
        ListTag colossusStructuresList = new ListTag();
        for (BlockPos pos : clockworkColossusSpawnedStructures) {
            CompoundTag structureTag = new CompoundTag();
            structureTag.putLong("Pos", pos.asLong());
            colossusStructuresList.add(structureTag);
        }
        colossusTag.put("SpawnedStructures", colossusStructuresList);
        ListTag engineRoomsList = new ListTag();
        for (EngineRoomBounds room : clockworkColossusEngineRooms) {
            CompoundTag roomTag = new CompoundTag();
            roomTag.putInt("MinX", room.minX());
            roomTag.putInt("MinY", room.minY());
            roomTag.putInt("MinZ", room.minZ());
            roomTag.putInt("MaxX", room.maxX());
            roomTag.putInt("MaxY", room.maxY());
            roomTag.putInt("MaxZ", room.maxZ());
            engineRoomsList.add(roomTag);
        }
        colossusTag.put("EngineRooms", engineRoomsList);
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

    public Set<EngineRoomBounds> getClockworkColossusEngineRooms() {
        return Collections.unmodifiableSet(clockworkColossusEngineRooms);
    }

    public void registerClockworkColossusEngineRoom(net.minecraft.world.level.levelgen.structure.BoundingBox room) {
        if (clockworkColossusEngineRooms.add(new EngineRoomBounds(
            room.minX(), room.minY(), room.minZ(), room.maxX(), room.maxY(), room.maxZ()
        ))) {
            setDirty();
        }
    }

    // ========================================
    // Temporal Phantom methods
    // ========================================

    public boolean isTemporalPhantomStructureProcessed(BlockPos pos) {
        return temporalPhantomProcessedStructures.contains(pos);
    }

    public void markTemporalPhantomStructureProcessed(BlockPos pos) {
        if (temporalPhantomProcessedStructures.add(pos.immutable())) {
            setDirty();
        }
    }

    public Set<BlockPos> getTemporalPhantomBossRooms() {
        return Collections.unmodifiableSet(temporalPhantomBossRooms);
    }

    public void registerTemporalPhantomBossRoom(BlockPos pos) {
        if (temporalPhantomBossRooms.add(pos.immutable())) {
            setDirty();
        }
    }

    public boolean hasTemporalPhantomRoomSpawned(BlockPos pos) {
        return temporalPhantomSpawnedRooms.contains(pos);
    }

    public void markTemporalPhantomRoomSpawned(BlockPos pos) {
        if (temporalPhantomSpawnedRooms.add(pos.immutable())) {
            setDirty();
        }
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
        clockworkColossusEngineRooms.clear();
        setDirty();
    }

    public record EngineRoomBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }

    public void resetTemporalPhantom() {
        temporalPhantomProcessedStructures.clear();
        temporalPhantomBossRooms.clear();
        temporalPhantomSpawnedRooms.clear();
        setDirty();
    }

    public void resetAll() {
        resetTimeTyrant();
        resetTimeGuardian();
        resetChronosWarden();
        resetEntropyKeeper();
        resetClockworkColossus();
        resetTemporalPhantom();
        setDirty();
    }
}
