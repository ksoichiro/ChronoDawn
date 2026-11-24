package com.chronodawn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * The needle must agree with a vanilla compass. Minecraft yaw 0 faces south (+Z),
 * 90 faces west (-X), and the angle runs clockwise from straight ahead.
 */
class TimeCompassNeedleTest {
    private static final float EPS = 1e-5f;

    @Test
    void targetAheadPointsAhead() {
        assertEquals(0.0f, TimeCompassNeedle.needleAngle(0, 100, 0f), EPS); // south, facing south
        assertEquals(0.0f, TimeCompassNeedle.needleAngle(-100, 0, 90f), EPS); // west, facing west
        assertEquals(0.0f, TimeCompassNeedle.needleAngle(0, -100, 180f), EPS); // north, facing north
        assertEquals(0.0f, TimeCompassNeedle.needleAngle(100, 0, 270f), EPS); // east, facing east
    }

    @Test
    void targetOnTheSidesAndBehind() {
        // Facing south: west is on the right, east on the left, north behind.
        assertEquals(0.25f, TimeCompassNeedle.needleAngle(-100, 0, 0f), EPS);
        assertEquals(0.75f, TimeCompassNeedle.needleAngle(100, 0, 0f), EPS);
        assertEquals(0.5f, TimeCompassNeedle.needleAngle(0, -100, 0f), EPS);
    }

    @Test
    void southwestTargetSeenFromSouthFacing() {
        // The case reported from the game: target to the south-west, player facing south.
        assertEquals(0.125f, TimeCompassNeedle.needleAngle(-100, 100, 0f), EPS);
    }

    @Test
    void wobbleStepIsBoundedAndStaysInRange() {
        // delta' = (delta + diff * 0.1) * 0.8 with |diff| <= 0.5 converges to at most 0.2 per tick.
        Random random = new Random(42);
        double rotation = 0.0;
        double delta = 0.0;
        for (int tick = 0; tick < 2000; tick++) {
            double[] next = TimeCompassNeedle.wobbleStep(rotation, delta, random.nextDouble());
            double moved = Math.abs(((next[0] - rotation + 1.5) % 1.0) - 0.5);
            assertTrue(moved <= 0.2 + 1e-9, "needle jumped " + moved + " in one tick");
            assertTrue(next[0] >= 0.0 && next[0] < 1.0);
            rotation = next[0];
            delta = next[1];
        }
    }
}
