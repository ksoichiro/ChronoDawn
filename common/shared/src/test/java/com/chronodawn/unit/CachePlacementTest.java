package com.chronodawn.unit;

import com.chronodawn.core.wayside.CachePlacement;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CachePlacementTest {

    @Test
    void eightOffsetsInTheDistanceBand() {
        List<int[]> offsets = CachePlacement.candidateOffsets(42L, 16, 24);
        assertEquals(8, offsets.size());
        Set<Long> sectors = new HashSet<>();
        for (int[] o : offsets) {
            double distance = Math.hypot(o[0], o[1]);
            assertTrue(distance >= 16 && distance <= 24.5, "distance " + distance);
            long degrees = Math.round(Math.toDegrees(Math.atan2(o[1], o[0])) / 45.0) * 45L;
            sectors.add(Math.floorMod(degrees, 360L));
        }
        assertEquals(8, sectors.size());
    }

    @Test
    void shortDistancesStayInside() {
        List<int[]> offsets = CachePlacement.candidateOffsets(42L, 3, 4);
        assertEquals(8, offsets.size());
        for (int[] o : offsets) {
            assertTrue(Math.hypot(o[0], o[1]) <= 4.5, "offset " + o[0] + "," + o[1]);
        }
    }

    @Test
    void sameSeedGivesSameOffsets() {
        List<int[]> a = CachePlacement.candidateOffsets(7L, 16, 24);
        List<int[]> b = CachePlacement.candidateOffsets(7L, 16, 24);
        for (int i = 0; i < a.size(); i++) {
            assertEquals(a.get(i)[0], b.get(i)[0]);
            assertEquals(a.get(i)[1], b.get(i)[1]);
        }
    }
}
