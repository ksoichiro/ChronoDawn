package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared recipe-load test generator used across all Minecraft versions.
 *
 * Verifies that recipe JSON files are actually loaded into the server's
 * RecipeManager. This catches recipes that fail to load silently, e.g. due
 * to using an ingredient JSON shape that is not valid for the target
 * Minecraft version (bare-string ingredients only work from 1.21.2+, while
 * 1.21.1 and earlier require the {"item": "..."} object form).
 */
public final class RecipeLoadTests {

    private RecipeLoadTests() {
        // Utility class
    }

    /**
     * Recipe IDs (under the chronodawn namespace) that must be present in
     * the server's RecipeManager after world load.
     */
    private static final List<String> CHRONITE_RECIPE_IDS = List.of(
        "chronite_block",
        "chronite_from_block",
        "clock_from_chronite",
        "time_arrow_from_chronite",
        "time_compass"
    );

    /**
     * Generates tests verifying each Chronite-related recipe is loaded by
     * the server's RecipeManager.
     */
    public static <T> List<T> generateChroniteRecipeLoadTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (String recipeId : CHRONITE_RECIPE_IDS) {
            String testName = "recipe_loaded_" + recipeId;
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    var id = CompatResourceLocation.create("chronodawn", recipeId);
                    var recipeManager = helper.getLevel().getRecipeManager();
                    if (recipeManager.byKey(id).isPresent()) {
                        helper.succeed();
                    } else {
                        CompatGameTestHelper.fail(helper, "Recipe not loaded: chronodawn:" + recipeId);
                    }
                });
            }));
        }
        return tests;
    }
}
