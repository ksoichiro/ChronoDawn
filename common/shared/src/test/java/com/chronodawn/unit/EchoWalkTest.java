package com.chronodawn.unit;

import com.chronodawn.core.wayside.EchoWalk;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EchoWalkTest {

    private static EchoWalk walk() {
        return new EchoWalk(0, 0, 20, 0, 100);
    }

    @Test
    void walksAtConstantSpeedFromTheStart() {
        assertArrayEquals(new double[] {0, 0}, walk().positionAt(100), 1e-9);
        assertArrayEquals(new double[] {8, 0}, walk().positionAt(200), 1e-9);
    }

    @Test
    void staysAtTheStartBeforeItBegins() {
        assertArrayEquals(new double[] {0, 0}, walk().positionAt(50), 1e-9);
    }

    @Test
    void arrivesAfterTheWholeDistance() {
        assertFalse(walk().arrived(349));
        assertTrue(walk().arrived(350));
        assertEquals(350L, walk().arrivalGameTime());
    }

    @Test
    void finishesAfterTheLinger() {
        assertFalse(walk().finished(350));
        assertFalse(walk().finished(1549));
        assertTrue(walk().finished(1550));
    }

    @Test
    void staysAtTheEndAfterArrival() {
        assertArrayEquals(new double[] {20, 0}, walk().positionAt(350), 1e-9);
        assertArrayEquals(new double[] {20, 0}, walk().positionAt(5000), 1e-9);
    }

    @Test
    void walksAlongADiagonal() {
        EchoWalk diagonal = new EchoWalk(0, 0, 3, 4, 0);
        // 5 blocks long at 0.08 per tick: 31 ticks is 2.48 blocks along the 3-4-5 line.
        assertArrayEquals(new double[] {2.48 * 0.6, 2.48 * 0.8}, diagonal.positionAt(31), 1e-9);
        assertEquals(63L, diagonal.arrivalGameTime());
        assertArrayEquals(new double[] {3, 4}, diagonal.positionAt(63), 1e-9);
    }
}
