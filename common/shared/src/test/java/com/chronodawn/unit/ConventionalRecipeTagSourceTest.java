package com.chronodawn.unit;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards modern recipes against regressing to exact Chrono Dawn material inputs. */
class ConventionalRecipeTagSourceTest {
    private static final Gson GSON = new Gson();
    private static final Map<String, String> MATERIAL_TAGS = Map.of(
        "clockstone", "c:ingots/clockstone",
        "clockstone_block", "c:storage_blocks/clockstone",
        "enhanced_clockstone", "c:ingots/enhanced_clockstone",
        "time_crystal", "c:gems/time_crystal",
        "time_crystal_block", "c:storage_blocks/time_crystal",
        "entropy_crystal", "c:gems/entropy_crystal",
        "raw_temporal_amber", "c:raw_materials/temporal_amber"
    );

    @Test
    void modernMaterialRecipeInputsUseConventionalTags() throws IOException {
        Path recipeDirectory = Paths.get(
            TestUtils.getProjectRoot(),
            "common", "1.21.1", "src", "main", "resources", "data", "chronodawn", "recipe"
        );
        List<String> recipes;
        try (Stream<Path> paths = Files.walk(recipeDirectory)) {
            recipes = paths.filter(path -> path.toString().endsWith(".json"))
                .map(path -> read(path))
                .toList();
        }

        for (Map.Entry<String, String> material : MATERIAL_TAGS.entrySet()) {
            String exactIngredient = "\"item\": \"chronodawn:" + material.getKey() + "\"";
            String conventionalTag = "\"tag\": \"" + material.getValue() + "\"";
            assertFalse(
                recipes.stream().anyMatch(recipe -> recipe.contains(exactIngredient)),
                "Modern recipe still uses exact material ingredient: " + material.getKey()
            );
            assertTrue(
                recipes.stream().anyMatch(recipe -> recipe.contains(conventionalTag)),
                "Modern recipes do not use conventional tag: " + material.getValue()
            );
        }
    }

    @Test
    void newerRecipeFormatUsesConventionalTagsForMaterialInputs() throws IOException {
        Path recipeDirectory = Paths.get(
            TestUtils.getProjectRoot(),
            "common", "shared-1.21.2+", "src", "main", "resources", "data", "chronodawn", "recipe"
        );

        List<String> recipes;
        try (Stream<Path> paths = Files.walk(recipeDirectory)) {
            recipes = paths.filter(path -> path.toString().endsWith(".json"))
                .map(ConventionalRecipeTagSourceTest::read)
                .toList();
        }

        for (Map.Entry<String, String> material : MATERIAL_TAGS.entrySet()) {
            assertFalse(
                recipes.stream().anyMatch(recipe -> containsExactIngredient(
                    GSON.fromJson(recipe, Object.class), "chronodawn:" + material.getKey()
                )),
                "Newer recipe format still uses exact material ingredient: " + material.getKey()
            );
            assertTrue(
                recipes.stream().anyMatch(recipe -> recipe.contains("#" + material.getValue())),
                "Newer recipe format does not use conventional tag: " + material.getValue()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean containsExactIngredient(Object recipe, String exactItem) {
        if (!(recipe instanceof Map<?, ?> map)) {
            return false;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (Set.of("key", "ingredients", "ingredient").contains(entry.getKey())) {
                if (containsExactIngredientValue(entry.getValue(), exactItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean containsExactIngredientValue(Object value, String exactItem) {
        if (value instanceof String string) {
            return string.equals(exactItem);
        }
        if (value instanceof Map<?, ?> map) {
            return map.values().stream().anyMatch(child -> containsExactIngredientValue(child, exactItem));
        }
        if (value instanceof List<?> list) {
            return list.stream().anyMatch(child -> containsExactIngredientValue(child, exactItem));
        }
        return false;
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read recipe: " + path, e);
        }
    }
}
