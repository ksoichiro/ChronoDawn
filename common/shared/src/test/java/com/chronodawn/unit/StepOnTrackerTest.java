package com.chronodawn.unit;

import com.chronodawn.core.wayside.StepOnTracker;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StepOnTrackerTest {

    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void firstStepIsNewVisit() {
        StepOnTracker tracker = new StepOnTracker();
        assertTrue(tracker.step(ALICE, 1000));
    }

    @Test
    void standingStillNeverRetriggers() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 1000);
        for (long t = 1001; t <= 1000 + 20 * 60 * 5; t++) {
            assertFalse(tracker.step(ALICE, t), "retriggered at tick " + t);
        }
    }

    @Test
    void shortGapIsStillTheSameVisit() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 1000);
        assertFalse(tracker.step(ALICE, 1000 + StepOnTracker.STEP_OFF_TICKS));
    }

    @Test
    void steppingBackOnAfterLeavingIsNewVisit() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 1000);
        assertTrue(tracker.step(ALICE, 1000 + StepOnTracker.STEP_OFF_TICKS + 1));
    }

    @Test
    void playersAreIndependent() {
        StepOnTracker tracker = new StepOnTracker();
        assertTrue(tracker.step(ALICE, 1000));
        assertTrue(tracker.step(BOB, 1001));
        assertFalse(tracker.step(ALICE, 1002));
        assertFalse(tracker.step(BOB, 1002));
    }

    @Test
    void staleEntriesArePruned() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 1000);
        tracker.step(BOB, 2000);
        assertEquals(1, tracker.size());
    }

    @Test
    void timeRunningBackwardsCountsAsNewVisit() {
        StepOnTracker tracker = new StepOnTracker();
        tracker.step(ALICE, 5000);
        assertTrue(tracker.step(ALICE, 100));
    }
}
