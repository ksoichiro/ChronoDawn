package com.chronodawn.core.portal;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/** Shared state transition for an arrival portal that is about to be removed. */
public final class PortalArrivalHelper {
    private PortalArrivalHelper() {
    }

    /** Deactivate the exact arrival portal before its blocks are removed. */
    public static void deactivate(ServerLevel level, @Nullable BlockPos framePos, BlockPos portalPos) {
        if (framePos == null) {
            ChronoDawn.LOGGER.warn("Could not find arrival portal frame for portal block at {}", portalPos);
            return;
        }

        PortalStateMachine portal = PortalRegistry.getInstance().getPortalAt(level.dimension(), framePos);
        if (portal != null && portal.getCurrentState() == PortalState.ACTIVATED) {
            portal.deactivate();
        }
    }
}
