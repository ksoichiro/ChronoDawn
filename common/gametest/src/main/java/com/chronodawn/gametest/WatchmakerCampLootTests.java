package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WatchmakerCampLootTests {

    private static final String LOOT_TABLE_PATH = "chests/watchmaker_camp";
    // Enough rolls for the two weight-6 stockpile entries to appear with near certainty.
    private static final int ROLLS = 200;
    private static final int MIN_SUPPLY_STACKS = 1;
    private static final int MAX_SUPPLY_STACKS = 2;

    /** The rare materials, of which every camp roll holds exactly one stack. */
    public static final Set<String> STOCKPILE_ITEMS = Set.of(
        "chronodawn:entropy_crystal",
        "chronodawn:dark_time_wood_log",
        "chronodawn:time_crystal",
        "chronodawn:enhanced_clockstone",
        "chronodawn:ancient_gear",
        "chronodawn:dark_time_wood_sapling",
        "chronodawn:ancient_time_wood_sapling"
    );

    /** The common supplies, of which a camp roll holds one or two stacks. */
    public static final Set<String> SUPPLY_ITEMS = Set.of(
        "minecraft:copper_ingot",
        "minecraft:iron_ingot",
        "minecraft:clock",
        "chronodawn:clockwork_cookie"
    );

    // Inclusive count ranges per item. Items not listed here are always a single item.
    private static final Map<String, int[]> COUNT_RANGES = Map.of(
        "chronodawn:entropy_crystal", new int[] {1, 2},
        "chronodawn:dark_time_wood_log", new int[] {2, 4},
        "chronodawn:time_crystal", new int[] {1, 2},
        "minecraft:copper_ingot", new int[] {1, 3},
        "minecraft:iron_ingot", new int[] {1, 2},
        "chronodawn:clockwork_cookie", new int[] {2, 4}
    );

    private WatchmakerCampLootTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("watchmaker_camp_loot_rolls_one_stockpile_item",
            WatchmakerCampLootTests::testRollsOneStockpileItem));
        return tests;
    }

    /**
     * Test: each roll of the camp table holds exactly one stockpile stack and one or two supply
     * stacks, with counts in range, and the two most common stockpile items show up.
     */
    private static void testRollsOneStockpileItem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Set<String> seenStockpile = new HashSet<>();
        for (int roll = 0; roll < ROLLS; roll++) {
            List<ItemStack> stacks = CompatGameTestHelper.rollModLootTable(level, LOOT_TABLE_PATH);
            int stockpileStacks = 0;
            int supplyStacks = 0;
            for (ItemStack stack : stacks) {
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (STOCKPILE_ITEMS.contains(id)) {
                    stockpileStacks++;
                    seenStockpile.add(id);
                } else if (SUPPLY_ITEMS.contains(id)) {
                    supplyStacks++;
                } else {
                    CompatGameTestHelper.fail(helper, "Unexpected camp item: " + stack);
                    return;
                }
                int[] range = COUNT_RANGES.getOrDefault(id, new int[] {1, 1});
                if (stack.getCount() < range[0] || stack.getCount() > range[1]) {
                    CompatGameTestHelper.fail(helper, "Count out of range for " + stack);
                    return;
                }
            }
            if (stockpileStacks != 1) {
                CompatGameTestHelper.fail(helper, "Camp roll should hold one stockpile stack, got " + stockpileStacks);
                return;
            }
            if (supplyStacks < MIN_SUPPLY_STACKS || supplyStacks > MAX_SUPPLY_STACKS) {
                CompatGameTestHelper.fail(helper, "Camp roll should hold 1 to 2 supply stacks, got " + supplyStacks);
                return;
            }
        }
        for (String id : List.of("chronodawn:entropy_crystal", "chronodawn:dark_time_wood_log")) {
            if (!seenStockpile.contains(id)) {
                CompatGameTestHelper.fail(helper, "Never rolled " + id + " in " + ROLLS + " rolls");
                return;
            }
        }
        helper.succeed();
    }
}
