package com.chronodawn.unit;

import com.chronodawn.data.ChronoDawnGlobalState;
import com.chronodawn.data.ChronoDawnTimeData;
import com.chronodawn.data.DimensionStateData;
import com.chronodawn.data.DimensionStateData.TimeDistortionLevel;
import com.chronodawn.data.PlayerProgressData;
import com.chronodawn.data.TimeKeeperVillageData;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Save/reload round trips, missing-key defaults and state transitions for the saved-data classes.
 *
 * Only the version-independent saveData()/loadData() pair and the public accessors are used, so the
 * test compiles against every version's copy of these classes. The get(ServerLevel) factories and
 * the actual disk write need a live server and are left to GameTest and upgradeTest.
 */
class SavedDataPersistenceTest {
    private static final UUID PLAYER_A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID PLAYER_B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    @BeforeAll
    static void initialize() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    // --- ChronoDawnTimeData ---

    @Test
    void timeDataRoundTrip() {
        ChronoDawnTimeData original = new ChronoDawnTimeData();
        original.setIndependentTime(123_456L);

        ChronoDawnTimeData reloaded = new ChronoDawnTimeData();
        reloaded.loadData(original.saveData(new CompoundTag()));

        assertEquals(123_456L, reloaded.getIndependentTime());
    }

    @Test
    void timeDataDefaultsToZeroWhenKeyMissing() {
        ChronoDawnTimeData data = new ChronoDawnTimeData();
        data.loadData(new CompoundTag());

        assertEquals(0L, data.getIndependentTime());
    }

    @Test
    void timeDataSetterMarksDirty() {
        ChronoDawnTimeData data = new ChronoDawnTimeData();
        assertFalse(data.isDirty());

        data.setIndependentTime(5L);

        assertTrue(data.isDirty());
    }

    // --- ChronoDawnGlobalState ---

    @Test
    void globalStateDefaultsAreAllFalse() {
        ChronoDawnGlobalState state = new ChronoDawnGlobalState();
        state.loadData(new CompoundTag());

        assertFalse(state.hasEnteredChronoDawn());
        assertFalse(state.isPortalStabilized());
        assertFalse(state.isTyrantDefeated());
        assertFalse(state.arePortalsUnstable());
    }

    @Test
    void globalStateRoundTripKeepsEveryFlag() {
        ChronoDawnGlobalState original = new ChronoDawnGlobalState();
        original.markChronoDawnEntered();
        original.markPortalStabilized();
        original.markTyrantDefeated();

        ChronoDawnGlobalState reloaded = new ChronoDawnGlobalState();
        reloaded.loadData(original.saveData(new CompoundTag()));

        assertTrue(reloaded.hasEnteredChronoDawn());
        assertTrue(reloaded.isPortalStabilized());
        assertTrue(reloaded.isTyrantDefeated());
    }

    @Test
    void firstEntryMakesPortalsUnstableUntilStabilized() {
        ChronoDawnGlobalState state = new ChronoDawnGlobalState();
        assertFalse(state.arePortalsUnstable());

        state.markChronoDawnEntered();
        assertTrue(state.arePortalsUnstable());

        state.markPortalStabilized();
        assertFalse(state.arePortalsUnstable());
    }

    @Test
    void secondEntryDoesNotResetStabilization() {
        ChronoDawnGlobalState state = new ChronoDawnGlobalState();
        state.markChronoDawnEntered();
        state.markPortalStabilized();

        state.markChronoDawnEntered();

        assertTrue(state.isPortalStabilized());
        assertFalse(state.arePortalsUnstable());
    }

    // --- TimeKeeperVillageData ---

    @Test
    void villageDataRoundTripKeepsPositionIncludingNegatives() {
        TimeKeeperVillageData original = new TimeKeeperVillageData();
        original.setPlaced(new BlockPos(-120, 64, 340));

        TimeKeeperVillageData reloaded = new TimeKeeperVillageData();
        reloaded.loadData(original.saveData(new CompoundTag()));

        assertTrue(reloaded.isPlaced());
        assertEquals(new BlockPos(-120, 64, 340), reloaded.getPosition());
    }

    @Test
    void villageDataDefaultsWhenKeysMissing() {
        TimeKeeperVillageData data = new TimeKeeperVillageData();
        data.loadData(new CompoundTag());

        assertFalse(data.isPlaced());
        assertEquals(BlockPos.ZERO, data.getPosition());
    }

    @Test
    void villageDataStoresImmutablePosition() {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(1, 2, 3);
        TimeKeeperVillageData data = new TimeKeeperVillageData();
        data.setPlaced(mutable);

        mutable.set(9, 9, 9);

        assertEquals(new BlockPos(1, 2, 3), data.getPosition());
    }

