package com.chronodawn.unit;

import com.chronodawn.core.portal.PortalFrameValidator;
import com.chronodawn.core.portal.PortalFrameValidator.PortalFrameData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Exercises the frame search of PortalFrameValidator through the Predicate overloads, so no Level
 * or block registry is needed. Blocks are two sets: frame blocks and non-air blocks.
 */
class PortalFrameValidatorTest {

    private static final BlockPos ORIGIN = new BlockPos(100, 64, 200);

    /** A world made of explicit block sets. Everything not listed is air. */
    private static final class FakeWorld {
        final Set<BlockPos> frame = new HashSet<>();
        final Set<BlockPos> otherSolid = new HashSet<>();

        Predicate<BlockPos> isFrame() {
            return frame::contains;
        }

        Predicate<BlockPos> isInterior() {
            return pos -> !frame.contains(pos) && !otherSolid.contains(pos);
        }

        PortalFrameData validate(BlockPos pos, Direction.Axis axis) {
            return PortalFrameValidator.validateFrame(isFrame(), isInterior(), pos, axis);
        }

        PortalFrameData find(BlockPos pos) {
            return PortalFrameValidator.findFrameContaining(isFrame(), isInterior(), pos);
        }
    }

    private static Direction horizontal(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    private static BlockPos at(BlockPos origin, Direction.Axis axis, int h, int v) {
        return origin.relative(horizontal(axis), h).relative(Direction.UP, v);
    }

    /** Builds a full rectangular frame (corners included) with its bottom-left block at origin. */
    private static FakeWorld frameOf(int width, int height, Direction.Axis axis) {
        FakeWorld world = new FakeWorld();
        for (int h = 0; h < width; h++) {
            world.frame.add(at(ORIGIN, axis, h, 0));
            world.frame.add(at(ORIGIN, axis, h, height - 1));
        }
        for (int v = 0; v < height; v++) {
            world.frame.add(at(ORIGIN, axis, 0, v));
            world.frame.add(at(ORIGIN, axis, width - 1, v));
        }
        return world;
    }

    private static void assertFrame(PortalFrameData data, int width, int height, Direction.Axis axis) {
        assertNotNull(data);
        assertEquals(ORIGIN, data.getBottomLeft());
        assertEquals(width, data.getWidth());
        assertEquals(height, data.getHeight());
        assertEquals(axis, data.getAxis());
    }

    @Test
    void minimumFrameIsValidOnBothAxes() {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            FakeWorld world = frameOf(PortalFrameValidator.MIN_WIDTH, PortalFrameValidator.MIN_HEIGHT, axis);
            assertFrame(world.validate(ORIGIN, axis), 4, 5, axis);
        }
    }

    @Test
    void maximumFrameIsValid() {
        FakeWorld world = frameOf(PortalFrameValidator.MAX_WIDTH, PortalFrameValidator.MAX_HEIGHT, Direction.Axis.X);
        assertFrame(world.validate(ORIGIN, Direction.Axis.X), 23, 23, Direction.Axis.X);
    }

    @Test
    void validationStartsFromAnyFrameBlock() {
        FakeWorld world = frameOf(6, 7, Direction.Axis.X);
        BlockPos topEdge = at(ORIGIN, Direction.Axis.X, 3, 6);
        assertFrame(world.validate(topEdge, Direction.Axis.X), 6, 7, Direction.Axis.X);
    }

    @Test
    void startingOnANonFrameBlockIsInvalid() {
        FakeWorld world = frameOf(5, 6, Direction.Axis.X);
        assertNull(world.validate(at(ORIGIN, Direction.Axis.X, 2, 2), Direction.Axis.X));
    }

    @Test
    void framesBelowTheMinimumSizeAreInvalid() {
        assertNull(frameOf(3, 5, Direction.Axis.X).validate(ORIGIN, Direction.Axis.X));
        assertNull(frameOf(4, 4, Direction.Axis.X).validate(ORIGIN, Direction.Axis.X));
    }

