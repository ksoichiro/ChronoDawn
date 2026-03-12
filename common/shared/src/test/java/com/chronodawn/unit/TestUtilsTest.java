package com.chronodawn.unit;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TestUtils} utility methods.
 * Validates version comparison, registry ID conversion, and reference extraction.
 */
class TestUtilsTest {

    // === compareVersions ===

    @Test
    void compareVersions_equal() {
        assertEquals(0, TestUtils.compareVersions("1.21.2", "1.21.2"));
    }

    @Test
    void compareVersions_greaterPatch() {
        assertTrue(TestUtils.compareVersions("1.21.5", "1.21.2") > 0);
    }

    @Test
    void compareVersions_lesserPatch() {
        assertTrue(TestUtils.compareVersions("1.21.2", "1.21.5") < 0);
    }

    @Test
    void compareVersions_doubleDigitPatch() {
        // Semantic: 1.21.10 > 1.21.5 (not lexicographic)
        assertTrue(TestUtils.compareVersions("1.21.10", "1.21.5") > 0);
        assertTrue(TestUtils.compareVersions("1.21.5", "1.21.10") < 0);
    }

    @Test
    void compareVersions_differentMinor() {
        assertTrue(TestUtils.compareVersions("1.21.1", "1.20.1") > 0);
        assertTrue(TestUtils.compareVersions("1.20.1", "1.21.1") < 0);
    }

    @Test
    void compareVersions_differentMajor() {
        assertTrue(TestUtils.compareVersions("2.0.0", "1.21.10") > 0);
    }

    @Test
    void compareVersions_differentLengths() {
        // "1.21" treated as "1.21.0"
        assertTrue(TestUtils.compareVersions("1.21.1", "1.21") > 0);
        assertEquals(0, TestUtils.compareVersions("1.21", "1.21.0"));
    }

    // === toRegistryId ===

    @Test
    void toRegistryId_lowercasesFieldName() {
        assertEquals("time_crystal_ore", TestUtils.toRegistryId("TIME_CRYSTAL_ORE"));
    }

    @Test
    void toRegistryId_overrides() {
        assertEquals("fruit_of_time", TestUtils.toRegistryId("FRUIT_OF_TIME_BLOCK"));
        assertEquals("chronodawn_boat", TestUtils.toRegistryId("CHRONO_DAWN_BOAT"));
        assertEquals("chronodawn_chest_boat", TestUtils.toRegistryId("CHRONO_DAWN_CHEST_BOAT"));
    }

    @Test
    void toRegistryId_nonOverriddenFieldName() {
        assertEquals("clockstone_block", TestUtils.toRegistryId("CLOCKSTONE_BLOCK"));
    }

    // === extractChronodawnReferences ===

    @Test
    void extractChronodawnReferences_simpleString() {
        Set<String> refs = TestUtils.extractChronodawnReferences("chronodawn:clockstone");
        assertEquals(Set.of("chronodawn:clockstone"), refs);
    }

    @Test
    void extractChronodawnReferences_nonChronodawnString() {
        Set<String> refs = TestUtils.extractChronodawnReferences("minecraft:stone");
        assertTrue(refs.isEmpty());
    }

    @Test
    void extractChronodawnReferences_tagReference() {
        // Tag references (#chronodawn:...) should not be included
        Set<String> refs = TestUtils.extractChronodawnReferences("#chronodawn:some_tag");
        assertTrue(refs.isEmpty());
    }

    @Test
    void extractChronodawnReferences_nestedMap() {
        Map<String, Object> json = Map.of(
            "type", "chronodawn:time_guardian",
            "other", "minecraft:zombie",
            "nested", Map.of("item", "chronodawn:clockstone")
        );
        Set<String> refs = TestUtils.extractChronodawnReferences(json);
        assertEquals(Set.of("chronodawn:time_guardian", "chronodawn:clockstone"), refs);
    }

    @Test
    void extractChronodawnReferences_list() {
        List<Object> json = List.of(
            "chronodawn:item_a",
            "minecraft:stone",
            "chronodawn:item_b"
        );
        Set<String> refs = TestUtils.extractChronodawnReferences(json);
        assertEquals(Set.of("chronodawn:item_a", "chronodawn:item_b"), refs);
    }

    @Test
    void extractChronodawnReferences_deeplyNested() {
        Map<String, Object> json = Map.of(
            "pools", List.of(
                Map.of(
                    "entries", List.of(
                        Map.of("name", "chronodawn:deep_item")
                    )
                )
            )
        );
        Set<String> refs = TestUtils.extractChronodawnReferences(json);
        assertEquals(Set.of("chronodawn:deep_item"), refs);
    }

    @Test
    void extractChronodawnReferences_emptyStructures() {
        assertTrue(TestUtils.extractChronodawnReferences(Map.of()).isEmpty());
        assertTrue(TestUtils.extractChronodawnReferences(List.of()).isEmpty());
        assertTrue(TestUtils.extractChronodawnReferences("").isEmpty());
    }

    @Test
    void extractChronodawnReferences_numericValue() {
        // Non-string values should be safely ignored
        Set<String> refs = TestUtils.extractChronodawnReferences(42);
        assertTrue(refs.isEmpty());
    }
}
