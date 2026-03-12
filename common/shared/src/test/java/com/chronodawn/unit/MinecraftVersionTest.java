package com.chronodawn.unit;

import com.chronodawn.version.MinecraftVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MinecraftVersion} enum logic.
 * Validates version comparison, component extraction, and boundary values.
 */
class MinecraftVersionTest {

    @Test
    void isAtLeast_sameVersion() {
        for (MinecraftVersion v : MinecraftVersion.values()) {
            assertTrue(v.isAtLeast(v), v + " should be at least itself");
        }
    }

    @Test
    void isAtLeast_newerVersion() {
        assertTrue(MinecraftVersion.MC_1_21_1.isAtLeast(MinecraftVersion.MC_1_20_1));
        assertTrue(MinecraftVersion.MC_1_21_2.isAtLeast(MinecraftVersion.MC_1_21_1));
        assertTrue(MinecraftVersion.MC_1_21_10.isAtLeast(MinecraftVersion.MC_1_21_9));
    }

    @Test
    void isAtLeast_olderVersion() {
        assertFalse(MinecraftVersion.MC_1_20_1.isAtLeast(MinecraftVersion.MC_1_21_1));
        assertFalse(MinecraftVersion.MC_1_21_1.isAtLeast(MinecraftVersion.MC_1_21_2));
        assertFalse(MinecraftVersion.MC_1_21_7.isAtLeast(MinecraftVersion.MC_1_21_8));
    }

    @Test
    void isAtLeast_handlesDoubleDigitPatch() {
        assertTrue(MinecraftVersion.MC_1_21_10.isAtLeast(MinecraftVersion.MC_1_21_9));
        assertFalse(MinecraftVersion.MC_1_21_9.isAtLeast(MinecraftVersion.MC_1_21_10));
    }

    @Test
    void isAtLeast_crossMajorMinor() {
        assertTrue(MinecraftVersion.MC_1_21_1.isAtLeast(MinecraftVersion.MC_1_20_1));
        assertFalse(MinecraftVersion.MC_1_20_1.isAtLeast(MinecraftVersion.MC_1_21_1));
    }

    @Test
    void componentExtraction() {
        assertEquals(1, MinecraftVersion.MC_1_21_10.major());
        assertEquals(21, MinecraftVersion.MC_1_21_10.minor());
        assertEquals(10, MinecraftVersion.MC_1_21_10.patch());

        assertEquals(1, MinecraftVersion.MC_1_20_1.major());
        assertEquals(20, MinecraftVersion.MC_1_20_1.minor());
        assertEquals(1, MinecraftVersion.MC_1_20_1.patch());
    }

    @Test
    void displayName() {
        assertEquals("1.20.1", MinecraftVersion.MC_1_20_1.displayName());
        assertEquals("1.21.10", MinecraftVersion.MC_1_21_10.displayName());
    }

    @Test
    void toString_returnsDisplayName() {
        for (MinecraftVersion v : MinecraftVersion.values()) {
            assertEquals(v.displayName(), v.toString());
        }
    }

    @Test
    void oldest_returnsFirstVersion() {
        assertEquals(MinecraftVersion.MC_1_20_1, MinecraftVersion.oldest());
    }

    @Test
    void newest_returnsLastVersion() {
        assertEquals(MinecraftVersion.MC_1_21_10, MinecraftVersion.newest());
    }

    @Test
    void oldest_isAtMostEveryVersion() {
        for (MinecraftVersion version : MinecraftVersion.values()) {
            assertTrue(version.isAtLeast(MinecraftVersion.oldest()),
                version + " should be at least " + MinecraftVersion.oldest());
        }
    }

    @Test
    void newest_isAtLeastEveryVersion() {
        for (MinecraftVersion version : MinecraftVersion.values()) {
            assertTrue(MinecraftVersion.newest().isAtLeast(version),
                MinecraftVersion.newest() + " should be at least " + version);
        }
    }
}
