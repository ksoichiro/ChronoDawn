package com.chronodawn.core.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

/** Shared safe terrain selection for automatically generated portal frames. */
public final class PortalPlacementHelper {
    private static final int PORTAL_WIDTH = 4;
    private static final int PORTAL_HEIGHT = 5;

    private PortalPlacementHelper() {
    }

    /**
     * Finds the nearest level, clear placement around the mapped destination.
     * The exact coordinates are always preferred.
     */
    @Nullable
    public static BlockPos findSafeFramePosition(
            ServerLevel level, BlockPos start, Direction.Axis axis, int searchRadius) {
        for (int radius = 0; radius <= searchRadius; radius++) {
            for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    if (Math.max(Math.abs(xOffset), Math.abs(zOffset)) != radius) {
                        continue;
                    }

                    BlockPos candidate = new BlockPos(start.getX() + xOffset, 0, start.getZ() + zOffset);
                    BlockPos framePos = validateFramePosition(level, candidate, axis);
                    if (framePos != null) {
                        return framePos;
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    private static BlockPos validateFramePosition(ServerLevel level, BlockPos candidate, Direction.Axis axis) {
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Integer frameY = null;

        for (int offset = 0; offset < PORTAL_WIDTH; offset++) {
            BlockPos surfacePos = candidate.relative(horizontal, offset);
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, surfacePos.getX(), surfacePos.getZ());
            BlockState topBlock = level.getBlockState(surfacePos.atY(surfaceY - 1));
            if (topBlock.isAir()) {
                return null;
            }

            int columnFrameY = topBlock.getFluidState().isEmpty() && !topBlock.canBeReplaced()
                ? surfaceY
                : surfaceY - 1;
            if (frameY == null) {
                frameY = columnFrameY;
            } else if (frameY != columnFrameY) {
                return null;
            }
        }

        for (int horizontalOffset = 0; horizontalOffset < PORTAL_WIDTH; horizontalOffset++) {
            for (int verticalOffset = 1; verticalOffset < PORTAL_HEIGHT; verticalOffset++) {
                BlockPos portalSpace = candidate.relative(horizontal, horizontalOffset).above(frameY + verticalOffset);
                if (!isClearPortalSpace(level.getBlockState(portalSpace))) {
                    return null;
                }
            }
        }

        return candidate.atY(frameY);
    }

    private static boolean isClearPortalSpace(BlockState state) {
        return (state.isAir() || state.canBeReplaced()) && state.getFluidState().isEmpty();
    }
}
