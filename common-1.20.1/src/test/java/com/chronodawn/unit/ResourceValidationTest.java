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
 * JUnit 5 resource validation tests for Minecraft 1.20.1.
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

    // Blocks that intentionally don't have loot tables
    private static final Set<String> LOOT_TABLE_EXCLUDED_BLOCKS = Set.of(
        "CHRONO_DAWN_PORTAL",           // Portal block, not obtainable
        "BOSS_ROOM_BOUNDARY_MARKER",    // Structure marker, uses .noLootTable()
        "DECORATIVE_WATER",             // Placeholder converted to vanilla water
        "TEMPORAL_PARTICLE_EMITTER",    // Invisible, indestructible effect block
        "REVERSING_TIME_SANDSTONE",     // Self-restoring block, no drops by design
        "BOSS_ROOM_DOOR",               // Iron door equivalent, no drop
        "ENTROPY_CRYPT_TRAPDOOR",       // Iron trapdoor equivalent, boss trigger
        "UNSTABLE_FUNGUS"               // Special effect block, no drop by design
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

    /**
     * Validates that all wood-type blocks and melon blocks are included
     * in the mineable/axe tag for proper axe mining speed.
     */
    @TestFactory
    Collection<DynamicTest> mineableAxeTagTests() {
        Set<String> axeTagValues = loadTagValues("data/minecraft/tags/blocks/mineable/axe.json");
        if (axeTagValues.isEmpty()) {
            return Collections.singletonList(
                DynamicTest.dynamicTest("mineable_axe_tag_load", () -> {
                    throw new AssertionError("Failed to load mineable/axe.json tag file");
                })
            );
        }

        List<String> blockFieldNames = getFieldNames("ModBlocks.java");
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String fieldName : blockFieldNames) {
            if (!shouldBeInMineableAxe(fieldName)) continue;
            String blockId = ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
            tests.add(DynamicTest.dynamicTest("mineable_axe_contains_" + blockId, () -> {
                assertTrue(
                    axeTagValues.contains("chronodawn:" + blockId),
                    "Block 'chronodawn:" + blockId + "' should be in mineable/axe tag"
                );
            }));
        }

        return tests;
    }

    /**
     * Validates that all blocks have a loot table file, except those
     * intentionally excluded (portals, structure markers, etc.).
     *
     * Note: 1.20.1 uses 'loot_tables' (plural) directory.
     */
    @TestFactory
    Collection<DynamicTest> lootTableExistenceTests() {
        List<String> blockFieldNames = getFieldNames("ModBlocks.java");
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String fieldName : blockFieldNames) {
            if (fieldName.startsWith("POTTED_")) continue;
            if (LOOT_TABLE_EXCLUDED_BLOCKS.contains(fieldName)) continue;
            String blockId = ID_OVERRIDES.getOrDefault(fieldName, fieldName.toLowerCase());
            // 1.20.1 uses 'loot_tables' (plural)
            String resourcePath = "data/chronodawn/loot_tables/blocks/" + blockId + ".json";
            tests.add(DynamicTest.dynamicTest("loot_table_exists_" + blockId, () -> {
                assertNotNull(
                    getClass().getClassLoader().getResource(resourcePath),
                    "Missing loot table file: " + resourcePath
                );
            }));
        }

        return tests;
    }

    /**
     * Validates that planks recipes use tag-based ingredients so that
     * all log variants (including stripped) can be crafted into planks.
     */
    @SuppressWarnings("unchecked")
    @TestFactory
    Collection<DynamicTest> planksRecipeTagTests() {
        String[] woodFamilies = {"time_wood", "dark_time_wood", "ancient_time_wood"};
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String family : woodFamilies) {
            String recipePath = "data/chronodawn/recipes/" + family + "_planks.json";
            tests.add(DynamicTest.dynamicTest("planks_recipe_uses_tag_" + family, () -> {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(recipePath)) {
                    assertNotNull(is, "Missing recipe file: " + recipePath);
                    Gson gson = new Gson();
                    Map<String, Object> recipe = gson.fromJson(
                        new InputStreamReader(is, StandardCharsets.UTF_8),
                        new TypeToken<Map<String, Object>>() {}.getType()
                    );
                    List<Object> ingredients = (List<Object>) recipe.get("ingredients");
                    assertNotNull(ingredients, "Recipe has no ingredients: " + recipePath);
                    assertTrue(!ingredients.isEmpty(), "Recipe has empty ingredients: " + recipePath);
                    Object ingredient = ingredients.get(0);
                    // The ingredient is a Map with a "tag" key when using tag-based ingredients
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ingredientMap = (Map<String, Object>) ingredient;
                    String tagValue = (String) ingredientMap.get("tag");
                    assertNotNull(tagValue,
                        "Planks recipe for " + family + " should use tag-based ingredient");
                    assertTrue(
                        tagValue.equals("chronodawn:" + family + "_logs"),
                        "Planks recipe for " + family + " should use tag 'chronodawn:" +
                        family + "_logs' but got: " + tagValue
                    );
                }
            }));
        }

        return tests;
    }

    /**
     * Determines if a block should be in the mineable/axe tag based on its field name.
     */
    private static boolean shouldBeInMineableAxe(String fieldName) {
        // Wood family blocks (except leaves, saplings, and potted variants)
        if (fieldName.contains("TIME_WOOD")) {
            if (fieldName.endsWith("_LEAVES")) return false;
            if (fieldName.endsWith("_SAPLING")) return false;
            if (fieldName.startsWith("POTTED_")) return false;
            return true;
        }
        // Melon block (not stems)
        if (fieldName.equals("CHRONO_MELON")) return true;
        return false;
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
                "Run tests via Gradle: ./gradlew :common-1.20.1:test -Ptarget_mc_version=1.20.1"
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

    /**
     * Load tag values from a tag JSON file on the classpath.
     * Returns the set of block IDs (e.g., "chronodawn:time_wood_log").
     */
    @SuppressWarnings("unchecked")
    private Set<String> loadTagValues(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) return Collections.emptySet();
            Gson gson = new Gson();
            Map<String, Object> tagData = gson.fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8),
                new TypeToken<Map<String, Object>>() {}.getType()
            );
            List<String> values = (List<String>) tagData.get("values");
            if (values == null) return Collections.emptySet();
            return new HashSet<>(values);
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }
}