    // --- DimensionStateData ---

    @Test
    void dimensionStateDefaults() {
        DimensionStateData data = new DimensionStateData();

        assertFalse(data.isStabilized());
        assertEquals(TimeDistortionLevel.SLOWNESS_IV, data.getTimeDistortionLevel());
    }

    @Test
    void dimensionStateRoundTrip() {
        DimensionStateData original = new DimensionStateData();
        original.enhanceTimeDistortion();

        DimensionStateData reloaded = new DimensionStateData();
        reloaded.loadData(original.saveData(new CompoundTag()));

        assertTrue(reloaded.isStabilized());
        assertEquals(TimeDistortionLevel.SLOWNESS_V, reloaded.getTimeDistortionLevel());
    }

    @Test
    void dimensionStateFallsBackToDefaultLevelWhenKeyMissing() {
        DimensionStateData data = new DimensionStateData();
        data.setTimeDistortionLevel(TimeDistortionLevel.SLOWNESS_V);

        CompoundTag oldFormat = new CompoundTag();
        oldFormat.putBoolean("is_stabilized", true);
        data.loadData(oldFormat);

        assertTrue(data.isStabilized());
        assertEquals(TimeDistortionLevel.SLOWNESS_IV, data.getTimeDistortionLevel());
    }

    @Test
    void dimensionStateSettersMarkDirtyOnlyOnChange() {
        DimensionStateData data = new DimensionStateData();

        data.setStabilized(false);
        data.setTimeDistortionLevel(TimeDistortionLevel.SLOWNESS_IV);
        assertFalse(data.isDirty());

        data.setStabilized(true);
        assertTrue(data.isDirty());
    }

    @Test
    void timeDistortionAmplifiersMatchLevelNames() {
        assertEquals(4, TimeDistortionLevel.SLOWNESS_IV.getAmplifier());
        assertEquals(5, TimeDistortionLevel.SLOWNESS_V.getAmplifier());
    }

    // --- PlayerProgressData ---

    @Test
    void playerProgressDefaultsToZeroForUnknownPlayer() {
        PlayerProgressData data = new PlayerProgressData();

        assertEquals(0L, data.getShieldSpeedCooldownEnd(PLAYER_A));
        assertEquals(0L, data.getShieldEchoActiveUntil(PLAYER_A));
        assertEquals(0L, data.getShieldEchoCooldownEnd(PLAYER_A));
    }

    @Test
    void playerProgressRoundTripKeepsPlayersSeparate() {
        PlayerProgressData original = new PlayerProgressData();
        original.setShieldSpeedCooldownEnd(PLAYER_A, 100L);
        original.setShieldEchoActiveUntil(PLAYER_A, 200L);
        original.setShieldEchoCooldownEnd(PLAYER_A, 300L);
        original.setShieldSpeedCooldownEnd(PLAYER_B, 999L);

        PlayerProgressData reloaded = new PlayerProgressData();
        reloaded.loadData(original.saveData(new CompoundTag()));

        assertEquals(100L, reloaded.getShieldSpeedCooldownEnd(PLAYER_A));
        assertEquals(200L, reloaded.getShieldEchoActiveUntil(PLAYER_A));
        assertEquals(300L, reloaded.getShieldEchoCooldownEnd(PLAYER_A));
        assertEquals(999L, reloaded.getShieldSpeedCooldownEnd(PLAYER_B));
        assertEquals(0L, reloaded.getShieldEchoCooldownEnd(PLAYER_B));
    }

    @Test
    void playerProgressMissingFieldsLoadAsZero() {
        CompoundTag player = new CompoundTag();
        player.putLong("shield_speed_cd", 50L);
        CompoundTag players = new CompoundTag();
        players.put(PLAYER_A.toString(), player);
        CompoundTag tag = new CompoundTag();
        tag.put("players", players);

        PlayerProgressData data = new PlayerProgressData();
        data.loadData(tag);

        assertEquals(50L, data.getShieldSpeedCooldownEnd(PLAYER_A));
        assertEquals(0L, data.getShieldEchoActiveUntil(PLAYER_A));
        assertEquals(0L, data.getShieldEchoCooldownEnd(PLAYER_A));
    }

    @Test
    void playerProgressEmptyTagLoadsNoPlayers() {
        PlayerProgressData data = new PlayerProgressData();
        data.loadData(new CompoundTag());

        assertEquals(0L, data.getShieldSpeedCooldownEnd(PLAYER_A));
    }

    @Test
    void playerProgressSetterMarksDirty() {
        PlayerProgressData data = new PlayerProgressData();
        assertFalse(data.isDirty());

        data.setShieldEchoCooldownEnd(PLAYER_A, 1L);

        assertTrue(data.isDirty());
    }
}
