package com.chronodawn.gametest;

import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * GameTests for Lumen Polyp bone meal growth. Vanilla sea pickles only grow on coral blocks,
 * so these tests use dirt and sand to check that the count rises on any supporting block.
 */
public final class LumenPolypTests {

    private static final BlockPos GROUND_POS = new BlockPos(1, 1, 1);
    private static final BlockPos POLYP_POS = new BlockPos(1, 2, 1);

    private LumenPolypTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("lumen_polyp_bonemeal_grows_on_dirt",
            helper -> testBonemealGrows(helper, Blocks.DIRT)));
        tests.add(factory.create("lumen_polyp_bonemeal_grows_on_sand",
            helper -> testBonemealGrows(helper, Blocks.SAND)));
        tests.add(factory.create("lumen_polyp_bonemeal_stops_at_four",
            LumenPolypTests::testBonemealStopsAtFour));
        return tests;
    }

    private static void placeWaterloggedPolyp(GameTestHelper helper, Block ground) {
        helper.setBlock(GROUND_POS, ground);
        BlockState polyp = ModBlocks.LUMEN_POLYP.get().defaultBlockState()
            .setValue(SeaPickleBlock.PICKLES, 1)
            .setValue(SeaPickleBlock.WATERLOGGED, true);
        helper.setBlock(POLYP_POS, polyp);
    }

    private static boolean useBoneMeal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        return BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, helper.absolutePos(POLYP_POS));
    }

    private static int pickles(GameTestHelper helper) {
        return helper.getBlockState(POLYP_POS).getValue(SeaPickleBlock.PICKLES);
    }

    private static void testBonemealGrows(GameTestHelper helper, Block ground) {
        placeWaterloggedPolyp(helper, ground);
        if (!useBoneMeal(helper)) {
            CompatGameTestHelper.fail(helper, "Bone meal was not applied to the Lumen Polyp");
            return;
        }
        int count = pickles(helper);
        if (count == 2) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "Bone meal should raise the Lumen Polyp count from 1 to 2, got " + count);
        }
    }

    private static void testBonemealStopsAtFour(GameTestHelper helper) {
        placeWaterloggedPolyp(helper, Blocks.DIRT);
        for (int i = 0; i < 3; i++) {
            if (!useBoneMeal(helper)) {
                CompatGameTestHelper.fail(helper, "Bone meal use " + (i + 1) + " was not applied");
                return;
            }
        }
        if (pickles(helper) != 4) {
            CompatGameTestHelper.fail(helper, "Three bone meal uses should reach count 4, got " + pickles(helper));
            return;
        }
        if (useBoneMeal(helper)) {
            CompatGameTestHelper.fail(helper, "Bone meal should not be applied to a full Lumen Polyp");
            return;
        }
        helper.succeed();
    }
}
