package com.chronodawn.unit;

import com.chronodawn.events.UnstablePortalCleanup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The unstable-portal cleanup must still run, but only once per interval. */
public class UnstablePortalCleanupTest {

    @Test
    void scansExactlyOncePerInterval() {
        int scans = 0;
        for (int tick = 0; tick < UnstablePortalCleanup.INTERVAL_TICKS * 10; tick++) {
            if (UnstablePortalCleanup.shouldScan(tick)) {
                scans++;
            }
        }
        assertEquals(10, scans);
    }

    @Test
    void scansOnIntervalBoundaryOnly() {
        assertTrue(UnstablePortalCleanup.shouldScan(0));
        assertFalse(UnstablePortalCleanup.shouldScan(1));
        assertFalse(UnstablePortalCleanup.shouldScan(UnstablePortalCleanup.INTERVAL_TICKS - 1));
        assertTrue(UnstablePortalCleanup.shouldScan(UnstablePortalCleanup.INTERVAL_TICKS));
    }

    @Test
    void negativeTickCountDoesNotThrowOrSkipForever() {
        assertTrue(UnstablePortalCleanup.shouldScan(-UnstablePortalCleanup.INTERVAL_TICKS));
    }
}
