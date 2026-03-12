package com.chronodawn.unit;

import com.chronodawn.core.portal.PortalState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link PortalState} enum logic.
 * Validates state properties and transition predicates.
 */
class PortalStateTest {

    @Test
    void allowsTravel_onlyActivatedAndStabilized() {
        assertFalse(PortalState.INACTIVE.allowsTravel());
        assertTrue(PortalState.ACTIVATED.allowsTravel());
        assertFalse(PortalState.DEACTIVATED.allowsTravel());
        assertTrue(PortalState.STABILIZED.allowsTravel());
    }

    @Test
    void isBidirectional_onlyStabilized() {
        assertFalse(PortalState.INACTIVE.isBidirectional());
        assertFalse(PortalState.ACTIVATED.isBidirectional());
        assertFalse(PortalState.DEACTIVATED.isBidirectional());
        assertTrue(PortalState.STABILIZED.isBidirectional());
    }

    @Test
    void hasPortalBlocks_onlyActivatedAndStabilized() {
        assertFalse(PortalState.INACTIVE.hasPortalBlocks());
        assertTrue(PortalState.ACTIVATED.hasPortalBlocks());
        assertFalse(PortalState.DEACTIVATED.hasPortalBlocks());
        assertTrue(PortalState.STABILIZED.hasPortalBlocks());
    }

    @Test
    void canBeStabilized_onlyDeactivated() {
        assertFalse(PortalState.INACTIVE.canBeStabilized());
        assertFalse(PortalState.ACTIVATED.canBeStabilized());
        assertTrue(PortalState.DEACTIVATED.canBeStabilized());
        assertFalse(PortalState.STABILIZED.canBeStabilized());
    }

    @Test
    void allStatesExist() {
        assertEquals(4, PortalState.values().length);
    }

    @Test
    void allowsTravel_impliesHasPortalBlocks() {
        for (PortalState state : PortalState.values()) {
            if (state.allowsTravel()) {
                assertTrue(state.hasPortalBlocks(),
                    state + " allows travel but has no portal blocks");
            }
        }
    }

    @Test
    void bidirectional_impliesAllowsTravel() {
        for (PortalState state : PortalState.values()) {
            if (state.isBidirectional()) {
                assertTrue(state.allowsTravel(),
                    state + " is bidirectional but doesn't allow travel");
            }
        }
    }
}
