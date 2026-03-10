package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates spawn egg consistency.
 * Checks that all non-projectile, non-boat entities have corresponding
 * spawn egg items registered.
 */
public class SpawnEggConsistencyTest {

    // Entities that intentionally don't have spawn eggs
    private static final Set<String> SPAWN_EGG_EXCLUDED_ENTITIES = Set.of(
        "GEAR_PROJECTILE",   // Projectile, not a spawnable mob
        "TIME_ARROW",        // Projectile
        "TIME_BLAST",        // Projectile
        "CHRONO_DAWN_BOAT",  // Vehicle, spawned via boat item
        "CHRONO_DAWN_CHEST_BOAT"  // Vehicle, spawned via chest boat item
    );

    @TestFactory
    Collection<DynamicTest> entityHasSpawnEggTests() {
        List<String> entityFields = TestUtils.getFieldNames("ModEntities.java");
        List<String> itemFields = TestUtils.getFieldNames("ModItems.java");
        Set<String> itemFieldSet = new java.util.HashSet<>(itemFields);

        Collection<DynamicTest> tests = new ArrayList<>();

        for (String entityField : entityFields) {
            if (SPAWN_EGG_EXCLUDED_ENTITIES.contains(entityField)) continue;

            String expectedSpawnEgg = entityField + "_SPAWN_EGG";
            tests.add(DynamicTest.dynamicTest(
                "spawn_egg_exists_" + entityField.toLowerCase(),
                () -> assertTrue(
                    itemFieldSet.contains(expectedSpawnEgg),
                    "Entity '" + entityField + "' has no spawn egg item. " +
                    "Expected: " + expectedSpawnEgg
                )
            ));
        }

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> spawnEggReferencesValidEntityTests() {
        List<String> itemFields = TestUtils.getFieldNames("ModItems.java");
        List<String> entityFields = TestUtils.getFieldNames("ModEntities.java");
        Set<String> entityFieldSet = new java.util.HashSet<>(entityFields);

        Collection<DynamicTest> tests = new ArrayList<>();

        for (String itemField : itemFields) {
            if (!itemField.endsWith("_SPAWN_EGG")) continue;

            String expectedEntity = itemField.replace("_SPAWN_EGG", "");
            tests.add(DynamicTest.dynamicTest(
                "spawn_egg_valid_entity_" + itemField.toLowerCase(),
                () -> assertTrue(
                    entityFieldSet.contains(expectedEntity),
                    "Spawn egg '" + itemField + "' references unknown entity. " +
                    "Expected entity: " + expectedEntity
                )
            ));
        }

        return tests;
    }
}
