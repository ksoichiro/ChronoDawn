package com.chronodawn.core.portal;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Portal Frame Validator - Validates ChronoDawn portal frame structure.
 *
 * This class validates whether a portal frame is correctly constructed according to
 * the ChronoDawn portal specifications.
 *
 * Portal Frame Requirements:
 * - Frame block: Clockstone Block
 * - Minimum size: 4x5 (width x height)
 * - Maximum size: 23x23 (width x height)
 * - Must form a complete rectangular frame
 * - All edges must be Clockstone Blocks (corners are optional, like Nether Portal)
 * - Interior must be air blocks (or will be filled with portal blocks)
 *
 * Validation Process:
 * 1. Find the frame's width and height
 * 2. Verify all edge blocks are Clockstone Blocks (corners optional)
 * 3. Verify interior is air or portal blocks
 * 4. Verify size is within valid range
 *
 * Reference: data-model.md (Portal System → Portal Frame)
 * Task: T045 [US1] Create portal frame validation logic
 */
public class PortalFrameValidator {
    /**
     * Minimum portal width (including frame).
     */
    public static final int MIN_WIDTH = 4;

    /**
     * Minimum portal height (including frame).
     */
    public static final int MIN_HEIGHT = 5;

    /**
     * Maximum portal width (including frame).
     */
    public static final int MAX_WIDTH = 23;

    /**
     * Maximum portal height (including frame).
     */
    public static final int MAX_HEIGHT = 23;

    /**
     * Validate a portal frame starting from a given position.
     * The position can be any frame block (not necessarily bottom-left corner).
     *
     * @param level The level containing the portal
     * @param pos Starting position (any frame block)
     * @param axis Portal axis (X or Z)
     * @return PortalFrameData if valid, null if invalid
     */
    public static PortalFrameData validateFrame(Level level, BlockPos pos, Direction.Axis axis) {
        // First check if starting position is a frame block
        if (!isFrameBlock(level, pos)) {
            return null;
        }

        // Determine the horizontal and vertical directions based on axis
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction vertical = Direction.UP;

        // Find the rectangular bounds by exploring in all directions
        BlockPos bottomLeft = findBottomLeftCorner(level, pos, horizontal, vertical);
        if (bottomLeft == null) {
            return null;
        }

        // Find the frame dimensions from bottom-left position
        int width = findFrameExtent(level, bottomLeft, horizontal, MAX_WIDTH);
        int height = findFrameExtent(level, bottomLeft, vertical, MAX_HEIGHT);

        // Validate dimensions
        if (width < MIN_WIDTH || width > MAX_WIDTH || height < MIN_HEIGHT || height > MAX_HEIGHT) {
            return null;
        }

        // Validate frame structure
        if (!validateFrameStructure(level, bottomLeft, horizontal, vertical, width, height)) {
            return null;
        }

        // Return valid portal frame data
        return new PortalFrameData(bottomLeft, width, height, axis);
    }

