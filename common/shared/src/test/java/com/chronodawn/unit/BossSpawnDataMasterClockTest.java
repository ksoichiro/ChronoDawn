package com.chronodawn.unit;

import com.chronodawn.data.BossSpawnData;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Master Clock processed structures must survive a save/load round trip.
 *
 * The Master Clock placer removes its surface Dropper marker before placing the stairs,
 * corridor and boss_room. If the processed structures are lost on restart, the placer
 * re-scans the structure, finds no marker, and logs a warning after a full-height scan.
 */
class BossSpawnDataMasterClockTest {

    private static final BlockPos STRUCTURE_A = new BlockPos(1000, 64, -2000);
    private static final BlockPos STRUCTURE_B = new BlockPos(-3000, 70, 4000);

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
        data.markMasterClockStructureProcessed(STRUCTURE_A);

        BossSpawnData loaded = roundTrip(data);

        assertTrue(loaded.isMasterClockStructureProcessed(STRUCTURE_A));
        assertFalse(loaded.isMasterClockStructureProcessed(STRUCTURE_B));
    }

    @Test
    void dataSavedBeforeMasterClockTrackingLoadsEmpty() {
        BossSpawnData data = new BossSpawnData();
        data.markEntropyKeeperStructureProcessed(STRUCTURE_A);
        CompoundTag tag = data.saveData(new CompoundTag());
        tag.remove("MasterClock");

        BossSpawnData loaded = new BossSpawnData();
        loaded.loadData(tag);

        assertFalse(loaded.isMasterClockStructureProcessed(STRUCTURE_A));
        assertTrue(loaded.isEntropyKeeperStructureProcessed(STRUCTURE_A));
    }

    @Test
    void resetMasterClockClearsProcessedStructures() {
        BossSpawnData data = new BossSpawnData();
        data.markMasterClockStructureProcessed(STRUCTURE_A);
        data.resetMasterClock();

        assertFalse(data.isMasterClockStructureProcessed(STRUCTURE_A));
    }
}