    @Test
    void framesAboveTheMaximumSizeAreInvalid() {
        assertNull(frameOf(24, 5, Direction.Axis.X).validate(ORIGIN, Direction.Axis.X));
        assertNull(frameOf(4, 24, Direction.Axis.X).validate(ORIGIN, Direction.Axis.X));
    }

    @Test
    void anyCombinationOfMissingCornersIsStillValid() {
        int width = 5;
        int height = 6;
        for (int mask = 0; mask < 16; mask++) {
            FakeWorld world = frameOf(width, height, Direction.Axis.X);
            if ((mask & 1) != 0) world.frame.remove(at(ORIGIN, Direction.Axis.X, 0, 0));
            if ((mask & 2) != 0) world.frame.remove(at(ORIGIN, Direction.Axis.X, width - 1, 0));
            if ((mask & 4) != 0) world.frame.remove(at(ORIGIN, Direction.Axis.X, 0, height - 1));
            if ((mask & 8) != 0) world.frame.remove(at(ORIGIN, Direction.Axis.X, width - 1, height - 1));

            // Start from a block on the bottom edge, which exists in every combination.
            PortalFrameData data = world.validate(at(ORIGIN, Direction.Axis.X, 2, 0), Direction.Axis.X);
            assertFrame(data, width, height, Direction.Axis.X);
        }
    }

    @Test
    void aMissingEdgeBlockIsInvalid() {
        int width = 5;
        int height = 6;
        BlockPos[] edgeBlocks = {
            at(ORIGIN, Direction.Axis.X, 2, 0),
            at(ORIGIN, Direction.Axis.X, 2, height - 1),
            at(ORIGIN, Direction.Axis.X, 0, 3),
            at(ORIGIN, Direction.Axis.X, width - 1, 3),
        };
        for (BlockPos missing : edgeBlocks) {
            FakeWorld world = frameOf(width, height, Direction.Axis.X);
            world.frame.remove(missing);
            assertNull(world.validate(ORIGIN, Direction.Axis.X), "missing " + missing);
        }
    }

    @Test
    void aNonAirBlockInsideTheFrameIsInvalid() {
        FakeWorld world = frameOf(5, 6, Direction.Axis.X);
        world.otherSolid.add(at(ORIGIN, Direction.Axis.X, 2, 3));
        assertNull(world.validate(ORIGIN, Direction.Axis.X));
    }

    @Test
    void aFrameOnTheOtherAxisIsNotValidForTheWrongAxis() {
        FakeWorld world = frameOf(5, 6, Direction.Axis.X);
        assertNull(world.validate(ORIGIN, Direction.Axis.Z));
    }

    @Test
    void findFrameContainingDetectsTheAxisOfTheFrame() {
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            FakeWorld world = frameOf(5, 6, axis);
            assertFrame(world.find(at(ORIGIN, axis, 2, 0)), 5, 6, axis);
        }
    }

    @Test
    void findFrameContainingReachesTheFarCornerOfTheLargestFrame() {
        FakeWorld world = frameOf(23, 23, Direction.Axis.Z);
        BlockPos farCorner = at(ORIGIN, Direction.Axis.Z, 22, 22);
        assertFrame(world.find(farCorner), 23, 23, Direction.Axis.Z);
    }

    @Test
    void findFrameContainingReturnsNullForAnInvalidFrame() {
        FakeWorld world = frameOf(5, 6, Direction.Axis.X);
        world.frame.remove(at(ORIGIN, Direction.Axis.X, 2, 0));
        assertNull(world.find(at(ORIGIN, Direction.Axis.X, 0, 3)));
    }

    @Test
    void findFrameContainingReturnsNullForANonFrameBlock() {
        FakeWorld world = frameOf(5, 6, Direction.Axis.X);
        assertNull(world.find(at(ORIGIN, Direction.Axis.X, 2, 2)));
    }
}
