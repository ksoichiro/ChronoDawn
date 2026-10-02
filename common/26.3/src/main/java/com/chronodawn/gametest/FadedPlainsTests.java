package com.chronodawn.gametest;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Faded Plains test logic for 1.21.5+ (shared by Fabric @GameTest and NeoForge registry-based tests).
 *
 * In 1.21.5+, Fabric uses @GameTest annotation directly and the registry-driven
 * generator returns empty from generateTests() to avoid duplicate registration.
 * NeoForge references the static Consumer constants to register the same tests.
 */
public final class FadedPlainsTests {

    private static final BlockPos BASE_POS = new BlockPos(1, 2, 1);
    private static final BlockPos TOP_POS = new BlockPos(1, 3, 1);

    private FadedPlainsTests() {
        // Utility class
    }

    // ============== Shared test logic (used by both Fabric @GameTest and NeoForge registerTest) ==============

    /**
     * Test: TEMPORAL_DEAD_BUSH canSurvive() returns true when the block below is PARCHED_TEMPORAL_DIRT.
     */
    public static final Consumer<GameTestHelper> TEST_DEAD_BUSH_SURVIVES_ON_PARCHED_DIRT = helper -> {
        helper.setBlock(BASE_POS, ModBlocks.PARCHED_TEMPORAL_DIRT.get());
        helper.runAfterDelay(1, () -> {
            BlockState deadBushState = ModBlocks.TEMPORAL_DEAD_BUSH.get().defaultBlockState();
            BlockPos absTop = helper.absolutePos(TOP_POS);
            boolean survives = deadBushState.canSurvive(helper.getLevel(), absTop);
            if (survives) {
                helper.succeed();
            } else {
                helper.fail(Component.literal(
                    "TEMPORAL_DEAD_BUSH should survive on PARCHED_TEMPORAL_DIRT but canSurvive() returned false"));
            }
        });
    };

    /**
     * Test: TEMPORAL_DEAD_BUSH canSurvive() returns false when the block below is TEMPORAL_GRASS_BLOCK.
     */
    public static final Consumer<GameTestHelper> TEST_DEAD_BUSH_BREAKS_ON_GRASS = helper -> {
        helper.setBlock(BASE_POS, ModBlocks.TEMPORAL_GRASS_BLOCK.get());
        helper.runAfterDelay(1, () -> {
            BlockState deadBushState = ModBlocks.TEMPORAL_DEAD_BUSH.get().defaultBlockState();
            BlockPos absTop = helper.absolutePos(TOP_POS);
            boolean survives = deadBushState.canSurvive(helper.getLevel(), absTop);
            if (!survives) {
                helper.succeed();
            } else {
                helper.fail(Component.literal(
                    "TEMPORAL_DEAD_BUSH should NOT survive on TEMPORAL_GRASS_BLOCK but canSurvive() returned true"));
            }
        });
    };

    /**
     * Test: CHRONO_MELON canSurvive() returns true when the block below is vanilla GRASS_BLOCK.
     *
     * Regression guard: since 26.1.2, minecraft:dirt no longer contains grass_block,
     * so a #minecraft:dirt soil check rejects vanilla grass.
     */
    public static final Consumer<GameTestHelper> TEST_CHRONO_MELON_SURVIVES_ON_VANILLA_GRASS = helper -> {
        helper.setBlock(BASE_POS, net.minecraft.world.level.block.Blocks.GRASS_BLOCK);
        helper.runAfterDelay(1, () -> {
            BlockState melonState = ModBlocks.CHRONO_MELON.get().defaultBlockState();
            BlockPos absTop = helper.absolutePos(TOP_POS);
            boolean survives = melonState.canSurvive(helper.getLevel(), absTop);
            if (survives) {
                helper.succeed();
            } else {
                helper.fail(Component.literal(
                    "CHRONO_MELON should survive on vanilla GRASS_BLOCK but canSurvive() returned false"));
            }
        });
    };

    /**
     * Test: dirt-planted Chrono Dawn plants canSurvive() on vanilla GRASS_BLOCK.
     *
     * Same regression guard as the Chrono Melon test. TIMELESS_MUSHROOM is excluded
     * because its canSurvive() also requires light level 12 or less.
     */
    public static final Consumer<GameTestHelper> TEST_PLANTS_SURVIVE_ON_VANILLA_GRASS = helper -> {
        helper.setBlock(BASE_POS, net.minecraft.world.level.block.Blocks.GRASS_BLOCK);
        helper.runAfterDelay(1, () -> {
            BlockPos absTop = helper.absolutePos(TOP_POS);
            List<String> failures = new ArrayList<>();
            for (var block : List.of(
                    ModBlocks.TEMPORAL_ROOT.get(),
                    ModBlocks.PURPLE_TIME_BLOSSOM.get(),
                    ModBlocks.TEMPORAL_FERN.get(),
                    ModBlocks.TEMPORAL_GRASS.get(),
                    ModBlocks.FADED_TEMPORAL_GRASS.get())) {
                if (!block.defaultBlockState().canSurvive(helper.getLevel(), absTop)) {
                    failures.add(block.getDescriptionId());
                }
            }
            if (failures.isEmpty()) {
                helper.succeed();
            } else {
                helper.fail(Component.literal(
                    "Plants should survive on vanilla GRASS_BLOCK but canSurvive() returned false: " + failures));
            }
        });
    };

    /**
     * Test: FADED_TEMPORAL_GRASS drops itself (1 item) when broken with shears.
     *
     * The loot table for faded_temporal_grass specifies a shears match_tool condition,
     * so only shears should cause the block to drop itself.
     */
    public static final Consumer<GameTestHelper> TEST_FADED_GRASS_SHEARS_DROPS_SELF = helper -> {
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
                helper.fail(Component.literal(
                    "FADED_TEMPORAL_GRASS with shears should drop exactly 1 faded_temporal_grass item, got: " + drops));
            }
        });
    };

    /**
     * Returns empty — for 1.21.5+ Faded Plains tests are registered directly as @GameTest
     * methods (Fabric) or via registerTest() (NeoForge), not through the registry-driven generator.
     */
    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        return new ArrayList<>();
    }
}
