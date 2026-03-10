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
 * Validates advancement JSON resources for item and entity reference consistency.
 * Checks that display icons, criteria items, and entity references point to
 * valid registered entries.
 */
public class AdvancementValidationTest {

    @TestFactory
    Collection<DynamicTest> advancementItemReferenceTests() {
        // 1.20.1 uses "advancements/", 1.21.1+ uses "advancement/"
        List<String> advFiles = TestUtils.findJsonResources("data/chronodawn/advancement/");
        advFiles.addAll(TestUtils.findJsonResources("data/chronodawn/advancements/"));
        if (advFiles.isEmpty()) return List.of();

        Set<String> validItemIds = TestUtils.getAllItemAndBlockIds();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String advPath : advFiles) {
            String advName = extractAdvancementName(advPath);
            Map<String, Object> adv = TestUtils.loadJsonResource(advPath);
            if (adv == null) continue;

            Set<String> itemRefs = extractItemReferences(adv);
            for (String ref : itemRefs) {
                tests.add(DynamicTest.dynamicTest(
                    "advancement_item_" + advName + "_" + ref.replace("chronodawn:", ""),
                    () -> assertTrue(
                        validItemIds.contains(ref),
                        "Advancement '" + advName + "' references unknown item: " + ref
                    )
                ));
            }
        }

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> advancementEntityReferenceTests() {
        List<String> advFiles = TestUtils.findJsonResources("data/chronodawn/advancement/");
        advFiles.addAll(TestUtils.findJsonResources("data/chronodawn/advancements/"));
        if (advFiles.isEmpty()) return List.of();

        Set<String> entityIds = TestUtils.getEntityIds();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String advPath : advFiles) {
            String advName = extractAdvancementName(advPath);
            Map<String, Object> adv = TestUtils.loadJsonResource(advPath);
            if (adv == null) continue;

            Set<String> entityRefs = extractEntityReferences(adv);
            for (String ref : entityRefs) {
                tests.add(DynamicTest.dynamicTest(
                    "advancement_entity_" + advName + "_" + ref.replace("chronodawn:", ""),
                    () -> assertTrue(
                        entityIds.contains(ref),
                        "Advancement '" + advName + "' references unknown entity: " + ref
                    )
                ));
            }
        }

        return tests;
    }

    private String extractAdvancementName(String path) {
        // Remove prefix and .json, replace / with _
        String name = path;
        name = name.replace("data/chronodawn/advancement/", "");
        name = name.replace("data/chronodawn/advancements/", "");
        name = name.replace(".json", "");
        name = name.replace("/", "_");
        return name;
    }

    /**
     * Extract chronodawn: item references from advancement JSON.
     * Looks in:
     * - display.icon.id / display.icon.item
     * - criteria.*.conditions.items[].items[]
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractItemReferences(Map<String, Object> adv) {
        Set<String> refs = new java.util.HashSet<>();

        // display.icon.id or display.icon.item
        Object display = adv.get("display");
        if (display instanceof Map) {
            Object icon = ((Map<String, Object>) display).get("icon");
            if (icon instanceof Map) {
                Map<String, Object> iconMap = (Map<String, Object>) icon;
                addIfChronodawn(refs, (String) iconMap.get("id"));
                addIfChronodawn(refs, (String) iconMap.get("item"));
            }
        }

        // criteria.*.conditions.items
        Object criteria = adv.get("criteria");
        if (criteria instanceof Map) {
            for (Object criterion : ((Map<String, Object>) criteria).values()) {
                if (!(criterion instanceof Map)) continue;
                Object conditions = ((Map<String, Object>) criterion).get("conditions");
                if (!(conditions instanceof Map)) continue;
                extractItemsFromConditions((Map<String, Object>) conditions, refs);
            }
        }

        return refs;
    }

    @SuppressWarnings("unchecked")
    private void extractItemsFromConditions(Map<String, Object> conditions, Set<String> refs) {
        // items: [{items: ["chronodawn:..."]}] or items: ["chronodawn:..."]
        Object items = conditions.get("items");
        if (items instanceof List) {
            for (Object item : (List<Object>) items) {
                if (item instanceof String) {
                    addIfChronodawn(refs, (String) item);
                } else if (item instanceof Map) {
                    Object innerItems = ((Map<String, Object>) item).get("items");
                    if (innerItems instanceof List) {
                        for (Object innerItem : (List<Object>) innerItems) {
                            if (innerItem instanceof String) {
                                addIfChronodawn(refs, (String) innerItem);
                            }
                        }
                    }
                }
            }
        }

        // item field (single item, may be String or Map)
        Object item = conditions.get("item");
        if (item instanceof String) {
            addIfChronodawn(refs, (String) item);
        }
    }

    /**
     * Extract chronodawn: entity references from advancement criteria.
     * Looks in criteria.*.conditions.entity.type
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractEntityReferences(Map<String, Object> adv) {
        Set<String> refs = new java.util.HashSet<>();

        Object criteria = adv.get("criteria");
        if (!(criteria instanceof Map)) return refs;

        for (Object criterion : ((Map<String, Object>) criteria).values()) {
            if (!(criterion instanceof Map)) continue;
            Object conditions = ((Map<String, Object>) criterion).get("conditions");
            if (!(conditions instanceof Map)) continue;

            // entity.type
            Object entity = ((Map<String, Object>) conditions).get("entity");
            if (entity instanceof Map) {
                addIfChronodawn(refs, (String) ((Map<String, Object>) entity).get("type"));
            }
            // Direct type field (some formats)
            addIfChronodawn(refs, (String) ((Map<String, Object>) conditions).get("type"));
        }

        return refs;
    }

    private void addIfChronodawn(Set<String> refs, String value) {
        if (value != null && value.startsWith("chronodawn:")) {
            refs.add(value);
        }
    }
}
