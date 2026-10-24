package com.chronodawn.unit;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.chronodawn.unit.FragmentSavingTest.ingredientId;
import static com.chronodawn.unit.FragmentSavingTest.loadData;
import static com.chronodawn.unit.FragmentSavingTest.resultId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins how players obtain the Chronicle: granted once by the dimension entry
 * advancement reward, and craftable inside Chrono Dawn if it is lost.
 */
public class ChronicleAcquisitionTest {

    private static final String CHRONICLE = "chronodawn:chronicle_book";
    private static final String REWARD_LOOT = "chronodawn:advancement_reward/chronicle_book";

    @Test
    @SuppressWarnings("unchecked")
    void dimensionEntryRewardsChronicleLoot() {
        Map<String, Object> advancement = loadData(
            "advancement/story/us1/dimension_entry.json",
            "advancements/story/us1/dimension_entry.json");
        assertNotNull(advancement, "dimension_entry advancement is not on the classpath");

        Map<String, Object> rewards = (Map<String, Object>) advancement.get("rewards");
        assertNotNull(rewards, "dimension_entry has no rewards");
        assertEquals(List.of(REWARD_LOOT), rewards.get("loot"), "rewards.loot");
    }

    @Test
    @SuppressWarnings("unchecked")
    void chronicleRewardLootTableGivesOneBook() {
        Map<String, Object> table = loadData(
            "loot_table/advancement_reward/chronicle_book.json",
            "loot_tables/advancement_reward/chronicle_book.json");
        assertNotNull(table, "chronicle_book reward loot table is not on the classpath");
        assertEquals("minecraft:advancement_reward", table.get("type"), "type");

        List<Object> pools = (List<Object>) table.get("pools");
        assertEquals(1, pools.size(), "pools");
        Map<String, Object> pool = (Map<String, Object>) pools.get(0);
        assertEquals(1.0, ((Number) pool.get("rolls")).doubleValue(), "rolls");

        List<Object> entries = (List<Object>) pool.get("entries");
        assertEquals(1, entries.size(), "entries");
        Map<String, Object> entry = (Map<String, Object>) entries.get(0);
        assertEquals("minecraft:item", entry.get("type"), "entry type");
        assertEquals(CHRONICLE, entry.get("name"), "entry name");
        assertFalse(entry.containsKey("functions") || entry.containsKey("modifier"),
            "the reward must be exactly one book");
    }

    // Vertical, so it does not collide with the horizontal Time Bread row.
    @Test
    @SuppressWarnings("unchecked")
    void paperFromTimeWheatIsVerticalColumn() {
        Map<String, Object> recipe = recipe("paper_from_time_wheat");
        assertEquals(List.of("W", "W", "W"), recipe.get("pattern"), "pattern");
        Map<String, Object> key = (Map<String, Object>) recipe.get("key");
        assertEquals("chronodawn:time_wheat", ingredientId(key.get("W")), "W");
        assertEquals("minecraft:paper", resultId(recipe), "result");
        assertEquals(3.0, resultCount(recipe), "count");
    }

    @Test
    void timeBreadStaysHorizontal() {
        assertEquals(List.of("WWW"), recipe("time_bread").get("pattern"), "pattern");
    }

    @Test
    @SuppressWarnings("unchecked")
    void chronicleRecipeIsBookPlusTimeCrystal() {
        Map<String, Object> recipe = recipe("chronicle_book");
        assertEquals("minecraft:crafting_shapeless", recipe.get("type"), "type");
        List<String> ids = new ArrayList<>();
        for (Object ingredient : (List<Object>) recipe.get("ingredients")) {
            ids.add(ingredientId(ingredient));
        }
        assertEquals(2, ids.size(), "ingredients " + ids);
        assertTrue(ids.contains("minecraft:book"), "book in " + ids);
        // 1.20.1 names the item directly, 1.21.1+ uses the c: tag.
        assertTrue(ids.contains("chronodawn:time_crystal") || ids.contains("#c:gems/time_crystal"),
            "Time Crystal in " + ids);
        assertEquals(CHRONICLE, resultId(recipe), "result");
        assertEquals(1.0, resultCount(recipe), "count");
    }

    private record Unlock(String recipe, String triggerItem) {}

    private static final List<Unlock> UNLOCKS = List.of(
        new Unlock("paper_from_time_wheat", "chronodawn:time_wheat"),
        new Unlock("chronicle_book", "chronodawn:time_crystal")
    );

    @TestFactory
    @SuppressWarnings("unchecked")
    Collection<DynamicTest> unlockAdvancementsRewardRecipes() {
        Collection<DynamicTest> tests = new ArrayList<>();
        for (Unlock unlock : UNLOCKS) {
            String id = "chronodawn:" + unlock.recipe();
            tests.add(DynamicTest.dynamicTest(unlock.recipe(), () -> {
                Map<String, Object> advancement = loadData(
                    "advancement/recipes/misc/" + unlock.recipe() + ".json",
                    "advancements/recipes/misc/" + unlock.recipe() + ".json");
                assertNotNull(advancement, "Missing unlock advancement for " + unlock.recipe());

                Map<String, Object> rewards = (Map<String, Object>) advancement.get("rewards");
                assertEquals(List.of(id), rewards.get("recipes"), "rewards.recipes");

                Map<String, Object> criteria = (Map<String, Object>) advancement.get("criteria");
                Map<String, Object> hasItem = (Map<String, Object>) criteria.get("has_item");
                Map<String, Object> itemConditions = (Map<String, Object>) hasItem.get("conditions");
                Map<String, Object> predicate = (Map<String, Object>) ((List<Object>) itemConditions.get("items")).get(0);
                assertTrue(((List<Object>) predicate.get("items")).contains(unlock.triggerItem()),
                    "has_item triggers on " + unlock.triggerItem());

                // 26.3 renamed the recipe_unlocked condition key to "recipes".
                Map<String, Object> hasRecipe = (Map<String, Object>) criteria.get("has_the_recipe");
                Map<String, Object> recipeConditions = (Map<String, Object>) hasRecipe.get("conditions");
                Object unlocked = recipeConditions.containsKey("recipe")
                    ? recipeConditions.get("recipe") : recipeConditions.get("recipes");
                assertEquals(id, unlocked, "has_the_recipe");
            }));
        }
        return tests;
    }

    @Test
    @SuppressWarnings("unchecked")
    void chronicleModelUsesOwnTexture() {
        Map<String, Object> model = TestUtils.loadJsonResource(
            "assets/chronodawn/models/item/chronicle_book.json");
        assertNotNull(model, "chronicle_book item model is not on the classpath");
        Map<String, Object> textures = (Map<String, Object>) model.get("textures");
        assertEquals("chronodawn:item/chronicle_book", textures.get("layer0"), "layer0");
        assertNotNull(getClass().getClassLoader().getResource(
            "assets/chronodawn/textures/item/chronicle_book.png"), "chronicle_book.png");
    }

    private static Map<String, Object> recipe(String name) {
        Map<String, Object> recipe = loadData("recipe/" + name + ".json", "recipes/" + name + ".json");
        assertNotNull(recipe, "Missing recipe " + name);
        return recipe;
    }

    @SuppressWarnings("unchecked")
    private static double resultCount(Map<String, Object> recipe) {
        return ((Number) ((Map<String, Object>) recipe.get("result")).get("count")).doubleValue();
    }
}
