package com.chronodawn.worldgen.protection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pairing rules for Boss Room Boundary Markers. They need no registry bootstrap:
 * BlockPos and BoundingBox are plain value classes.
 */
class BossRoomMarkerPairingTest {
    private record TestMarker(BlockPos pos, BlockPos structureRelativePos, long addedTime)
        implements BossRoomMarkerPairing.Marker {
    }

    private static TestMarker marker(int x, int y, int z, int relY, long time) {
        return new TestMarker(new BlockPos(x, y, z), new BlockPos(0, relY, 0), time);
    }

    @Test
    void pairsOneMinWithOneMax() {
        TestMarker min = marker(0, 10, 0, 0, 1);
        TestMarker max = marker(5, 20, 7, 10, 1);

        var result = BossRoomMarkerPairing.pair(List.of(min), List.of(max));

        assertEquals(1, result.pairs().size());
        assertEquals(min, result.pairs().get(0).min());
        assertEquals(max, result.pairs().get(0).max());
        assertTrue(result.unpairedMins().isEmpty());
        assertTrue(result.unpairedMaxs().isEmpty());
    }

    @Test
    void pairsByStructureRelativeYDescendingNotByInputOrder() {
        TestMarker lowMin = marker(0, 10, 0, 0, 1);
        TestMarker highMin = marker(0, 30, 0, 20, 1);
        TestMarker lowMax = marker(9, 18, 9, 8, 1);
        TestMarker highMax = marker(9, 38, 9, 28, 1);

        // Both orders of input must give the same pairs: high with high, low with low.
        for (var input : List.of(
            List.of(List.of(lowMin, highMin), List.of(lowMax, highMax)),
            List.of(List.of(highMin, lowMin), List.of(highMax, lowMax)),
            List.of(List.of(lowMin, highMin), List.of(highMax, lowMax)))) {
            var result = BossRoomMarkerPairing.pair(input.get(0), input.get(1));

            assertEquals(2, result.pairs().size());
            assertEquals(highMin, result.pairs().get(0).min());
            assertEquals(highMax, result.pairs().get(0).max());
            assertEquals(lowMin, result.pairs().get(1).min());
            assertEquals(lowMax, result.pairs().get(1).max());
        }
    }

    @Test
    void sortsByRelativeYEvenWhenWorldYDisagrees() {
        // World Y differs from the template Y order, for example for pieces placed at other heights.
        TestMarker minA = marker(0, 50, 0, 0, 1);
        TestMarker minB = marker(0, 10, 0, 20, 1);
        TestMarker maxA = marker(4, 60, 4, 5, 1);
        TestMarker maxB = marker(4, 20, 4, 25, 1);

        var result = BossRoomMarkerPairing.pair(List.of(minA, minB), List.of(maxA, maxB));

        assertEquals(minB, result.pairs().get(0).min());
        assertEquals(maxB, result.pairs().get(0).max());
        assertEquals(minA, result.pairs().get(1).min());
        assertEquals(maxA, result.pairs().get(1).max());
    }

    @Test
    void leftoverMinIsReportedWhenThereIsNoMatchingMax() {
        TestMarker highMin = marker(0, 30, 0, 20, 1);
        TestMarker lowMin = marker(0, 10, 0, 0, 1);
        TestMarker max = marker(9, 38, 9, 28, 1);

        var result = BossRoomMarkerPairing.pair(List.of(lowMin, highMin), List.of(max));

        assertEquals(1, result.pairs().size());
        assertEquals(highMin, result.pairs().get(0).min());
        assertEquals(List.of(lowMin), result.unpairedMins());
        assertTrue(result.unpairedMaxs().isEmpty());
    }

    @Test
    void leftoverMaxIsReportedWhenThereIsNoMatchingMin() {
        TestMarker min = marker(0, 10, 0, 0, 1);
        TestMarker highMax = marker(9, 38, 9, 28, 1);
        TestMarker lowMax = marker(9, 18, 9, 8, 1);

        var result = BossRoomMarkerPairing.pair(List.of(min), List.of(lowMax, highMax));

        assertEquals(1, result.pairs().size());
        assertEquals(highMax, result.pairs().get(0).max());
        assertEquals(List.of(lowMax), result.unpairedMaxs());
        assertTrue(result.unpairedMins().isEmpty());
    }

    @Test
    void noMaxMeansNoPairsAndAllMinsLeftOver() {
        TestMarker min = marker(0, 10, 0, 0, 1);

        var result = BossRoomMarkerPairing.pair(List.of(min), List.of());

        assertTrue(result.pairs().isEmpty());
        assertEquals(List.of(min), result.unpairedMins());
    }

    @Test
    void markersAtOnePositionKeepOnlyTheNewestOne() {
        // A later template block (stairs_bottom) overwrote the first marker at this position.
        TestMarker oldMin = marker(0, 10, 0, 0, 100);
        TestMarker newMin = marker(0, 10, 0, 0, 200);
        TestMarker max = marker(5, 20, 5, 10, 100);

        var result = BossRoomMarkerPairing.pair(List.of(newMin, oldMin), List.of(max));
        assertEquals(1, result.pairs().size());
        assertEquals(newMin, result.pairs().get(0).min());
        assertTrue(result.unpairedMins().isEmpty());

        var reversed = BossRoomMarkerPairing.pair(List.of(oldMin, newMin), List.of(max));
        assertEquals(newMin, reversed.pairs().get(0).min());
    }

    @Test
    void duplicatesDoNotCountAsExtraMarkers() {
        TestMarker min = marker(0, 10, 0, 0, 1);
        TestMarker maxOld = marker(5, 20, 5, 10, 1);
        TestMarker maxNew = marker(5, 20, 5, 10, 2);

        var result = BossRoomMarkerPairing.pair(List.of(min), List.of(maxOld, maxNew));

        assertEquals(1, result.pairs().size());
        assertTrue(result.unpairedMaxs().isEmpty());
    }

    @Test
    void areaCoversBothCornersInAnyOrder() {
        BlockPos a = new BlockPos(-3, 70, 12);
        BlockPos b = new BlockPos(8, 64, -5);
        BoundingBox expected = new BoundingBox(-3, 64, -5, 8, 70, 12);

        assertEquals(expected, BossRoomMarkerPairing.areaOf(a, b));
        assertEquals(expected, BossRoomMarkerPairing.areaOf(b, a));
    }

    @Test
    void areaOfOneBlockIsThatBlock() {
        BlockPos p = new BlockPos(4, 5, 6);

        BoundingBox area = BossRoomMarkerPairing.areaOf(p, p);

        assertEquals(new BoundingBox(4, 5, 6, 4, 5, 6), area);
        assertTrue(area.isInside(p));
    }
}
