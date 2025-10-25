package com.chronosphere.core.portal;

/**
 * Portal State - Represents the state of a Chronosphere portal.
 *
 * Portal State Transitions:
 * 1. INACTIVE → ACTIVATED (ignite with Time Hourglass)
 * 2. ACTIVATED → DEACTIVATED (player enters portal to Chronosphere)
 * 3. DEACTIVATED → STABILIZED (use Portal Stabilizer)
 * 4. STABILIZED → STABILIZED (remains stable, bidirectional travel)
 *
 * State Properties:
 * - INACTIVE: Frame exists, no portal blocks, cannot travel
 * - ACTIVATED: Portal blocks present, one-way travel to Chronosphere
 * - DEACTIVATED: Portal blocks removed, cannot travel (after first use)
 * - STABILIZED: Portal blocks present, bidirectional travel enabled
 *
 * Reference: data-model.md (Portal System → Portal States)
 * Task: T046 [US1] Portal State Machine
 */
public enum PortalState {
    /**
     * Portal frame exists but portal is not activated.
     * No portal blocks, no travel possible.
     */
    INACTIVE,

    /**
     * Portal is activated and ready for one-way travel to Chronosphere.
     * Portal blocks present, player can enter to travel to Chronosphere.
     * Automatically deactivates after player entry.
     */
    ACTIVATED,

    /**
     * Portal has been used and is now deactivated.
     * Portal blocks removed, no travel possible.
     * Can be stabilized using Portal Stabilizer.
     */
    DEACTIVATED,

    /**
     * Portal is stabilized and allows bidirectional travel.
     * Portal blocks present, travel works in both directions.
     * Remains in this state permanently unless destroyed.
     */
    STABILIZED;

    /**
     * Check if this state allows travel.
     *
     * @return true if travel is allowed
     */
    public boolean allowsTravel() {
        return this == ACTIVATED || this == STABILIZED;
    }

    /**
     * Check if this state allows bidirectional travel.
     *
     * @return true if bidirectional travel is allowed
     */
    public boolean isBidirectional() {
        return this == STABILIZED;
    }

    /**
     * Check if portal blocks should be present in this state.
     *
     * @return true if portal blocks should exist
     */
    public boolean hasPortalBlocks() {
        return this == ACTIVATED || this == STABILIZED;
    }

    /**
     * Check if this state can be stabilized.
     *
     * @return true if Portal Stabilizer can be used
     */
    public boolean canBeStabilized() {
        return this == DEACTIVATED;
    }
}
