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
 * Validates recipe JSON resources for item reference consistency.
 * Checks that all chronodawn: item IDs in recipes reference valid registered items.
 */
public class RecipeValidationTest {

    @TestFactory
    Collection<DynamicTest> recipeItemReferenceTests() {
        List<String> recipeFiles = TestUtils.findJsonResources("data/chronodawn/recipe/");
        if (recipeFiles.isEmpty()) {
            return List.of(DynamicTest.dynamicTest("recipe_files_found", () -> {
                // No recipe files found for this version - skip
            }));
        }

        Set<String> validIds = TestUtils.getAllItemAndBlockIds();
        Collection<DynamicTest> tests = new ArrayList<>();

        for (String recipePath : recipeFiles) {
            String recipeName = recipePath.substring(recipePath.lastIndexOf('/') + 1).replace(".json", "");
            Map<String, Object> recipe = TestUtils.loadJsonResource(recipePath);
            if (recipe == null) continue;

            // Check result
            Set<String> resultRefs = extractResultReferences(recipe);
            for (String ref : resultRefs) {
                tests.add(DynamicTest.dynamicTest(
                    "recipe_result_" + recipeName + "_" + ref.replace("chronodawn:", ""),
                    () -> assertTrue(
                        validIds.contains(ref),
                        "Recipe '" + recipeName + "' result references unknown item: " + ref
                    )
                ));
            }

            // Check ingredients (key, ingredients)
            Set<String> ingredientRefs = extractIngredientReferences(recipe);
            for (String ref : ingredientRefs) {
                tests.add(DynamicTest.dynamicTest(
                    "recipe_ingredient_" + recipeName + "_" + ref.replace("chronodawn:", ""),
                    () -> assertTrue(
                        validIds.contains(ref),
                        "Recipe '" + recipeName + "' ingredient references unknown item: " + ref
                    )
                ));
            }
        }

        return tests;
    }

    /**
     * Extract chronodawn: item references from recipe result.
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractResultReferences(Map<String, Object> recipe) {
        Set<String> refs = new java.util.HashSet<>();

        // 1.21.2+ format: {"result": {"id": "chronodawn:...", "count": N}}
        Object result = recipe.get("result");
        if (result instanceof Map) {
            String id = (String) ((Map<String, Object>) result).get("id");
            if (id != null && id.startsWith("chronodawn:")) {
                refs.add(id);
            }
        }
        // 1.20.1/1.21.1 format: {"result": {"item": "chronodawn:...", "count": N}}
        // or {"result": "chronodawn:..."}
        if (result instanceof Map) {
            String item = (String) ((Map<String, Object>) result).get("item");
            if (item != null && item.startsWith("chronodawn:")) {
                refs.add(item);
            }
        } else if (result instanceof String && ((String) result).startsWith("chronodawn:")) {
            refs.add((String) result);
        }

        return refs;
    }

    /**
     * Extract chronodawn: item references from recipe ingredients.
     * Handles multiple recipe formats:
     * - shaped: "key" map with values as item strings or objects
     * - shapeless: "ingredients" list
     * - smelting/smoking/blasting: "ingredient" single value
     * - stonecutting: "ingredient" single value
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractIngredientReferences(Map<String, Object> recipe) {
        Set<String> refs = new java.util.HashSet<>();

        // Shaped recipe: "key" map
        Object key = recipe.get("key");
        if (key instanceof Map) {
            for (Object value : ((Map<String, Object>) key).values()) {
                refs.addAll(extractItemFromIngredient(value));
            }
        }

        // Shapeless recipe: "ingredients" list
        Object ingredients = recipe.get("ingredients");
        if (ingredients instanceof List) {
            for (Object ingredient : (List<Object>) ingredients) {
                refs.addAll(extractItemFromIngredient(ingredient));
            }
        }

        // Smelting/smoking/blasting/stonecutting: "ingredient" single value
        Object ingredient = recipe.get("ingredient");
        if (ingredient != null) {
            refs.addAll(extractItemFromIngredient(ingredient));
        }

        return refs;
    }

    /**
     * Extract chronodawn: item ID from an ingredient value.
     * Handles:
     * - String: "chronodawn:item_id" (1.21.2+)
     * - String: "#chronodawn:tag" (tag reference, ignored)
     * - Map: {"item": "chronodawn:item_id"} (1.20.1/1.21.1)
     * - List: array of alternatives
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractItemFromIngredient(Object ingredient) {
        Set<String> refs = new java.util.HashSet<>();

        if (ingredient instanceof String) {
            String s = (String) ingredient;
            if (s.startsWith("chronodawn:")) {
                refs.add(s);
            }
            // Skip tag references (#chronodawn:...)
        } else if (ingredient instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) ingredient;
            String item = (String) map.get("item");
            if (item != null && item.startsWith("chronodawn:")) {
                refs.add(item);
            }
        } else if (ingredient instanceof List) {
            for (Object alt : (List<Object>) ingredient) {
                refs.addAll(extractItemFromIngredient(alt));
            }
        }

        return refs;
    }
}
