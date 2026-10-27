package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatRecipeManager;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * GameTests for the Time Cairn wayside feature: the dial, the stones and the keystone.
 */
public class TimeCairnDialTests {

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("recipe_loaded_cairn_keystone",
            TimeCairnDialTests::testKeystoneRecipeLoaded));
        return tests;
    }

    /**
     * Test: the keystone recipe loads on this version. The three recipe roots use different
     * ingredient formats, so a format slip would drop the recipe silently.
     */
    private static void testKeystoneRecipeLoaded(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            var id = CompatResourceLocation.create("chronodawn", "cairn_keystone");
            var recipeManager = helper.getLevel().getServer().getRecipeManager();
            if (CompatRecipeManager.isRecipeLoaded(recipeManager, id)) {
                helper.succeed();
            } else {
                CompatGameTestHelper.fail(helper, "Recipe not loaded: chronodawn:cairn_keystone");
            }
        });
    }
}
