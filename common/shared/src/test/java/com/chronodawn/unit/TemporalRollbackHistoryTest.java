package com.chronodawn.unit;

import com.chronodawn.items.artifacts.TemporalRollbackHistory;
import com.chronodawn.items.artifacts.TemporalRollbackHistory.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Time Tyrant's Mail rewinds the wearer to where they stood about 3 seconds before a
 * fatal hit. The history must pick the oldest snapshot still inside that window, and
 * never one from another dimension.
 */
class TemporalRollbackHistoryTest {

    private static final String OVERWORLD = "overworld";
    private static final String CHRONO_DAWN = "chronodawn";

    private static Snapshot<String> snapshot(long time, String dimension, double x, float health) {
        return new Snapshot<>(time, dimension, x, 64.0, 0.0, health);
    }

    @Test
    void emptyHistoryHasNoRewindTarget() {
        TemporalRollbackHistory<String> history = new TemporalRollbackHistory<>(60);
        assertTrue(history.rewindTarget(100, OVERWORLD).isEmpty());
    }

    @Test
    void picksOldestSnapshotInsideWindow() {
        TemporalRollbackHistory<String> history = new TemporalRollbackHistory<>(60);
        for (long t = 0; t <= 200; t += 5) {
            history.record(snapshot(t, OVERWORLD, t, 20.0f - t / 20.0f));
        }

        Optional<Snapshot<String>> target = history.rewindTarget(200, OVERWORLD);

        assertTrue(target.isPresent());
        assertEquals(140, target.get().gameTime());
        assertEquals(140.0, target.get().x());
        assertEquals(13.0f, target.get().health());
    }

    @Test
    void dropsSnapshotsOlderThanWindow() {
        TemporalRollbackHistory<String> history = new TemporalRollbackHistory<>(60);
        history.record(snapshot(0, OVERWORLD, 0, 20.0f));
        history.record(snapshot(10, OVERWORLD, 10, 20.0f));

        assertTrue(history.rewindTarget(100, OVERWORLD).isEmpty());
    }

    @Test
    void skipsSnapshotsFromOtherDimensions() {
        TemporalRollbackHistory<String> history = new TemporalRollbackHistory<>(60);
        history.record(snapshot(150, OVERWORLD, 1, 20.0f));
        history.record(snapshot(160, OVERWORLD, 2, 20.0f));
        history.record(snapshot(170, CHRONO_DAWN, 3, 18.0f));
        history.record(snapshot(180, CHRONO_DAWN, 4, 16.0f));

        Optional<Snapshot<String>> target = history.rewindTarget(200, CHRONO_DAWN);

        assertTrue(target.isPresent());
        assertEquals(170, target.get().gameTime());
        assertEquals(3.0, target.get().x());
    }

    @Test
    void clearRemovesAllSnapshots() {
        TemporalRollbackHistory<String> history = new TemporalRollbackHistory<>(60);
        history.record(snapshot(190, OVERWORLD, 1, 20.0f));
        history.clear();

        assertTrue(history.rewindTarget(200, OVERWORLD).isEmpty());
    }
}