    /**
     * Find the bottom-left corner of the portal frame bounding box.
     * Explores from the given position in all directions to find the frame bounds.
     * This returns the minimum position in both horizontal and vertical directions.
     *
     * @param level The level
     * @param pos Starting position (any frame block)
     * @param horizontal Horizontal direction (EAST or SOUTH)
     * @param vertical Vertical direction (UP)
     * @return Bottom-left corner position of the bounding box, or null if invalid
     */
    private static BlockPos findBottomLeftCorner(Level level, BlockPos pos, Direction horizontal, Direction vertical) {
        // Collect all frame blocks in the vicinity to find the bounding box
        Set<BlockPos> frameBlocks = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toVisit = new LinkedList<>();

        toVisit.add(pos);
        visited.add(pos);

        // Flood fill to find all connected frame blocks (within limits)
        while (!toVisit.isEmpty()) {
            BlockPos current = toVisit.poll();
            if (!isFrameBlock(level, current)) {
                continue;
            }

            frameBlocks.add(current);

            // Check all 4 perpendicular directions (not diagonal)
            for (Direction dir : new Direction[]{horizontal, horizontal.getOpposite(), vertical, vertical.getOpposite()}) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    // Only continue if within reasonable bounds
                    if (Math.abs(neighbor.getX() - pos.getX()) <= MAX_WIDTH &&
                        Math.abs(neighbor.getY() - pos.getY()) <= MAX_HEIGHT &&
                        Math.abs(neighbor.getZ() - pos.getZ()) <= MAX_WIDTH) {
                        toVisit.add(neighbor);
                    }
                }
            }
        }

        if (frameBlocks.isEmpty()) {
            return null;
        }

        // Find the minimum position (bottom-left of bounding box)
        BlockPos min = null;
        for (BlockPos framePos : frameBlocks) {
            if (min == null) {
                min = framePos;
            } else {
                // Compare positions based on axis
                int minHorizontal = getHorizontalCoordinate(min, horizontal.getAxis());
                int minVertical = min.getY();
                int currentHorizontal = getHorizontalCoordinate(framePos, horizontal.getAxis());
                int currentVertical = framePos.getY();

                if (currentVertical < minVertical ||
                    (currentVertical == minVertical && currentHorizontal < minHorizontal)) {
                    min = framePos;
                }
            }
        }

        return min;
    }

    /**
     * Get the coordinate value for the given axis.
     *
     * @param pos Position
     * @param axis Axis (X or Z)
     * @return X or Z coordinate
     */
    private static int getHorizontalCoordinate(BlockPos pos, Direction.Axis axis) {
        return axis == Direction.Axis.X ? pos.getX() : pos.getZ();
    }

    /**
     * Find the extent (width or height) of the frame in a given direction.
     * This scans from the bottom-left position to find where the frame ends.
     *
     * @param level The level
     * @param start Starting position (bottom-left of bounding box)
     * @param direction Direction to search (horizontal or vertical)
     * @param maxSize Maximum size to search
     * @return Frame extent, or 0 if invalid
     */
    private static int findFrameExtent(Level level, BlockPos start, Direction direction, int maxSize) {
        // Scan from start position to find the last frame block in this direction
        // We need to account for missing corners
        int maxExtent = 1; // At least the starting position

        for (int i = 1; i < maxSize; i++) {
            BlockPos checkPos = start.relative(direction, i);

            // Check if this position or any position perpendicular to it has a frame block
            // This handles missing corners
            Direction perpendicular = direction.getAxis() == Direction.Axis.Y ?
                Direction.EAST : Direction.UP;

            boolean hasFrameInLine = false;
            for (int offset = -1; offset <= 1; offset++) {
                BlockPos testPos = checkPos.relative(perpendicular, offset);
                if (isFrameBlock(level, testPos)) {
                    hasFrameInLine = true;
                    maxExtent = i + 1;
                    break;
                }
            }

            if (!hasFrameInLine) {
                // No frame blocks found in this line, assume end of frame
                break;
            }
        }

        return maxExtent;
    }

    /**
     * Validate the entire frame structure.
     *
     * Like Nether Portal, corners are optional - only edges need to be Clockstone Blocks.
     *
     * @param level The level
     * @param bottomLeft Bottom-left corner position
     * @param horizontal Horizontal direction
     * @param vertical Vertical direction
     * @param width Frame width
     * @param height Frame height
     * @return true if frame structure is valid
     */
    private static boolean validateFrameStructure(Level level, BlockPos bottomLeft, Direction horizontal,
                                                    Direction vertical, int width, int height) {
        Set<BlockPos> framePositions = new HashSet<>();
        Set<BlockPos> cornerPositions = new HashSet<>();

        // Identify corner positions (these are optional)
        BlockPos bottomLeftCorner = bottomLeft;
        BlockPos bottomRightCorner = bottomLeft.relative(horizontal, width - 1);
        BlockPos topLeftCorner = bottomLeft.relative(vertical, height - 1);
        BlockPos topRightCorner = bottomLeft.relative(horizontal, width - 1).relative(vertical, height - 1);

        cornerPositions.add(bottomLeftCorner);
        cornerPositions.add(bottomRightCorner);
        cornerPositions.add(topLeftCorner);
        cornerPositions.add(topRightCorner);

        // Collect all frame positions (edges only, excluding corners)
        // Bottom edge (excluding corners)
        for (int x = 1; x < width - 1; x++) {
            framePositions.add(bottomLeft.relative(horizontal, x));
        }

        // Top edge (excluding corners)
        for (int x = 1; x < width - 1; x++) {
            framePositions.add(bottomLeft.relative(horizontal, x).relative(vertical, height - 1));
        }

        // Left edge (excluding corners)
        for (int y = 1; y < height - 1; y++) {
            framePositions.add(bottomLeft.relative(vertical, y));
        }

        // Right edge (excluding corners)
        for (int y = 1; y < height - 1; y++) {
            framePositions.add(bottomLeft.relative(horizontal, width - 1).relative(vertical, y));
        }

        // Validate all frame positions are Clockstone Blocks
        for (BlockPos framePos : framePositions) {
            if (!isFrameBlock(level, framePos)) {
                return false;
            }
        }

        // Validate interior is air (or portal blocks if already activated)
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                BlockPos interiorPos = bottomLeft.relative(horizontal, x).relative(vertical, y);
                if (!isValidInteriorBlock(level, interiorPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if a block is a valid frame block (Clockstone Block).
     *
     * @param level The level
     * @param pos Position to check
     * @return true if block is Clockstone Block
     */
    private static boolean isFrameBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.CLOCKSTONE_BLOCK.get());
    }

    /**
     * Check if a block is a valid interior block (air or portal block).
     *
     * @param level The level
     * @param pos Position to check
     * @return true if block is air or portal block
     */
    private static boolean isValidInteriorBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // For now, only accept air blocks
        // Portal blocks will be added when portal activation is implemented
        return state.isAir();
    }

    /**
     * Portal Frame Data - Contains validated portal frame information.
     */
    public static class PortalFrameData {
        private final BlockPos bottomLeft;
        private final int width;
        private final int height;
        private final Direction.Axis axis;

        public PortalFrameData(BlockPos bottomLeft, int width, int height, Direction.Axis axis) {
            this.bottomLeft = bottomLeft;
            this.width = width;
            this.height = height;
            this.axis = axis;
        }

        public BlockPos getBottomLeft() {
            return bottomLeft;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Direction.Axis getAxis() {
            return axis;
        }

        /**
         * Get all interior positions of the portal frame.
         *
         * @return Set of interior block positions
         */
        public Set<BlockPos> getInteriorPositions() {
            Set<BlockPos> positions = new HashSet<>();
            Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
            Direction vertical = Direction.UP;

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    positions.add(bottomLeft.relative(horizontal, x).relative(vertical, y));
                }
            }

            return positions;
        }
    }
}
