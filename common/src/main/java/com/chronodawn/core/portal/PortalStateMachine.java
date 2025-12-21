package com.chronodawn.core.portal;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Portal State Machine - Manages portal state transitions.
 *
 * This class handles all state transitions for ChronoDawn portals and enforces
 * the rules for valid state changes.
 *
 * State Transition Rules:
 * - INACTIVE → ACTIVATED: Requires Time Hourglass ignition
 * - ACTIVATED → DEACTIVATED: Automatic when player enters portal
 * - DEACTIVATED → STABILIZED: Requires Portal Stabilizer
 * - STABILIZED → STABILIZED: Permanent state (unless portal is destroyed)
 *
 * Invalid Transitions:
 * - Cannot go from ACTIVATED directly to STABILIZED (must deactivate first)
 * - Cannot go from STABILIZED back to any other state
 * - Cannot go from DEACTIVATED to ACTIVATED (must stabilize or rebuild)
 *
 * Reference: data-model.md (Portal System → Portal States)
 * Task: T046 [US1] Implement portal state machine
 */
public class PortalStateMachine {
    private final UUID portalId;
    private final ResourceKey<Level> sourceDimension;
    private final BlockPos position;
    private PortalState currentState;

    /**
     * Create a new portal state machine.
     *
     * @param portalId Unique portal identifier
     * @param sourceDimension Dimension where portal is located
     * @param position Portal position (bottom-left corner)
     */
    public PortalStateMachine(UUID portalId, ResourceKey<Level> sourceDimension, BlockPos position) {
        this.portalId = portalId;
        this.sourceDimension = sourceDimension;
        this.position = position;
        this.currentState = PortalState.INACTIVE;
    }

    /**
     * Activate the portal (ignite with Time Hourglass).
     *
     * @return true if activation succeeded
     */
    public boolean activate() {
        if (currentState != PortalState.INACTIVE) {
            ChronoDawn.LOGGER.warn("Cannot activate portal {} - current state is {}", portalId, currentState);
            return false;
        }

        currentState = PortalState.ACTIVATED;
        ChronoDawn.LOGGER.info("Portal {} activated at {} in dimension {}",
            portalId, position, sourceDimension.location());
        return true;
    }

    /**
     * Deactivate the portal (after player entry).
     *
     * @return true if deactivation succeeded
     */
    public boolean deactivate() {
        if (currentState != PortalState.ACTIVATED) {
            ChronoDawn.LOGGER.warn("Cannot deactivate portal {} - current state is {}", portalId, currentState);
            return false;
        }

        currentState = PortalState.DEACTIVATED;
        ChronoDawn.LOGGER.info("Portal {} deactivated at {} in dimension {}",
            portalId, position, sourceDimension.location());
        return true;
    }

    /**
     * Stabilize the portal (use Portal Stabilizer).
     *
     * @return true if stabilization succeeded
     */
    public boolean stabilize() {
        if (!currentState.canBeStabilized()) {
            ChronoDawn.LOGGER.warn("Cannot stabilize portal {} - current state is {}", portalId, currentState);
            return false;
        }

        currentState = PortalState.STABILIZED;
        ChronoDawn.LOGGER.info("Portal {} stabilized at {} in dimension {}",
            portalId, position, sourceDimension.location());
        return true;
    }

    /**
     * Get current portal state.
     *
     * @return Current state
     */
    public PortalState getCurrentState() {
        return currentState;
    }

    /**
     * Set portal state directly (for loading from data).
     *
     * @param state State to set
     */
    public void setState(PortalState state) {
        this.currentState = state;
    }

    /**
     * Get portal ID.
     *
     * @return Portal UUID
     */
    public UUID getPortalId() {
        return portalId;
    }

    /**
     * Get source dimension.
     *
     * @return Dimension key
     */
    public ResourceKey<Level> getSourceDimension() {
        return sourceDimension;
    }

    /**
     * Get portal position.
     *
     * @return Bottom-left corner position
     */
    public BlockPos getPosition() {
        return position;
    }

    /**
     * Check if portal allows travel.
     *
     * @return true if travel is allowed
     */
    public boolean allowsTravel() {
        return currentState.allowsTravel();
    }

    /**
     * Check if portal allows bidirectional travel.
     *
     * @return true if bidirectional travel is allowed
     */
    public boolean isBidirectional() {
        return currentState.isBidirectional();
    }

    /**
     * Check if portal blocks should be present.
     *
     * @return true if portal blocks should exist
     */
    public boolean hasPortalBlocks() {
        return currentState.hasPortalBlocks();
    }
}
