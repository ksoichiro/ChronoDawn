package com.chronodawn.unit;

import com.chronodawn.data.BossSpawnData;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Temporal Phantom spawn state must survive a save/load round trip.
 *
 * The Phantom Catacombs placer removes its Crying Obsidian markers once the boss_room is
 * placed. If the processed structures and registered boss_rooms are lost on restart, the
 * placer re-processes the catacombs, finds no markers, and places a second boss_room
 * while the original one never spawns the Phantom.
 */
class BossSpawnDataTemporalPhantomTest {

    private static final BlockPos STRUCTURE_A = new BlockPos(1000, 20, -2000);
    private static final BlockPos STRUCTURE_B = new BlockPos(-3000, 15, 4000);
    private static final BlockPos ROOM_A = new BlockPos(1050, 24, -1950);
    private static final BlockPos ROOM_B = new BlockPos(-2950, 19, 4060);

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static BossSpawnData roundTrip(BossSpawnData data) {
        CompoundTag tag = data.saveData(new CompoundTag());
        BossSpawnData loaded = new BossSpawnData();
        loaded.loadData(tag);
        return loaded;
    }

    @Test
    void processedStructuresSurviveRoundTrip() {
        BossSpawnData data = new BossSpawnData();
        data.markTemporalPhantomStructureProcessed(STRUCTURE_A);
        data.markTemporalPhantomStructureProcessed(STRUCTURE_B);

        BossSpawnData loaded = roundTrip(data);

        assertTrue(loaded.isTemporalPhantomStructureProcessed(STRUCTURE_A));
        assertTrue(loaded.isTemporalPhantomStructureProcessed(STRUCTURE_B));
        assertFalse(loaded.isTemporalPhantomStructureProcessed(ROOM_A));
    }

    @Test
    void bossRoomsAndSpawnStateSurviveRoundTrip() {
        BossSpawnData data = new BossSpawnData();
        data.registerTemporalPhantomBossRoom(ROOM_A);
        data.registerTemporalPhantomBossRoom(ROOM_B);
        data.markTemporalPhantomRoomSpawned(ROOM_A);

        BossSpawnData loaded = roundTrip(data);

        assertEquals(Set.of(ROOM_A, ROOM_B), loaded.getTemporalPhantomBossRooms());
        assertTrue(loaded.hasTemporalPhantomRoomSpawned(ROOM_A));
        assertFalse(loaded.hasTemporalPhantomRoomSpawned(ROOM_B));
    }

    @Test
    void dataSavedBeforeTemporalPhantomTrackingLoadsEmpty() {
        BossSpawnData data = new BossSpawnData();
        data.markEntropyKeeperStructureProcessed(STRUCTURE_A);
        CompoundTag tag = data.saveData(new CompoundTag());
        tag.remove("TemporalPhantom");

        BossSpawnData loaded = new BossSpawnData();
        loaded.loadData(tag);

        assertTrue(loaded.getTemporalPhantomBossRooms().isEmpty());
        assertFalse(loaded.isTemporalPhantomStructureProcessed(STRUCTURE_A));
        assertTrue(loaded.isEntropyKeeperStructureProcessed(STRUCTURE_A));
    }
}
