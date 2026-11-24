package com.chronodawn.core.portal;

/**
 * Portal State - Represents the state of a ChronoDawn portal.
 *
 * Typical transitions for a registry entry:
 * - INACTIVE → ACTIVATED on ignition or frame restoration
 * - ACTIVATED → DEACTIVATED at an unstable Chrono Dawn arrival
 * - DEACTIVATED → ACTIVATED when an existing destination frame is restored
 * - DEACTIVATED → STABILIZED using a Portal Stabilizer
 *
 * Igniting an empty frame creates a new ACTIVATED entry, replacing the position
 * index immediately even for a STABILIZED portal. The old entry remains in the registry and
 * saved data. Registry state does not guarantee that portal blocks are present.
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
     * Portal is activated. Portal blocks normally allow travel when present.
     * Under one-way entry, the unstable arrival portal is deactivated and its
     * blocks are removed. The source portal remains activated.
     */
    ACTIVATED,

    /**
     * An unstable arrival portal has been deactivated and its blocks removed.
     * Can be stabilized using Portal Stabilizer.
     */
    DEACTIVATED,

    /**
     * Portal is stabilized and allows bidirectional travel.
     * Portal blocks normally allow travel in both directions when present.
     * A new ignition at the same position can replace its position index.
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
