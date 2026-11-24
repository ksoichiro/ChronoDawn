package com.chronodawn.items;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Minecraft's -Z is north, +Z south, -X west and +X east.
 */
class TimeCompassDirectionTest {

    @Test
    void cardinalDirections() {
        assertEquals("north", TimeCompassDirection.of(0, -100));
        assertEquals("east", TimeCompassDirection.of(100, 0));
        assertEquals("south", TimeCompassDirection.of(0, 100));
        assertEquals("west", TimeCompassDirection.of(-100, 0));
    }

    @Test
    void ordinalDirections() {
        assertEquals("northeast", TimeCompassDirection.of(100, -100));
        assertEquals("southeast", TimeCompassDirection.of(100, 100));
        assertEquals("southwest", TimeCompassDirection.of(-100, 100));
        assertEquals("northwest", TimeCompassDirection.of(-100, -100));
    }

    @Test
    void reportedCaseIsSouthwest() {
        // Player (-1163, 432), target (-1264, 528): west and south of the player.
        assertEquals("southwest", TimeCompassDirection.of(-1264 - -1163, 528 - 432));
    }

    @Test
    void sectorBoundaries() {
        // 22.5 degrees east of north starts northeast; just below stays north.
        assertEquals("north", TimeCompassDirection.of(41, -100));
        assertEquals("northeast", TimeCompassDirection.of(42, -100));
        // Just west of north wraps around to north, not northwest.
        assertEquals("north", TimeCompassDirection.of(-41, -100));
        assertEquals("northwest", TimeCompassDirection.of(-42, -100));
    }

    @Test
    void samePositionDoesNotFail() {
        // atan2(0, 0) is 0 (east), which keeps the message well-formed when standing on the target.
        assertEquals("east", TimeCompassDirection.of(0, 0));
    }
}
