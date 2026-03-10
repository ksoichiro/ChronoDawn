package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates loot table JSON resources for item reference consistency.
 * Checks that all chronodawn: item names in loot tables reference valid registered items.
 */
public class LootTableValidationTest {

    @TestFactory
    Collection<DynamicTest> blockLootTableItemReferenceTests() {
        return lootTableItemTests("data/chronodawn/loot_table/blocks/", "block_loot");
    }

    @TestFactory
    Collection<DynamicTest> entityLootTableItemReferenceTests() {
        return lootTableItemTests("data/chronodawn/loot_table/entities/", "entity_loot");
    }

    @TestFactory
    Collection<DynamicTest> chestLootTableItemReferenceTests() {
        return lootTableItemTests("data/chronodawn/loot_table/chests/", "chest_loot");
    }

    private Collection<DynamicTest> lootTableItemTests(String resourcePrefix, String testPrefix) {
        List<String> lootTableFiles = TestUtils.findJsonResources(resourcePrefix);
        if (lootTableFiles.isEmpty()) {
            return List.of(DynamicTest.dynamicTest(testPrefix + "_files_found", () -> {
                // No loot table files found for this version - skip
            }));
        }

        Set<String> validIds = TestUtils.getAllItemAndBlockIds();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String ltPath : lootTableFiles) {
            String ltName = ltPath.substring(ltPath.lastIndexOf('/') + 1).replace(".json", "");
            Map<String, Object> lt = TestUtils.loadJsonResource(ltPath);
            if (lt == null) continue;

            Set<String> itemRefs = extractLootTableItemReferences(lt);
            for (String ref : itemRefs) {
                tests.add(DynamicTest.dynamicTest(
                    testPrefix + "_" + ltName + "_" + ref.replace("chronodawn:", ""),
                    () -> assertTrue(
                        validIds.contains(ref),
                        "Loot table '" + ltName + "' references unknown item: " + ref
                    )
                ));
            }
        }

        return tests;
    }

    /**
     * Extract chronodawn: item references from loot table JSON.
     * Looks for "name" fields in entries of type "minecraft:item".
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractLootTableItemReferences(Object obj) {
        Set<String> refs = new java.util.HashSet<>();

        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;

            // Check if this is an item entry with a "name" field
            String type = (String) map.get("type");
            String name = (String) map.get("name");
            if ("minecraft:item".equals(type) && name != null && name.startsWith("chronodawn:")) {
                refs.add(name);
            }

            // Also check "name" in functions (e.g., set_nbt, set_lore referencing items)
            // and recursively check all nested objects
            for (Object value : map.values()) {
                refs.addAll(extractLootTableItemReferences(value));
            }
        } else if (obj instanceof List) {
            for (Object item : (List<Object>) obj) {
                refs.addAll(extractLootTableItemReferences(item));
            }
        }

        return refs;
    }
}
