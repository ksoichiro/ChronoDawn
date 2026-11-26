package com.chronodawn.core.portal;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Portal State Machine - Manages portal state transitions.
 *
 * The transition methods enforce their individual preconditions. Direct state
 * assignment is also used for loading and restoring an existing portal.
 *
 * Typical transitions for an entry:
 * - INACTIVE → ACTIVATED: ignition or frame restoration
 * - ACTIVATED → DEACTIVATED: unstable arrival in Chrono Dawn
 * - DEACTIVATED → ACTIVATED: restoration of an existing destination frame
 * - DEACTIVATED → STABILIZED: use of a Portal Stabilizer
 *
 * Igniting an empty frame reuses the existing entry at that position when it is
 * INACTIVE, DEACTIVATED or ACTIVATED, so no duplicate is registered. Only when a
 * STABILIZED entry exists does ignition register a new ACTIVATED entry. The position
 * index then points to the new entry, while the old entry remains in the registry and
 * saved data. This is replacement, not a transition of the old entry.
 * Portal blocks and registry state can diverge when a frame is broken.
 *
 * Reference: data-model.md (Portal System → Portal States)
 * Task: T046 [US1] Implement portal state machine
 */
public class PortalStateMachine {
    private final UUID portalId;
    private final ResourceKey<Level> sourceDimension;
    private final BlockPos position;
    private PortalState currentState;
    private boolean legacyCounterpartPending;

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
        // Notify registry to persist state change
        PortalRegistry.getInstance().markDirtyForPortal(portalId);
        ChronoDawn.LOGGER.debug("Portal {} activated at {} in dimension {}",
            portalId, position, sourceDimension.identifier());
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
        // Notify registry to persist state change
        PortalRegistry.getInstance().markDirtyForPortal(portalId);
        ChronoDawn.LOGGER.debug("Portal {} deactivated at {} in dimension {}",
            portalId, position, sourceDimension.identifier());
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
        legacyCounterpartPending = false;
        // Notify registry to persist state change
        PortalRegistry.getInstance().markDirtyForPortal(portalId);
        ChronoDawn.LOGGER.debug("Portal {} stabilized at {} in dimension {}",
            portalId, position, sourceDimension.identifier());
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
     * Set portal state directly for loading or restoring an existing frame.
     *
     * @param state State to set
     */
    public void setState(PortalState state) {
        this.currentState = state;
    }

    /**
     * Whether this portal comes from a world saved before registry data was versioned and
     * may lack a registered counterpart in the other dimension. See
     * {@link PortalRegistry#findLegacyPortalNear}.
     *
     * @return true if the legacy fallback may still apply to this portal
     */
    public boolean isLegacyCounterpartPending() {
        return legacyCounterpartPending;
    }

    public void setLegacyCounterpartPending(boolean legacyCounterpartPending) {
        this.legacyCounterpartPending = legacyCounterpartPending;
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
