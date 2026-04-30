package com.chronodawn.gametest;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * GameTests for Faded Plains biome blocks.
 *
 * Verifies placement rules for TEMPORAL_DEAD_BUSH and FADED_TEMPORAL_GRASS,
 * ensuring canSurvive() returns the correct value for each substrate.
 */
public final class FadedPlainsTests {

    private static final BlockPos BASE_POS = new BlockPos(1, 2, 1);
    private static final BlockPos TOP_POS = new BlockPos(1, 3, 1);

    private FadedPlainsTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("dead_bush_survives_on_parched_dirt",
            FadedPlainsTests::testDeadBushSurvivesOnParchedDirt));
        tests.add(factory.create("dead_bush_breaks_on_grass",
            FadedPlainsTests::testDeadBushBreaksOnGrass));
        return tests;
    }

    /**
     * Test: TEMPORAL_DEAD_BUSH canSurvive() returns true when the block below is PARCHED_TEMPORAL_DIRT.
     *
     * PARCHED_TEMPORAL_DIRT is one of the accepted substrates for TEMPORAL_DEAD_BUSH
     * (see TemporalDeadBushBlock.mayPlaceOn).
     */
    private static void testDeadBushSurvivesOnParchedDirt(GameTestHelper helper) {
        helper.setBlock(BASE_POS, ModBlocks.PARCHED_TEMPORAL_DIRT.get());
        helper.runAfterDelay(1, () -> {
            BlockState deadBushState = ModBlocks.TEMPORAL_DEAD_BUSH.get().defaultBlockState();
            BlockPos absTop = helper.absolutePos(TOP_POS);
            boolean survives = deadBushState.canSurvive(helper.getLevel(), absTop);
            if (survives) {
                helper.succeed();
            } else {
                helper.fail("TEMPORAL_DEAD_BUSH should survive on PARCHED_TEMPORAL_DIRT but canSurvive() returned false");
            }
        });
    }

    /**
     * Test: TEMPORAL_DEAD_BUSH canSurvive() returns false when the block below is TEMPORAL_GRASS_BLOCK.
     *
     * TEMPORAL_GRASS_BLOCK is not in the accepted substrate list for TEMPORAL_DEAD_BUSH
     * (see TemporalDeadBushBlock.mayPlaceOn), so the bush must not survive there.
     */
    private static void testDeadBushBreaksOnGrass(GameTestHelper helper) {
        helper.setBlock(BASE_POS, ModBlocks.TEMPORAL_GRASS_BLOCK.get());
        helper.runAfterDelay(1, () -> {
            BlockState deadBushState = ModBlocks.TEMPORAL_DEAD_BUSH.get().defaultBlockState();
            BlockPos absTop = helper.absolutePos(TOP_POS);
            boolean survives = deadBushState.canSurvive(helper.getLevel(), absTop);
            if (!survives) {
                helper.succeed();
            } else {
                helper.fail("TEMPORAL_DEAD_BUSH should NOT survive on TEMPORAL_GRASS_BLOCK but canSurvive() returned true");
            }
        });
    }
}
