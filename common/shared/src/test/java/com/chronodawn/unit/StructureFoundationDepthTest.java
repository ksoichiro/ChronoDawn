package com.chronodawn.unit;

import com.chronodawn.worldgen.StructureFoundationFiller;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link StructureFoundationFiller#maxFoundationDepth}, which decides whether a
 * surface structure hangs too far over the ground to be placed.
 */
class StructureFoundationDepthTest {

    // A 35 x 35 floor like the Forgotten Library, at y=100.
    private static final int FLOOR_Y = 100;
    private static final BoundingBox FLOOR = new BoundingBox(0, FLOOR_Y, 0, 34, FLOOR_Y + 14, 34);

    @Test
    void flatGroundUnderTheFloorHasNoHollow() {
        // The ground's top block is replaced by the floor, so the first free y is the floor's y.
        assertEquals(0, StructureFoundationFiller.maxFoundationDepth(FLOOR, (x, z) -> FLOOR_Y));
    }

    @Test
    void groundAboveTheFloorDoesNotCount() {
        assertEquals(0, StructureFoundationFiller.maxFoundationDepth(FLOOR, (x, z) -> FLOOR_Y + 10));
    }

    @Test
    void slopeReportsTheDeepestEdge() {
        // The ground falls one block for every two blocks in x, down to 17 blocks at x=34.
        assertEquals(17, StructureFoundationFiller.maxFoundationDepth(FLOOR, (x, z) -> FLOOR_Y - x / 2));
    }

    @Test
    void farEdgeIsSampledEvenOffTheGrid() {
        // Only the last row is low. It is not on the 8-block grid, so it must be sampled on its own.
        assertEquals(12, StructureFoundationFiller.maxFoundationDepth(FLOOR,
            (x, z) -> z == 34 ? FLOOR_Y - 12 : FLOOR_Y));
    }
}
