package com.chronodawn.core.portal;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
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
 * - All corners and edges must be Clockstone Blocks
 * - Interior must be air blocks (or will be filled with portal blocks)
 *
 * Validation Process:
 * 1. Check if bottom-left corner is Clockstone Block
 * 2. Find the frame's width and height
 * 3. Verify all frame blocks are Clockstone Blocks
 * 4. Verify interior is air or portal blocks
 * 5. Verify size is within valid range
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
     *
     * @param level The level containing the portal
     * @param pos Starting position (should be bottom-left corner of frame)
     * @param axis Portal axis (X or Z)
     * @return PortalFrameData if valid, null if invalid
     */
    public static PortalFrameData validateFrame(Level level, BlockPos pos, Direction.Axis axis) {
        // Determine the horizontal and vertical directions based on axis
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction vertical = Direction.UP;

        // Find the frame dimensions
        int width = findFrameDimension(level, pos, horizontal, MAX_WIDTH);
        int height = findFrameDimension(level, pos, vertical, MAX_HEIGHT);

        // Validate dimensions
        if (width < MIN_WIDTH || width > MAX_WIDTH || height < MIN_HEIGHT || height > MAX_HEIGHT) {
            return null;
        }

        // Validate frame structure
        if (!validateFrameStructure(level, pos, horizontal, vertical, width, height)) {
            return null;
        }

        // Return valid portal frame data
        return new PortalFrameData(pos, width, height, axis);
    }

    /**
     * Find the dimension of the frame in a given direction.
     *
     * @param level The level
     * @param start Starting position
     * @param direction Direction to search
     * @param maxSize Maximum size to search
     * @return Frame dimension, or 0 if invalid
     */
    private static int findFrameDimension(Level level, BlockPos start, Direction direction, int maxSize) {
        for (int i = 1; i <= maxSize; i++) {
            BlockPos checkPos = start.relative(direction, i);
            if (!isFrameBlock(level, checkPos)) {
                return i;
            }
        }
        return 0; // Exceeded max size
    }

    /**
     * Validate the entire frame structure.
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

        // Collect all frame positions (edges only)
        // Bottom edge
        for (int x = 0; x < width; x++) {
            framePositions.add(bottomLeft.relative(horizontal, x));
        }

        // Top edge
        for (int x = 0; x < width; x++) {
            framePositions.add(bottomLeft.relative(horizontal, x).relative(vertical, height - 1));
        }

        // Left edge
        for (int y = 0; y < height; y++) {
            framePositions.add(bottomLeft.relative(vertical, y));
        }

        // Right edge
        for (int y = 0; y < height; y++) {
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
