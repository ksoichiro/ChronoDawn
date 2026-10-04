package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * GameTests for Timeless Mushroom survival rules.
 *
 * Worldgen places Timeless Mushrooms before light is calculated, so naturally
 * generated mushrooms can sit in light above the placement limit. They must
 * stay in place when a neighbor changes (e.g. bone meal growing grass next to
 * them) instead of breaking and dropping.
 */
public final class TimelessMushroomTests {

    private static final BlockPos GROUND_POS = new BlockPos(1, 1, 1);
    private static final BlockPos MUSHROOM_POS = new BlockPos(1, 2, 1);
    private static final BlockPos LIGHT_POS = new BlockPos(1, 3, 1);
    private static final BlockPos NEIGHBOR_POS = new BlockPos(2, 2, 1);
    private static final int MAX_LIGHT_WAIT_TICKS = 20;

    private TimelessMushroomTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("timeless_mushroom_survives_neighbor_update_in_bright_light",
            TimelessMushroomTests::testSurvivesNeighborUpdateInBrightLight));
        return tests;
    }

    /**
     * Test: a Timeless Mushroom already standing in bright light is not broken
     * when a neighboring block changes.
     */
    private static void testSurvivesNeighborUpdateInBrightLight(GameTestHelper helper) {
        helper.setBlock(GROUND_POS, Blocks.MOSS_BLOCK);
        helper.setBlock(LIGHT_POS, Blocks.GLOWSTONE);
        // setBlock does not check canSurvive, mirroring worldgen placement in bright spots
        helper.setBlock(MUSHROOM_POS, ModBlocks.TIMELESS_MUSHROOM.get());
        helper.runAfterDelay(1, () -> updateNeighborOnceBright(helper, 0));
    }

    /**
     * Light updates are applied asynchronously, so poll each tick until the
     * glowstone light reaches the mushroom instead of relying on a fixed delay.
     */
    private static void updateNeighborOnceBright(GameTestHelper helper, int waitedTicks) {
        int brightness = helper.getLevel().getRawBrightness(helper.absolutePos(MUSHROOM_POS), 0);
        if (brightness <= 12) {
            if (waitedTicks >= MAX_LIGHT_WAIT_TICKS) {
                CompatGameTestHelper.fail(helper, "Test setup expected light above 12 at the mushroom, got "
                    + brightness + " after " + waitedTicks + " ticks");
                return;
            }
            helper.runAfterDelay(1, () -> updateNeighborOnceBright(helper, waitedTicks + 1));
            return;
        }
        helper.setBlock(NEIGHBOR_POS, Blocks.STONE);
        helper.runAfterDelay(1, () -> {
            if (helper.getBlockState(MUSHROOM_POS).is(ModBlocks.TIMELESS_MUSHROOM.get())) {
                helper.succeed();
            } else {
                CompatGameTestHelper.fail(helper, "Timeless Mushroom in light " + brightness
                    + " broke after a neighbor update");
            }
        });
    }
}
