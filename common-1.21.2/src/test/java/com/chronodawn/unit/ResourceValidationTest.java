package com.chronodawn.unit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 resource validation tests.
 *
 * These tests verify the existence of blockstate JSON, item model JSON,
 * and translation keys without requiring a Minecraft server environment.
 * Field names are extracted by parsing Java source files (avoiding class loading
 * of Minecraft-dependent registry classes).
 */
public class ResourceValidationTest {

    // Pattern to match: public static final RegistrySupplier<...> FIELD_NAME = ...
    private static final Pattern REGISTRY_FIELD_PATTERN = Pattern.compile(
        "public\\s+static\\s+final\\s+RegistrySupplier<[^>]+>\\s+(\\w+)\\s*="
    );

    // Known cases where field name doesn't match registry ID
    private static final Map<String, String> ID_OVERRIDES = Map.of(
        "FRUIT_OF_TIME_BLOCK", "fruit_of_time",
        "CHRONO_DAWN_BOAT", "chronodawn_boat",
        "CHRONO_DAWN_CHEST_BOAT", "chronodawn_chest_boat"
    );

    // Technical blocks/entities that don't need player-facing translations
    private static final Set<String> TRANSLATION_EXCLUDED_BLOCKS = Set.of(
        "CHRONO_DAWN_PORTAL"
    );
    private static final Set<String> TRANSLATION_EXCLUDED_ENTITIES = Set.of(
        "GEAR_PROJECTILE", "TIME_ARROW"
    );

    @TestFactory
    Collection<DynamicTest> blockstateExistenceTests() {
        List<String> fieldNames = getFieldNames("ModBlocks.java");
        Collection<DynamicTest> tests = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String expectedId = ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
            String resourcePath = "assets/chronodawn/blockstates/" + expectedId + ".json";
            tests.add(DynamicTest.dynamicTest("blockstate_exists_" + expectedId, () -> {
                assertNotNull(
                    getClass().getClassLoader().getResource(resourcePath),
                    "Missing blockstate file: " + resourcePath
                );
            }));
        }
        return tests;
    }

    @TestFactory
    Collection<DynamicTest> itemModelExistenceTests() {
        Set<String> blockFieldNames = new HashSet<>(getFieldNames("ModBlocks.java"));
        List<String> itemFieldNames = getFieldNames("ModItems.java");
        Collection<DynamicTest> tests = new ArrayList<>();
        for (String fieldName : itemFieldNames) {
            if (blockFieldNames.contains(fieldName)) continue;
            String expectedId = ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
            String resourcePath = "assets/chronodawn/models/item/" + expectedId + ".json";
            tests.add(DynamicTest.dynamicTest("item_model_exists_" + expectedId, () -> {
                assertNotNull(
                    getClass().getClassLoader().getResource(resourcePath),
                    "Missing item model file: " + resourcePath
                );
            }));
        }
        return tests;
    }

    @TestFactory
    Collection<DynamicTest> translationKeyTests() {
        Map<String, String> langMap = loadLangFile();
        if (langMap.isEmpty()) {
            return Collections.singletonList(
                DynamicTest.dynamicTest("translation_lang_file_load", () -> {
                    throw new AssertionError("Failed to load en_us.json lang file");
                })
            );
        }

        Collection<DynamicTest> tests = new ArrayList<>();
        Set<String> blockFieldNames = new HashSet<>(getFieldNames("ModBlocks.java"));

        // Items (block items use block.chronodawn.<id> as translation key)
        for (String fieldName : getFieldNames("ModItems.java")) {
            String expectedId = ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
            String translationKey = blockFieldNames.contains(fieldName)
                ? "block.chronodawn." + expectedId
                : "item.chronodawn." + expectedId;
            tests.add(DynamicTest.dynamicTest("translation_item_" + expectedId, () -> {
                assertTrue(
                    langMap.containsKey(translationKey),
                    "Missing translation key: " + translationKey
                );
            }));
        }

        // Blocks (exclude POTTED_* and technical blocks)
        for (String fieldName : getFieldNames("ModBlocks.java")) {
            if (fieldName.startsWith("POTTED_")) continue;
            if (TRANSLATION_EXCLUDED_BLOCKS.contains(fieldName)) continue;
            String expectedId = ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
            String translationKey = "block.chronodawn." + expectedId;
            tests.add(DynamicTest.dynamicTest("translation_block_" + expectedId, () -> {
                assertTrue(
                    langMap.containsKey(translationKey),
                    "Missing translation key: " + translationKey
                );
            }));
        }

        // Entities (exclude technical entities like projectiles)
        for (String fieldName : getFieldNames("ModEntities.java")) {
            if (TRANSLATION_EXCLUDED_ENTITIES.contains(fieldName)) continue;
            String expectedId = ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
            String translationKey = "entity.chronodawn." + expectedId;
            tests.add(DynamicTest.dynamicTest("translation_entity_" + expectedId, () -> {
                assertTrue(
                    langMap.containsKey(translationKey),
                    "Missing translation key: " + translationKey
                );
            }));
        }

        return tests;
    }

    // --- Utility methods ---

    /**
     * Parse a registry source file to extract RegistrySupplier field names.
     */
    private static List<String> getFieldNames(String fileName) {
        String sourceDir = System.getProperty("chronodawn.source.dir");
        if (sourceDir == null) {
            throw new IllegalStateException(
                "System property 'chronodawn.source.dir' not set. " +
                "Run tests via Gradle: ./gradlew :common-1.21.2:test"
            );
        }
        Path filePath = Paths.get(sourceDir, "com", "chronodawn", "registry", fileName);
        List<String> fieldNames = new ArrayList<>();
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            Matcher matcher = REGISTRY_FIELD_PATTERN.matcher(content);
            while (matcher.find()) {
                fieldNames.add(matcher.group(1));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read source file: " + filePath, e);
        }
        return fieldNames;
    }

    private Map<String, String> loadLangFile() {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("assets/chronodawn/lang/en_us.json")) {
            if (is == null) return Collections.emptyMap();
            Gson gson = new Gson();
            return gson.fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8),
                new TypeToken<Map<String, String>>() {}.getType()
            );
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
