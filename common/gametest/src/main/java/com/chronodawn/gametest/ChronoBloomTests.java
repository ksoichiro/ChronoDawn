package com.chronodawn.gametest;

import com.chronodawn.blocks.ChronoBloomBlock;
import com.chronodawn.blocks.ChronoBloomStage;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatLevelTime;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GameTests for the Chrono Bloom, whose STAGE follows the time of day.
 *
 * Each test runs at noon (6000), where the expected stage is OPEN (2), which differs from
 * the default CLOSED (0) state. GameTests share one level, so {@link #atNoon} restores the
 * original time as soon as the synchronous test body returns or throws, before any other
 * test runs.
 */
public final class ChronoBloomTests {

    private static final BlockPos GROUND_POS = new BlockPos(1, 1, 1);
    private static final BlockPos FLOWER_POS = new BlockPos(1, 2, 1);
    private static final long NOON = 6000L;

    private ChronoBloomTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("chrono_bloom_placed_at_noon_is_open",
            helper -> atNoon(helper, ChronoBloomTests::testPlacedAtNoonIsOpen)));
        tests.add(factory.create("chrono_bloom_random_tick_updates_stage",
            helper -> atNoon(helper, ChronoBloomTests::testRandomTickUpdatesStage)));
        tests.add(factory.create("chrono_bloom_potting_at_noon_shows_open_stage",
            helper -> atNoon(helper, ChronoBloomTests::testPottingAtNoonShowsOpenStage)));
        tests.add(factory.create("chrono_bloom_potted_drops_flower_and_pot",
            helper -> atNoon(helper, ChronoBloomTests::testPottedDropsFlowerAndPot)));
        tests.add(factory.create("chrono_bloom_bonemeal_drops_one_bloom",
            helper -> atNoon(helper, ChronoBloomTests::testBonemealDropsOneBloom)));
        return tests;
    }

    /**
     * Run a synchronous test body at noon and put the level time back afterwards,
     * on success and on failure (GameTest failures are thrown as exceptions).
     */
    private static void atNoon(GameTestHelper helper, Consumer<GameTestHelper> body) {
        ServerLevel level = helper.getLevel();
        long originalTime = CompatLevelTime.dayTime(level);
        CompatLevelTime.setDayTime(level, NOON);
        try {
            body.accept(helper);
        } finally {
            CompatLevelTime.setDayTime(level, originalTime);
        }
    }

    /**
     * Test: the placement state takes its stage from the current time of day.
     */
    private static void testPlacedAtNoonIsOpen(GameTestHelper helper) {
        helper.setBlock(GROUND_POS, Blocks.GRASS_BLOCK);
        BlockPos absGround = helper.absolutePos(GROUND_POS);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absGround), Direction.UP, absGround, false);
        Player player = CompatGameTestHelper.makeMockPlayer(helper);
        BlockPlaceContext context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND,
            new ItemStack(ModItems.CHRONO_BLOOM.get()), hit);
        BlockState placed = ModBlocks.CHRONO_BLOOM.get().getStateForPlacement(context);
        if (placed == null) {
            CompatGameTestHelper.fail(helper, "Chrono Bloom placement state was null");
            return;
        }
        helper.setBlock(FLOWER_POS, placed);
        int stage = helper.getBlockState(FLOWER_POS).getValue(ChronoBloomBlock.STAGE);
        if (stage == ChronoBloomStage.OPEN) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "Chrono Bloom placed at noon should be stage 2, got " + stage);
        }
    }

    /**
     * Test: a random tick moves a stale stage to the stage for the current time.
     */
    private static void testRandomTickUpdatesStage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(GROUND_POS, Blocks.GRASS_BLOCK);
        helper.setBlock(FLOWER_POS, ModBlocks.CHRONO_BLOOM.get().defaultBlockState()
            .setValue(ChronoBloomBlock.STAGE, ChronoBloomStage.CLOSED));
        BlockPos absPos = helper.absolutePos(FLOWER_POS);
        level.getBlockState(absPos).randomTick(level, absPos, level.getRandom());
        int stage = helper.getBlockState(FLOWER_POS).getValue(ChronoBloomBlock.STAGE);
        if (stage == ChronoBloomStage.OPEN) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "Random tick at noon should set stage 2, got " + stage);
        }
    }

    /**
     * Test: using a Chrono Bloom on an empty vanilla flower pot creates the potted
     * Chrono Bloom, already showing the stage for the current time.
     */
    private static void testPottingAtNoonShowsOpenStage(GameTestHelper helper) {
        helper.setBlock(GROUND_POS, Blocks.STONE);
        helper.setBlock(FLOWER_POS, Blocks.FLOWER_POT);
        Player player = CompatGameTestHelper.makeMockPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CHRONO_BLOOM.get()));
        helper.useBlock(FLOWER_POS, player);
        BlockState state = helper.getBlockState(FLOWER_POS);
        if (!state.is(ModBlocks.POTTED_CHRONO_BLOOM.get())) {
            CompatGameTestHelper.fail(helper, "Flower pot should hold a Chrono Bloom, got " + state);
            return;
        }
        int stage = state.getValue(ChronoBloomBlock.STAGE);
        if (stage == ChronoBloomStage.OPEN) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "Potted Chrono Bloom at noon should be stage 2, got " + stage);
        }
    }

    /**
     * Test: breaking a potted Chrono Bloom drops both the flower and the pot.
     */
    private static void testPottedDropsFlowerAndPot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(GROUND_POS, Blocks.STONE);
        helper.setBlock(FLOWER_POS, ModBlocks.POTTED_CHRONO_BLOOM.get());
        BlockPos absPos = helper.absolutePos(FLOWER_POS);
        Player player = CompatGameTestHelper.makeMockPlayer(helper);
        level.destroyBlock(absPos, true, player);
        int blooms = countDropped(level, absPos, ModItems.CHRONO_BLOOM.get());
        int pots = countDropped(level, absPos, Items.FLOWER_POT);
        if (blooms == 1 && pots == 1) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "Potted Chrono Bloom should drop 1 chrono_bloom and 1 flower_pot, got "
                + blooms + " and " + pots);
        }
    }

    /**
     * Test: bone meal drops one extra Chrono Bloom and leaves the block unchanged.
     */
    private static void testBonemealDropsOneBloom(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(GROUND_POS, Blocks.GRASS_BLOCK);
        helper.setBlock(FLOWER_POS, ModBlocks.CHRONO_BLOOM.get());
        BlockPos absPos = helper.absolutePos(FLOWER_POS);
        BlockState before = level.getBlockState(absPos);
        if (!BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, absPos)) {
            CompatGameTestHelper.fail(helper, "Bone meal was not applied to the Chrono Bloom");
            return;
        }
        int blooms = countDropped(level, absPos, ModItems.CHRONO_BLOOM.get());
        BlockState after = level.getBlockState(absPos);
        if (blooms == 1 && after == before) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "Bone meal should drop 1 chrono_bloom and keep the block, got "
                + blooms + " drops and state " + after);
        }
    }

    private static int countDropped(ServerLevel level, BlockPos pos, Item item) {
        int count = 0;
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1))) {
            if (entity.getItem().is(item)) {
                count += entity.getItem().getCount();
            }
        }
        return count;
    }
}
