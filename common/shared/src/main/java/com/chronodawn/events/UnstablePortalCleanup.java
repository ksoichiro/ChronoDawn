package com.chronodawn.events;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Scheduling for the cleanup of portal blocks while portals are unstable.
 *
 * <p>The cleanup scans a 21x21x21 volume per registered portal. Running it every tick cost
 * about 9,000 block lookups per portal for as long as the gate stayed on, so it runs once per
 * interval instead. Portal blocks that reappear are removed at most one interval later.
 */
public final class UnstablePortalCleanup {
    /** Ticks between scans (1 second). */
    public static final int INTERVAL_TICKS = 20;

    /** Scan radius around each registered portal position. */
    public static final int RADIUS = 10;

    private UnstablePortalCleanup() {
    }

    /**
     * @param tickCount the server tick counter
     * @return true when the cleanup scan should run on this tick
     */
    public static boolean shouldScan(int tickCount) {
        return Math.floorMod(tickCount, INTERVAL_TICKS) == 0;
    }

    /**
     * Removes every Chrono Dawn portal block within {@link #RADIUS} of {@code center}.
     * Skips the scan when any chunk of the volume is unloaded, because getBlockState on an
     * unloaded chunk would force-load it.
     *
     * @return true when at least one portal block was removed
     */
    public static boolean removePortalBlocksNear(ServerLevel level, BlockPos center) {
        int r = RADIUS;
        if (!level.hasChunksAt(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            return false;
        }
        boolean removed = false;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    if (level.getBlockState(checkPos).is(ModBlocks.CHRONO_DAWN_PORTAL.get())) {
                        level.removeBlock(checkPos, false);
                        removed = true;
                    }
                }
            }
        }
        return removed;
    }
}
