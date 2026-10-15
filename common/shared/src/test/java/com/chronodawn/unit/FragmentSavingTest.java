package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins the numbers that let one Time Tyrant kill cover all four ultimate
 * artifacts through the Clockwork Mechanism recipes: at least 4 Fragments
 * per kill, and exactly 1 Fragment per alternative recipe.
 */
public class FragmentSavingTest {

    private static final String FRAGMENT = "chronodawn:fragment_of_stasis_core";

    @Test
    void timeTyrantDropsFourToSixFragments() {
        Map<String, Object> table = loadData(
            "loot_table/entities/time_tyrant.json",
            "loot_tables/entities/time_tyrant.json");
        assertNotNull(table, "time_tyrant loot table is not on the classpath");

        Map<String, Object> count = findFragmentSetCount(table);
        assertNotNull(count, "No set_count function on the Fragment entry");
        assertEquals(4.0, ((Number) count.get("min")).doubleValue(), "Fragment min");
        assertEquals(6.0, ((Number) count.get("max")).doubleValue(), "Fragment max");
    }

    private static final String MECHANISM = "chronodawn:clockwork_mechanism";

    private record Alternative(String recipe, String result, int mechanisms) {}

    private static final List<Alternative> ALTERNATIVES = List.of(
        new Alternative("chronoblade_from_clockwork_mechanism", "chronodawn:chronoblade", 3),
        new Alternative("time_tyrant_mail_from_clockwork_mechanism", "chronodawn:time_tyrant_mail", 5),
        new Alternative("echoing_time_boots_from_clockwork_mechanism", "chronodawn:echoing_time_boots", 1),
        new Alternative("unstable_pocket_watch_from_clockwork_mechanism", "chronodawn:unstable_pocket_watch", 2)
    );

    @TestFactory
    Collection<DynamicTest> alternativeRecipesUseOneFragment() {
        Collection<DynamicTest> tests = new ArrayList<>();
        for (Alternative alt : ALTERNATIVES) {
            tests.add(DynamicTest.dynamicTest(alt.recipe(), () -> {
                Map<String, Object> recipe = loadData(
                    "recipe/" + alt.recipe() + ".json", "recipes/" + alt.recipe() + ".json");
                assertNotNull(recipe, "Missing recipe " + alt.recipe());
                assertEquals(alt.result(), resultId(recipe), "result");
                assertEquals(1, countIngredient(recipe, FRAGMENT), "Fragments");
                assertEquals(alt.mechanisms(), countIngredient(recipe, MECHANISM), "Mechanisms");
            }));
        }
        return tests;
    }

    @Test
    @SuppressWarnings("unchecked")
    void mechanismRecipeUsesFourGearsOneClockstoneFourCrystals() {
        Map<String, Object> recipe = loadData("recipe/clockwork_mechanism.json", "recipes/clockwork_mechanism.json");
        assertNotNull(recipe, "Missing recipe clockwork_mechanism");
        assertEquals(MECHANISM, resultId(recipe), "result");
        assertEquals(1.0, ((Number) ((Map<String, Object>) recipe.get("result")).get("count")).doubleValue(), "count");
        assertEquals(4, countIngredient(recipe, "chronodawn:ancient_gear"), "Ancient Gears");

        // 1.20.1 names the item directly, 1.21.1+ uses the c: tag.
        int clockstone = countIngredient(recipe, "chronodawn:enhanced_clockstone")
            + countIngredient(recipe, "#c:ingots/enhanced_clockstone");
        int crystal = countIngredient(recipe, "chronodawn:time_crystal")
            + countIngredient(recipe, "#c:gems/time_crystal");
        assertEquals(1, clockstone, "Enhanced Clockstone");
        assertEquals(4, crystal, "Time Crystals");
    }

    // 1.20.1 uses plural data directories, 1.21.1+ uses singular ones.
    static Map<String, Object> loadData(String singularPath, String pluralPath) {
        Map<String, Object> json = TestUtils.loadJsonResource("data/chronodawn/" + singularPath);
        if (json == null) {
            json = TestUtils.loadJsonResource("data/chronodawn/" + pluralPath);
        }
        return json;
    }

    // 26.3 lists entry functions under "modifier" with a "type" key. Older
    // versions use "functions" with a "function" key.
    @SuppressWarnings("unchecked")
    private static Map<String, Object> findFragmentSetCount(Map<String, Object> table) {
        for (Object poolObj : (List<Object>) table.get("pools")) {
            Map<String, Object> pool = (Map<String, Object>) poolObj;
            for (Object entryObj : (List<Object>) pool.get("entries")) {
                Map<String, Object> entry = (Map<String, Object>) entryObj;
                if (!FRAGMENT.equals(entry.get("name"))) continue;
                Object fns = entry.containsKey("functions") ? entry.get("functions") : entry.get("modifier");
                for (Object fnObj : (List<Object>) fns) {
                    Map<String, Object> fn = (Map<String, Object>) fnObj;
                    Object type = fn.containsKey("function") ? fn.get("function") : fn.get("type");
                    if ("minecraft:set_count".equals(type)) {
                        return (Map<String, Object>) fn.get("count");
                    }
                }
            }
        }
        return null;
    }

    // Recipe key values are a bare string in 1.21.2+, and {"item": ...} or
    // {"tag": ...} in 1.21.1 and 1.20.1. Tags are returned with a leading '#'.
    @SuppressWarnings("unchecked")
    static String ingredientId(Object keyValue) {
        if (keyValue instanceof String s) return s;
        Map<String, Object> m = (Map<String, Object>) keyValue;
        if (m.containsKey("item")) return (String) m.get("item");
        return "#" + m.get("tag");
    }

    @SuppressWarnings("unchecked")
    static int countIngredient(Map<String, Object> recipe, String id) {
        Map<String, Object> key = (Map<String, Object>) recipe.get("key");
        int n = 0;
        for (Object rowObj : (List<Object>) recipe.get("pattern")) {
            for (char c : ((String) rowObj).toCharArray()) {
                if (c == ' ') continue;
                if (id.equals(ingredientId(key.get(String.valueOf(c))))) n++;
            }
        }
        return n;
    }

    // 1.20.1 names the result "item", 1.21.1+ names it "id".
    @SuppressWarnings("unchecked")
    static String resultId(Map<String, Object> recipe) {
        Map<String, Object> result = (Map<String, Object>) recipe.get("result");
        return (String) (result.containsKey("id") ? result.get("id") : result.get("item"));
    }
}
