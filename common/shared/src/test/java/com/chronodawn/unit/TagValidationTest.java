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
 * Validates tag JSON resources for block/item reference consistency.
 * Checks that all chronodawn: references in tag files point to valid
 * registered blocks or items.
 */
public class TagValidationTest {

    @TestFactory
    Collection<DynamicTest> blockTagReferenceTests() {
        Set<String> blockIds = TestUtils.getBlockIds();
        Collection<DynamicTest> tests = new ArrayList<>();

        // chronodawn namespace block tags
        addTagTests(tests, "data/chronodawn/tags/block/", blockIds, "chronodawn_block_tag");
        // minecraft namespace block tags (our additions)
        addTagTests(tests, "data/minecraft/tags/block/", blockIds, "minecraft_block_tag");

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> itemTagReferenceTests() {
        Set<String> validIds = TestUtils.getAllItemAndBlockIds();
        Collection<DynamicTest> tests = new ArrayList<>();

        // chronodawn namespace item tags
        addTagTests(tests, "data/chronodawn/tags/item/", validIds, "chronodawn_item_tag");
        // minecraft namespace item tags (our additions)
        addTagTests(tests, "data/minecraft/tags/item/", validIds, "minecraft_item_tag");

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> worldgenBiomeTagReferenceTests() {
        List<String> biomeFiles = TestUtils.findJsonResources("data/chronodawn/worldgen/biome/");
        Set<String> biomeIds = new java.util.HashSet<>();
        for (String bf : biomeFiles) {
            String id = "chronodawn:" + bf.substring(bf.lastIndexOf('/') + 1).replace(".json", "");
            biomeIds.add(id);
        }

        List<String> tagFiles = TestUtils.findJsonResources("data/chronodawn/tags/worldgen/biome/");
        if (tagFiles.isEmpty()) return List.of();

        Collection<DynamicTest> tests = new ArrayList<>();
        for (String tagPath : tagFiles) {
            String tagName = tagPath.substring(tagPath.lastIndexOf('/') + 1).replace(".json", "");
            Map<String, Object> tag = TestUtils.loadJsonResource(tagPath);
            if (tag == null) continue;

            @SuppressWarnings("unchecked")
            List<String> values = (List<String>) tag.get("values");
            if (values == null) continue;

            for (String value : values) {
                if (value.startsWith("chronodawn:")) {
                    tests.add(DynamicTest.dynamicTest(
                        "biome_tag_" + tagName + "_" + value.replace("chronodawn:", ""),
                        () -> assertTrue(
                            biomeIds.contains(value),
                            "Biome tag '" + tagName + "' references unknown biome: " + value
                        )
                    ));
                }
            }
        }

        return tests;
    }

    @SuppressWarnings("unchecked")
    private void addTagTests(Collection<DynamicTest> tests, String resourcePrefix,
                             Set<String> validIds, String testPrefix) {
        List<String> tagFiles = TestUtils.findJsonResources(resourcePrefix);

        for (String tagPath : tagFiles) {
            String tagName = tagPath.replace(resourcePrefix, "").replace(".json", "").replace("/", "_");
            Map<String, Object> tag = TestUtils.loadJsonResource(tagPath);
            if (tag == null) continue;

            List<String> values = (List<String>) tag.get("values");
            if (values == null) continue;

            for (String value : values) {
                if (value.startsWith("chronodawn:")) {
                    tests.add(DynamicTest.dynamicTest(
                        testPrefix + "_" + tagName + "_" + value.replace("chronodawn:", ""),
                        () -> assertTrue(
                            validIds.contains(value),
                            "Tag '" + tagName + "' references unknown ID: " + value
                        )
                    ));
                }
            }
        }
    }
}
