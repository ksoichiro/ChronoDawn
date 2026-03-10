package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates creative tab completeness.
 * Checks that all registered items are either included in the creative tab
 * via populateCreativeTab() or documented as intentionally excluded.
 */
public class CreativeTabCompletenessTest {

    // Items intentionally excluded from creative tab
    private static final Set<String> CREATIVE_TAB_EXCLUDED_ITEMS = Set.of(
        // Structure/technical blocks - not obtainable in normal gameplay
        "BOSS_ROOM_BOUNDARY_MARKER",
        "BOSS_ROOM_DOOR",
        "TEMPORAL_PARTICLE_EMITTER",
        "ENTROPY_CRYPT_TRAPDOOR",
        "DECORATIVE_WATER_BUCKET",
        // Boss spawn eggs - command-only
        "TIME_GUARDIAN_SPAWN_EGG",
        "TIME_TYRANT_SPAWN_EGG",
        "CHRONOS_WARDEN_SPAWN_EGG",
        "CLOCKWORK_COLOSSUS_SPAWN_EGG",
        "ENTROPY_KEEPER_SPAWN_EGG",
        "TEMPORAL_PHANTOM_SPAWN_EGG",
        // Special items with non-standard registration
        "TIME_COMPASS"
    );

    // Pattern to match output.accept(FIELD_NAME.get())
    private static final Pattern ACCEPT_PATTERN = Pattern.compile(
        "output\\.accept\\((\\w+)\\.get\\(\\)"
    );

    @TestFactory
    Collection<DynamicTest> allItemsInCreativeTabOrExcludedTests() {
        List<String> itemFields = TestUtils.getFieldNames("ModItems.java");
        Set<String> tabItems = getCreativeTabItems();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String field : itemFields) {
            tests.add(DynamicTest.dynamicTest(
                "creative_tab_or_excluded_" + field.toLowerCase(),
                () -> assertTrue(
                    tabItems.contains(field) || CREATIVE_TAB_EXCLUDED_ITEMS.contains(field),
                    "Item '" + field + "' is not in creative tab and not in exclusion list. " +
                    "Add it to populateCreativeTab() or to CREATIVE_TAB_EXCLUDED_ITEMS."
                )
            ));
        }

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> creativeTabItemsExistInRegistryTests() {
        Set<String> tabItems = getCreativeTabItems();
        List<String> itemFields = TestUtils.getFieldNames("ModItems.java");
        Set<String> itemFieldSet = new HashSet<>(itemFields);

        Collection<DynamicTest> tests = new ArrayList<>();

        for (String tabItem : tabItems) {
            tests.add(DynamicTest.dynamicTest(
                "creative_tab_valid_item_" + tabItem.toLowerCase(),
                () -> assertTrue(
                    itemFieldSet.contains(tabItem),
                    "Creative tab references unknown item: " + tabItem
                )
            ));
        }

        return tests;
    }

    /**
     * Parse ModItems.java to extract all items referenced in populateCreativeTab().
     */
    private Set<String> getCreativeTabItems() {
        String sourceDir = TestUtils.getSourceDir();
        Path filePath = Paths.get(sourceDir, "com", "chronodawn", "registry", "ModItems.java");
        Set<String> items = new HashSet<>();

        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            // Extract the populateCreativeTab method body
            int methodStart = content.indexOf("populateCreativeTab");
            if (methodStart == -1) return items;

            // Find method body (from first { to matching })
            String methodBody = extractMethodBody(content, methodStart);
            if (methodBody == null) return items;

            Matcher matcher = ACCEPT_PATTERN.matcher(methodBody);
            while (matcher.find()) {
                items.add(matcher.group(1));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ModItems.java: " + filePath, e);
        }

        return items;
    }

    /**
     * Extract a method body starting from the given position.
     */
    private String extractMethodBody(String content, int startPos) {
        int braceStart = content.indexOf('{', startPos);
        if (braceStart == -1) return null;

        int depth = 1;
        int pos = braceStart + 1;
        while (pos < content.length() && depth > 0) {
            char c = content.charAt(pos);
            if (c == '{') depth++;
            else if (c == '}') depth--;
            pos++;
        }
        return content.substring(braceStart, pos);
    }
}
