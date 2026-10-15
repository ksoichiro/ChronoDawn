package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatRecipeManager;
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
        return generateRecipeLoadTests(CHRONITE_RECIPE_IDS, factory);
    }

    /**
     * Clockwork Mechanism recipes: the Mechanism itself and the alternative
     * ultimate artifact recipes that take 1 Fragment of Stasis Core.
     */
    private static final List<String> CLOCKWORK_MECHANISM_RECIPE_IDS = List.of(
        "clockwork_mechanism",
        "chronoblade_from_clockwork_mechanism",
        "time_tyrant_mail_from_clockwork_mechanism",
        "echoing_time_boots_from_clockwork_mechanism",
        "unstable_pocket_watch_from_clockwork_mechanism"
    );

    /**
     * Generates tests verifying each Clockwork Mechanism recipe is loaded by
     * the server's RecipeManager.
     */
    public static <T> List<T> generateClockworkMechanismRecipeLoadTests(MobBehaviorTests.TestFactory<T> factory) {
        return generateRecipeLoadTests(CLOCKWORK_MECHANISM_RECIPE_IDS, factory);
    }

    private static <T> List<T> generateRecipeLoadTests(List<String> recipeIds, MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        for (String recipeId : recipeIds) {
            String testName = "recipe_loaded_" + recipeId;
            tests.add(factory.create(testName, helper -> {
                helper.runAfterDelay(1, () -> {
                    var id = CompatResourceLocation.create("chronodawn", recipeId);
                    // Level.getRecipeManager() was removed starting in 1.21.2.
                    // MinecraftServer.getRecipeManager() is stable across every
                    // supported version (verified via javap on 1.21.2 and 26.2
                    // merged jars), so go through the server instead.
                    var recipeManager = helper.getLevel().getServer().getRecipeManager();
                    // RecipeManager.byKey() also changed shape in 1.21.2 (plain id
                    // -> ResourceKey<Recipe<?>> built from the RECIPE registry),
                    // so route the lookup through the era-split compat helper.
                    if (CompatRecipeManager.isRecipeLoaded(recipeManager, id)) {
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
