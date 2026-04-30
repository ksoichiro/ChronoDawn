package com.chronodawn.gametest;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * GameTests for Faded Plains biome blocks.
 *
 * Verifies placement rules for TEMPORAL_DEAD_BUSH and FADED_TEMPORAL_GRASS,
 * ensuring canSurvive() returns the correct value for each substrate.
 * Also verifies that FADED_TEMPORAL_GRASS drops itself when broken with shears.
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
        tests.add(factory.create("faded_grass_shears_drops_self",
            FadedPlainsTests::testFadedGrassShearsDropsSelf));
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

    /**
     * Test: FADED_TEMPORAL_GRASS drops itself (1 item) when broken with shears.
     *
     * The loot table for faded_temporal_grass specifies a shears match_tool condition,
     * so only shears should cause the block to drop itself.
     */
    private static void testFadedGrassShearsDropsSelf(GameTestHelper helper) {
        helper.setBlock(BASE_POS, ModBlocks.FADED_TEMPORAL_GRASS.get());
        helper.runAfterDelay(1, () -> {
            BlockState grassState = ModBlocks.FADED_TEMPORAL_GRASS.get().defaultBlockState();
            BlockPos absPos = helper.absolutePos(BASE_POS);
            ServerLevel serverLevel = helper.getLevel();
            ItemStack shears = new ItemStack(Items.SHEARS);
            LootParams.Builder lootParamsBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(absPos))
                .withParameter(LootContextParams.TOOL, shears);
            java.util.List<ItemStack> drops = grassState.getDrops(lootParamsBuilder);
            if (drops.size() == 1 && drops.get(0).is(ModBlocks.FADED_TEMPORAL_GRASS.get().asItem())) {
                helper.succeed();
            } else {
                helper.fail("FADED_TEMPORAL_GRASS with shears should drop exactly 1 faded_temporal_grass item, got: " + drops);
            }
        });
    }
}
