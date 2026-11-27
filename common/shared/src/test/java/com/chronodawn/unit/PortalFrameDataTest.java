package com.chronodawn.unit;

import com.chronodawn.core.portal.PortalFrameValidator;
import com.chronodawn.core.portal.PortalFrameValidator.PortalFrameData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the parts of PortalFrameValidator that do not need a Level or the block registry.
 * The frame search itself reads block states through Level and ModBlocks, so it stays
 * covered by the GameTests and scenarios.
 */
class PortalFrameDataTest {

    @Test
    void sizeLimitsMatchTheDocumentedFrameRange() {
        assertEquals(4, PortalFrameValidator.MIN_WIDTH);
        assertEquals(5, PortalFrameValidator.MIN_HEIGHT);
        assertEquals(23, PortalFrameValidator.MAX_WIDTH);
        assertEquals(23, PortalFrameValidator.MAX_HEIGHT);
    }

    @Test
    void accessorsReturnConstructorValues() {
        BlockPos origin = new BlockPos(10, 64, -3);
        PortalFrameData data = new PortalFrameData(origin, 4, 5, Direction.Axis.Z);

        assertEquals(origin, data.getBottomLeft());
        assertEquals(4, data.getWidth());
        assertEquals(5, data.getHeight());
        assertEquals(Direction.Axis.Z, data.getAxis());
    }

    @Test
    void interiorOfMinimumFrameIsTwoByThreeAlongX() {
        BlockPos origin = new BlockPos(10, 64, 0);
        Set<BlockPos> interior = new PortalFrameData(origin, 4, 5, Direction.Axis.X).getInteriorPositions();

        assertEquals(6, interior.size());
        for (int x = 11; x <= 12; x++) {
            for (int y = 65; y <= 67; y++) {
                assertTrue(interior.contains(new BlockPos(x, y, 0)), "missing " + x + "," + y);
            }
        }
    }

    @Test
    void interiorExtendsAlongZWhenAxisIsZ() {
        BlockPos origin = new BlockPos(0, 64, 20);
        Set<BlockPos> interior = new PortalFrameData(origin, 4, 5, Direction.Axis.Z).getInteriorPositions();

        assertEquals(6, interior.size());
        assertTrue(interior.contains(new BlockPos(0, 65, 21)));
        assertTrue(interior.contains(new BlockPos(0, 67, 22)));
        assertFalse(interior.contains(new BlockPos(1, 65, 21)));
    }

    @Test
    void interiorExcludesTheFrameRing() {
        BlockPos origin = new BlockPos(0, 0, 0);
        Set<BlockPos> interior = new PortalFrameData(origin, 5, 6, Direction.Axis.X).getInteriorPositions();

        assertEquals(3 * 4, interior.size());
        assertFalse(interior.contains(origin));
        assertFalse(interior.contains(new BlockPos(4, 0, 0)));
        assertFalse(interior.contains(new BlockPos(0, 5, 0)));
        assertFalse(interior.contains(new BlockPos(4, 5, 0)));
        assertFalse(interior.contains(new BlockPos(2, 0, 0)));
        assertFalse(interior.contains(new BlockPos(2, 5, 0)));
    }

    @Test
    void interiorOfMaximumFrameIsTwentyOneSquare() {
        Set<BlockPos> interior = new PortalFrameData(BlockPos.ZERO, 23, 23, Direction.Axis.X).getInteriorPositions();

        assertEquals(21 * 21, interior.size());
    }

    @Test
    void frameWithoutInteriorYieldsNoPositions() {
        assertTrue(new PortalFrameData(BlockPos.ZERO, 2, 2, Direction.Axis.X).getInteriorPositions().isEmpty());
    }
}
