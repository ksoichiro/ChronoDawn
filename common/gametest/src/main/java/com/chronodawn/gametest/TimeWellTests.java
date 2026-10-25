package com.chronodawn.gametest;

import com.chronodawn.blocks.TimeWellStoneBlock;
import com.chronodawn.blocks.TimeWellStoneBlockEntity;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.core.wayside.TimeWellOutcome;
import com.chronodawn.core.wayside.WellQueue;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public final class TimeWellTests {

    private static final BlockPos WATER_POS = new BlockPos(1, 2, 1);
    private static final BlockPos STONE_POS = new BlockPos(1, 2, 0);
    // Rolls per table. The rare result has weight 1 of 10 or 20, so one roll could miss a bad entry.
    private static final int ROLLS = 30;
    private static final int MERGE_WAIT_TICKS = 10;
    // Longer than the default pickup delay of 10 ticks, so a normal delay has run out by then.
    private static final int PICKUP_DELAY_WAIT_TICKS = 12;
    // A few ticks past the answer delay, so the ticker has answered the waiting item.
    private static final int ANSWER_WAIT_TICKS = (int) WellQueue.ANSWER_DELAY_TICKS + 5;
    // Away from the shaft, so the risen items land short of the player and are not picked up.
    private static final BlockPos PLAYER_POS = new BlockPos(8, 2, 8);
    private static final BlockPos ARMOR_STAND_POS = new BlockPos(4, 2, 4);
    // A second well for the findWater test, clear of the shared layout above. Stray water lies
    // north of the stone, which findWater checks first, and the shaft lies south of it.
    private static final BlockPos ENCLOSED_STONE_POS = new BlockPos(5, 2, 5);
    private static final BlockPos STRAY_WATER_POS = new BlockPos(5, 2, 4);
    private static final BlockPos ENCLOSED_SHAFT_POS = new BlockPos(5, 2, 6);
    // The full well from time_well.nbt, in the middle of the test area so the risen items land
    // on its floor. The template's shaft column (1, y, 1) is its rotation pivot, as in
    // NbtTemplateFeature, so the water stays at FULL_WELL_WATER_POS for every rotation.
    private static final BlockPos FULL_WELL_ORIGIN = new BlockPos(3, 1, 3);
    private static final BlockPos FULL_WELL_WATER_POS = new BlockPos(4, 2, 4);
    private static final BlockPos TEMPLATE_PIVOT = new BlockPos(1, 0, 1);
    private static final int FLOOR_SIZE = 10;
    // Before rotation the stone is north of the shaft and the roof slab south of it, one block
    // above the log posts.
    private static final Direction TEMPLATE_STONE_SIDE = Direction.NORTH;
    private static final Direction TEMPLATE_ROOF_SIDE = Direction.SOUTH;
    private static final int ROOF_HEIGHT_ABOVE_WATER = 3;
    private static final int ROOF_THROWER_DISTANCE = 3;
    private static final int ABOVE_TOSS_HEIGHT = 4;
    // A risen item lands about 20 ticks after it is tossed. Twice that leaves time to settle.
    private static final int LANDING_WAIT_TICKS = 40;
    // The toss lands about 4.7 blocks from the shaft's centre, possibly just past the floor's
    // edge. Neighbouring tests' items land more than 8 blocks away, so they are not counted.
    private static final int LANDING_SEARCH_RANGE = 6;
    // Half the width of the shaft and its rim, from the shaft's centre.
    private static final double RIM_OUTER_HALF_WIDTH = 1.5;

    private TimeWellTests() {
        // Utility class
    }

    /**
     * One row of the exchange table: the thrown item, the common result and its count range,
     * and the rare result, always a single item.
     */
    private record ExchangeRow(Supplier<Item> thrown, Supplier<Item> common, int commonMin, int commonMax,
                               Supplier<Item> rare) {
    }

    private static List<ExchangeRow> exchangeRows() {
        return List.of(
            new ExchangeRow(ModItems.TIME_WHEAT, ModItems.TIME_BREAD, 1, 1, ModItems.GOLDEN_TIME_WHEAT),
            new ExchangeRow(ModItems.TIME_BREAD, ModItems.ENHANCED_TIME_BREAD, 1, 1, ModItems.TIME_FRUIT_PIE),
            new ExchangeRow(ModItems.CHRONO_MELON_SLICE, ModItems.CHRONO_MELON_JUICE, 1, 1,
                ModItems.GLISTENING_CHRONO_MELON),
            new ExchangeRow(ModItems.RAW_TEMPORAL_AMBER, ModItems.TEMPORAL_AMBER_DUST, 2, 3, ModItems.TIME_CRYSTAL),
            new ExchangeRow(ModItems.CLOCKSTONE, ModItems.CLOCKSTONE, 2, 2, ModItems.TIME_CRYSTAL),
            new ExchangeRow(ModItems.TIME_CRYSTAL, ModItems.TIME_CRYSTAL, 2, 2, ModItems.ENHANCED_CLOCKSTONE)
        );
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("time_well_stone_has_block_entity",
            TimeWellTests::testHasBlockEntity));
        tests.add(factory.create("time_well_find_water_prefers_shaft",
            TimeWellTests::testFindWaterPrefersShaft));
        tests.add(factory.create("time_well_exchange_tables_roll_expected_items",
            TimeWellTests::testExchangeTables));
        tests.add(factory.create("time_well_exchanges_one_and_returns_rest",
            TimeWellTests::testExchangesOneAndReturnsRest));
        tests.add(factory.create("time_well_second_wish_returns_unchanged",
            TimeWellTests::testSecondWishReturnsUnchanged));
        tests.add(factory.create("time_well_unaccepted_item_keeps_cooldown_free",
            TimeWellTests::testUnacceptedKeepsCooldownFree));
        tests.add(factory.create("time_well_returns_when_thrower_gone",
            TimeWellTests::testReturnsWhenThrowerGone));
        tests.add(factory.create("time_well_ignores_item_without_thrower",
            TimeWellTests::testIgnoresItemWithoutThrower));
        tests.add(factory.create("time_well_tick_exchanges_for_joined_player",
            TimeWellTests::testTickExchangesForJoinedPlayer));
        tests.add(factory.create("time_well_tick_ignores_non_player_thrower",
            TimeWellTests::testTickIgnoresNonPlayerThrower));
        tests.add(factory.create("time_well_waiting_item_does_not_merge",
            TimeWellTests::testWaitingItemDoesNotMerge));
        tests.add(factory.create("time_well_requeues_orphaned_waiting_item",
            TimeWellTests::testRequeuesOrphanedWaitingItem));
        tests.add(factory.create("time_well_returns_requeued_orphan_as_plain_item",
            TimeWellTests::testReturnsRequeuedOrphan));
        tests.add(factory.create("time_well_answers_waiting_item_after_water_removed",
            TimeWellTests::testAnswersAfterWaterRemoved));
        tests.add(factory.create("time_well_stone_removal_releases_waiting_item",
            TimeWellTests::testStoneRemovalReleasesWaitingItem));
        tests.add(factory.create("time_well_stone_drops_mossy_cobblestone",
            TimeWellTests::testDrops));
        tests.add(factory.create("time_well_raises_again_risen_item_fallen_back",
            TimeWellTests::testRaisesAgainRisenItemFallenBack));
        for (Rotation rotation : Rotation.values()) {
            tests.add(factory.create("time_well_roof_side_return_lands_outside_"
                    + rotation.name().toLowerCase(Locale.ROOT),
                helper -> testRoofSideReturnLandsOutside(helper, rotation, false)));
        }
        tests.add(factory.create("time_well_block_above_toss_path_keeps_launch_direction",
            helper -> testRoofSideReturnLandsOutside(helper, Rotation.NONE, true)));
        return tests;
    }

    /**
     * Places the water shaft and the stone beside it, and returns the stone's block entity,
     * or null after failing the test.
     */
    private static TimeWellStoneBlockEntity placeWell(GameTestHelper helper) {
        helper.setBlock(WATER_POS, Blocks.WATER);
        helper.setBlock(STONE_POS, ModBlocks.TIME_WELL_STONE.get());
        if (helper.getLevel().getBlockEntity(helper.absolutePos(STONE_POS)) instanceof TimeWellStoneBlockEntity well) {
            return well;
        }
        CompatGameTestHelper.fail(helper, "time_well_stone has no TimeWellStoneBlockEntity");
        return null;
    }

    private static ItemEntity spawnInWater(GameTestHelper helper, ItemStack stack) {
        ServerLevel level = helper.getLevel();
        BlockPos water = helper.absolutePos(WATER_POS);
        ItemEntity item = new ItemEntity(level, water.getX() + 0.5, water.getY() + 0.5, water.getZ() + 0.5, stack);
        level.addFreshEntity(item);
        return item;
    }

    /**
     * Collects the items that rose out of the well since the throw, other than the thrown one,
     * and removes them so the next step of the test starts from an empty well.
     */
    private static List<ItemStack> takeRisen(GameTestHelper helper, ItemEntity original, long thrownAt) {
        List<ItemEntity> found = findRisen(helper, original, 3, thrownAt);
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : found) {
            stacks.add(entity.getItem().copy());
            entity.discard();
        }
        return stacks;
    }

    /**
     * Finds the items that rose out of the well since the throw, other than the thrown one.
     * Older items are skipped: the GameTest world is reused across runs, so items left by an
     * earlier run, or popped out by a neighbouring test, can already lie in the search area.
     */
    private static List<ItemEntity> findRisen(GameTestHelper helper, ItemEntity original, int range,
                                              long thrownAt) {
        ServerLevel level = helper.getLevel();
        return level.getEntitiesOfClass(ItemEntity.class,
            new AABB(helper.absolutePos(WATER_POS)).inflate(range),
            e -> e != original && e.isAlive() && level.getGameTime() - e.getAge() >= thrownAt);
    }

    private static boolean isSingleStack(List<ItemStack> stacks, Item item, int count) {
        return stacks.size() == 1 && stacks.get(0).is(item) && stacks.get(0).getCount() == count;
    }

    /**
     * @return whether the stacks are what one exchanged clockstone gives: 2 clockstone or
     *         1 time crystal, with nothing left of the thrown stack
     */
    private static boolean isClockstoneExchangeResult(List<ItemStack> stacks) {
        return isSingleStack(stacks, ModItems.CLOCKSTONE.get(), 2)
            || isSingleStack(stacks, ModItems.TIME_CRYSTAL.get(), 1);
    }

    /**
     * Creates a player who has joined the server, so the well can resolve them by UUID as it
     * does in a real game, and stands them away from the shaft. Call
     * {@link CompatGameTestHelper#removeMockServerPlayer} when the test ends.
     */
    private static ServerPlayer joinPlayer(GameTestHelper helper) {
        ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
        Vec3 pos = Vec3.atBottomCenterOf(helper.absolutePos(PLAYER_POS));
        player.setPos(pos.x, pos.y, pos.z);
        return player;
    }

    /**
     * After the answer delay, checks that the waiting clockstone is gone, that one exchange
     * result rose in its place, and that the exchange used up the player's cooldown.
     * Logs the player out whatever the result.
     */
    private static void expectTickExchangeAfterDelay(GameTestHelper helper, ServerPlayer player,
                                                     ItemEntity thrown, long thrownAt, String context) {
        helper.runAfterDelay(ANSWER_WAIT_TICKS, () -> {
            try {
                if (!thrown.isRemoved()) {
                    CompatGameTestHelper.fail(helper, context + ": the thrown item was not answered");
                    return;
                }
                // Only clockstone and time crystal can come out of this wish. Other items, such as a
                // button popped off a neighbouring test structure during the wait, are not counted.
                List<ItemStack> risen = new ArrayList<>();
                for (ItemStack stack : takeRisen(helper, thrown, thrownAt)) {
                    if (stack.is(ModItems.CLOCKSTONE.get()) || stack.is(ModItems.TIME_CRYSTAL.get())) {
                        risen.add(stack);
                    }
                }
                if (!isClockstoneExchangeResult(risen)) {
                    CompatGameTestHelper.fail(helper, context + ": expected 2 clockstone or 1 time crystal to rise, got "
                        + risen);
                    return;
                }
                if (!expectReturned(helper, new ItemStack(ModItems.CLOCKSTONE.get()), player,
                        TimeWellOutcome.COOLDOWN)) {
                    return;
                }
                helper.succeed();
            } finally {
                CompatGameTestHelper.removeMockServerPlayer(helper, player);
            }
        });
    }

    /**
     * Throws one stack in for the player, answers it at once, and checks the outcome and that
     * the same stack rose back unchanged. Returns false after failing the test.
     */
    private static boolean expectReturned(GameTestHelper helper, ItemStack stack, Player thrower,
                                          TimeWellOutcome expected) {
        long thrownAt = helper.getLevel().getGameTime();
        ItemEntity item = spawnInWater(helper, stack.copy());
        TimeWellOutcome outcome = TimeWellStoneBlock.answer(
            helper.getLevel(), helper.absolutePos(STONE_POS), item, thrower);
        if (outcome != expected) {
            CompatGameTestHelper.fail(helper, "Expected " + expected + " for " + stack + ", got " + outcome);
            return false;
        }
        List<ItemStack> risen = takeRisen(helper, item, thrownAt);
        if (!isSingleStack(risen, stack.getItem(), stack.getCount())) {
            CompatGameTestHelper.fail(helper, "Expected " + stack + " to rise back unchanged, got " + risen);
            return false;
        }
        return true;
    }

    /**
     * Throws one clockstone in for the player and checks that it was exchanged.
     * Returns false after failing the test.
     */
    private static boolean expectExchanged(GameTestHelper helper, Player thrower, String context) {
        long thrownAt = helper.getLevel().getGameTime();
        ItemEntity item = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        TimeWellOutcome outcome = TimeWellStoneBlock.answer(
            helper.getLevel(), helper.absolutePos(STONE_POS), item, thrower);
        takeRisen(helper, item, thrownAt);
        if (outcome != TimeWellOutcome.EXCHANGED) {
            CompatGameTestHelper.fail(helper, context + ": expected EXCHANGED, got " + outcome);
            return false;
        }
        return true;
    }

    /**
     * Test: the placed stone carries its block entity and finds the water source beside it.
     */
    private static void testHasBlockEntity(GameTestHelper helper) {
        if (placeWell(helper) == null) {
            return;
        }
        BlockPos water = TimeWellStoneBlock.findWater(helper.getLevel(), helper.absolutePos(STONE_POS));
        if (!helper.absolutePos(WATER_POS).equals(water)) {
            CompatGameTestHelper.fail(helper, "findWater returned " + water + ", expected "
                + helper.absolutePos(WATER_POS));
            return;
        }
        helper.succeed();
    }

    /**
     * Test: water outside the rim that touches the stone, and comes first in the scan order,
     * does not hide the shaft, which is walled in on its other three sides.
     */
    private static void testFindWaterPrefersShaft(GameTestHelper helper) {
        helper.setBlock(STRAY_WATER_POS, Blocks.WATER);
        helper.setBlock(ENCLOSED_SHAFT_POS, Blocks.WATER);
        helper.setBlock(ENCLOSED_SHAFT_POS.west(), Blocks.COBBLESTONE);
        helper.setBlock(ENCLOSED_SHAFT_POS.east(), Blocks.COBBLESTONE);
        helper.setBlock(ENCLOSED_SHAFT_POS.south(), Blocks.COBBLESTONE);
        helper.setBlock(ENCLOSED_STONE_POS, ModBlocks.TIME_WELL_STONE.get());
        BlockPos water = TimeWellStoneBlock.findWater(helper.getLevel(), helper.absolutePos(ENCLOSED_STONE_POS));
        if (!helper.absolutePos(ENCLOSED_SHAFT_POS).equals(water)) {
            CompatGameTestHelper.fail(helper, "findWater returned " + water + ", expected the shaft at "
                + helper.absolutePos(ENCLOSED_SHAFT_POS));
            return;
        }
        helper.succeed();
    }

    /**
     * Test: every exchange table rolls exactly one stack, either its common result in the
     * stated count range or its rare result as a single item.
     */
    private static void testExchangeTables(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(STONE_POS);
        for (ExchangeRow row : exchangeRows()) {
            Item thrown = row.thrown().get();
            for (int roll = 0; roll < ROLLS; roll++) {
                List<ItemStack> result = TimeWellStoneBlock.rollExchange(level, pos, thrown, null);
                if (result.size() != 1) {
                    CompatGameTestHelper.fail(helper, "Exchange for " + thrown + " should yield one stack, got " + result);
                    return;
                }
                ItemStack stack = result.get(0);
                boolean common = stack.is(row.common().get())
                    && stack.getCount() >= row.commonMin() && stack.getCount() <= row.commonMax();
                boolean rare = stack.is(row.rare().get()) && stack.getCount() == 1;
                if (!common && !rare) {
                    CompatGameTestHelper.fail(helper, "Unexpected exchange for " + thrown + ": " + stack);
                    return;
                }
            }
        }
        helper.succeed();
    }

    /**
     * Test: a full stack exchanges a single item. The result and the other 63 rise out,
     * and the thrown entity is gone.
     */
    private static void testExchangesOneAndReturnsRest(GameTestHelper helper) {
        if (placeWell(helper) == null) {
            return;
        }
        Player player = CompatGameTestHelper.makeMockPlayer(helper);
        long thrownAt = helper.getLevel().getGameTime();
        ItemEntity item = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get(), 64));
        TimeWellOutcome outcome = TimeWellStoneBlock.answer(
            helper.getLevel(), helper.absolutePos(STONE_POS), item, player);
        if (outcome != TimeWellOutcome.EXCHANGED) {
            CompatGameTestHelper.fail(helper, "Expected EXCHANGED, got " + outcome);
            return;
        }
        if (!item.isRemoved()) {
            CompatGameTestHelper.fail(helper, "The thrown item entity was not removed");
            return;
        }
        List<ItemStack> risen = takeRisen(helper, item, thrownAt);
        Item clockstone = ModItems.CLOCKSTONE.get();
        int rest = 0;
        int results = 0;
        for (ItemStack stack : risen) {
            if (stack.is(clockstone) && stack.getCount() == 63) {
                rest++;
            } else if ((stack.is(clockstone) && stack.getCount() == 2)
                    || (stack.is(ModItems.TIME_CRYSTAL.get()) && stack.getCount() == 1)) {
                results++;
            } else {
                CompatGameTestHelper.fail(helper, "Unexpected rising item: " + stack);
                return;
            }
        }
        if (rest != 1 || results != 1) {
            CompatGameTestHelper.fail(helper, "Expected one exchange result and 63 clockstone, got " + risen);
            return;
        }
        helper.succeed();
    }

    /**
     * Test: within the cooldown the same player's wish rises back unchanged, while another
     * player can still wish.
     */
    private static void testSecondWishReturnsUnchanged(GameTestHelper helper) {
        if (placeWell(helper) == null) {
            return;
        }
        Player first = CompatGameTestHelper.makeMockPlayer(helper);
        Player second = CompatGameTestHelper.makeMockPlayer(helper);
        if (!expectExchanged(helper, first, "First wish")) {
            return;
        }
        if (!expectReturned(helper, new ItemStack(ModItems.CLOCKSTONE.get()), first, TimeWellOutcome.COOLDOWN)) {
            return;
        }
        if (!expectExchanged(helper, second, "Wish by another player")) {
            return;
        }
        helper.succeed();
    }

    /**
     * Test: an item the well does not take rises back and does not start the cooldown.
     * This covers a vanilla item and a chronodawn item without an exchange table.
     */
    private static void testUnacceptedKeepsCooldownFree(GameTestHelper helper) {
        if (placeWell(helper) == null) {
            return;
        }
        Player player = CompatGameTestHelper.makeMockPlayer(helper);
        if (!expectReturned(helper, new ItemStack(Items.DIRT, 5), player, TimeWellOutcome.NOT_ACCEPTED)) {
            return;
        }
        if (!expectReturned(helper, new ItemStack(ModItems.TEMPORAL_STONE.get()), player,
                TimeWellOutcome.NOT_ACCEPTED)) {
            return;
        }
        if (!expectExchanged(helper, player, "Wish after unaccepted items")) {
            return;
        }
        helper.succeed();
    }

    /**
     * Test: when the thrower is gone the item rises back, and nobody's cooldown starts.
     */
    private static void testReturnsWhenThrowerGone(GameTestHelper helper) {
        if (placeWell(helper) == null) {
            return;
        }
        if (!expectReturned(helper, new ItemStack(ModItems.CLOCKSTONE.get()), null, TimeWellOutcome.THROWER_GONE)) {
            return;
        }
        if (!expectExchanged(helper, CompatGameTestHelper.makeMockPlayer(helper), "Wish after thrower gone")) {
            return;
        }
        helper.succeed();
    }

    /**
     * Test: an item nobody threw, such as one dropped by a mob or a hopper, is left alone.
     */
    private static void testIgnoresItemWithoutThrower(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        ItemEntity item = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        TimeWellStoneBlock.tick(helper.getLevel(), helper.absolutePos(STONE_POS), well);
        if (well.queue().size() != 0) {
            CompatGameTestHelper.fail(helper, "An item without a thrower was queued");
            return;
        }
        if (!item.isAlive()) {
            CompatGameTestHelper.fail(helper, "An item without a thrower was removed");
            return;
        }
        if (item.hasPickUpDelay()) {
            CompatGameTestHelper.fail(helper, "An item without a thrower got a pickup delay");
            return;
        }
        helper.succeed();
    }

    /**
     * Test: the production path. The stone's own ticker sees a clockstone thrown by a joined
     * player, answers it after the delay by looking the player up in the level, and exchanges it.
     */
    private static void testTickExchangesForJoinedPlayer(GameTestHelper helper) {
        if (placeWell(helper) == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper);
        boolean handedOver = false;
        try {
            long thrownAt = helper.getLevel().getGameTime();
            ItemEntity thrown = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
            CompatGameTestHelper.setThrower(thrown, player);
            expectTickExchangeAfterDelay(helper, player, thrown, thrownAt, "Thrown by a joined player");
            handedOver = true;
        } finally {
            if (!handedOver) {
                CompatGameTestHelper.removeMockServerPlayer(helper, player);
            }
        }
    }

    /**
     * Test: an item whose thrower is an entity in the level but not a player is left alone.
     */
    private static void testTickIgnoresNonPlayerThrower(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        // Built with its constructor, which every version has, because 26.2 moved the vanilla
        // EntityType constants out of EntityType.
        BlockPos standPos = helper.absolutePos(ARMOR_STAND_POS);
        ArmorStand stand = new ArmorStand(helper.getLevel(), standPos.getX() + 0.5, standPos.getY(),
            standPos.getZ() + 0.5);
        helper.getLevel().addFreshEntity(stand);
        ItemEntity item = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        CompatGameTestHelper.setThrower(item, stand);
        if (item.getOwner() != stand) {
            CompatGameTestHelper.fail(helper, "The item's owner did not resolve to the armor stand, got "
                + item.getOwner());
            return;
        }
        TimeWellStoneBlock.tick(helper.getLevel(), helper.absolutePos(STONE_POS), well);
        if (well.queue().contains(item.getUUID()) || TimeWellStoneBlock.isWaiting(item)) {
            CompatGameTestHelper.fail(helper, "An item thrown by an armor stand was queued");
            return;
        }
        if (!item.isAlive() || item.hasPickUpDelay()) {
            CompatGameTestHelper.fail(helper, "An item thrown by an armor stand was changed: alive="
                + item.isAlive() + ", pickup delay=" + item.hasPickUpDelay());
            return;
        }
        helper.succeed();
    }

    /**
     * Test: a waiting item cannot be picked up and does not merge with a matching stack
     * next to it, so the well still finds the same entity when the answer is due.
     */
    private static void testWaitingItemDoesNotMerge(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        Player player = CompatGameTestHelper.makeMockPlayer(helper);
        ItemEntity waiting = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        TimeWellStoneBlock.markWaiting(well, waiting, player, helper.getLevel().getGameTime());
        ItemEntity other = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        helper.runAfterDelay(MERGE_WAIT_TICKS, () -> {
            // Judge the merge by the waiting item alone. The GameTest world is reused across runs,
            // and entities of a chunk can load after the structure area was cleared, so the other
            // stack may merge into a clockstone left by an earlier run instead.
            boolean alive = waiting.isAlive();
            boolean pickUpDelay = waiting.hasPickUpDelay();
            int count = waiting.getItem().getCount();
            // Remove both items so they are not left behind for the next run.
            waiting.discard();
            other.discard();
            if (!pickUpDelay) {
                CompatGameTestHelper.fail(helper, "The waiting item can be picked up");
                return;
            }
            if (!alive || count != 1) {
                CompatGameTestHelper.fail(helper, "The waiting item merged: waiting alive=" + alive
                    + ", count=" + count);
                return;
            }
            helper.succeed();
        });
    }

    /**
     * Test: an item still tagged as waiting but missing from the queue, as after a chunk
     * reload, is queued again even without a player owner, so it is not stranded.
     */
    private static void testRequeuesOrphanedWaitingItem(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        ItemEntity item = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        item.addTag(TimeWellStoneBlock.WAITING_TAG);
        if (well.queue().size() != 0) {
            CompatGameTestHelper.fail(helper, "The queue was not empty before the tick");
            return;
        }
        TimeWellStoneBlock.tick(helper.getLevel(), helper.absolutePos(STONE_POS), well);
        if (well.queue().size() != 1 || !well.queue().contains(item.getUUID())) {
            CompatGameTestHelper.fail(helper, "The orphaned waiting item was not queued again, queue size "
                + well.queue().size());
            return;
        }
        helper.succeed();
    }

    /**
     * Test: a requeued orphan is answered as returned to a thrower who is gone. It rises as a
     * plain item: no waiting tag, and a normal pickup delay that runs out.
     */
    private static void testReturnsRequeuedOrphan(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos stone = helper.absolutePos(STONE_POS);
        long thrownAt = level.getGameTime();
        ItemEntity item = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        item.addTag(TimeWellStoneBlock.WAITING_TAG);
        TimeWellStoneBlock.tick(level, stone, well);
        if (!well.queue().contains(item.getUUID())) {
            CompatGameTestHelper.fail(helper, "The orphaned waiting item was not queued again");
            return;
        }
        TimeWellOutcome outcome = TimeWellStoneBlock.answer(level, stone, item, null);
        if (outcome != TimeWellOutcome.THROWER_GONE) {
            CompatGameTestHelper.fail(helper, "Expected THROWER_GONE for the orphan, got " + outcome);
            return;
        }
        List<ItemEntity> risen = findRisen(helper, item, 3, thrownAt);
        if (!item.isRemoved() || risen.size() != 1 || !risen.get(0).getItem().is(ModItems.CLOCKSTONE.get())
                || risen.get(0).getItem().getCount() != 1) {
            CompatGameTestHelper.fail(helper, "Expected the orphan to rise back as 1 clockstone, original removed="
                + item.isRemoved() + ", risen=" + risen.size());
            return;
        }
        ItemEntity returned = risen.get(0);
        if (TimeWellStoneBlock.isWaiting(returned)) {
            CompatGameTestHelper.fail(helper, "The returned item still carries the waiting tag");
            return;
        }
        helper.runAfterDelay(PICKUP_DELAY_WAIT_TICKS, () -> {
            if (returned.hasPickUpDelay()) {
                CompatGameTestHelper.fail(helper, "The returned item still cannot be picked up");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * Test: removing the water during the wait does not strand the waiting item. The thrower
     * is still in the level, so the wish is granted, and the result rises where the stone is.
     */
    private static void testAnswersAfterWaterRemoved(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper);
        boolean handedOver = false;
        try {
            long thrownAt = helper.getLevel().getGameTime();
            ItemEntity waiting = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
            TimeWellStoneBlock.markWaiting(well, waiting, player, thrownAt);
            helper.setBlock(WATER_POS, Blocks.AIR);
            expectTickExchangeAfterDelay(helper, player, waiting, thrownAt, "Water removed during the wait");
            handedOver = true;
        } finally {
            if (!handedOver) {
                CompatGameTestHelper.removeMockServerPlayer(helper, player);
            }
        }
    }

    /**
     * Test: breaking the stone during the wait releases the waiting item. It loses the tag at
     * once and can be picked up after a normal pickup delay.
     */
    private static void testStoneRemovalReleasesWaitingItem(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        ItemEntity waiting = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        TimeWellStoneBlock.markWaiting(well, waiting, CompatGameTestHelper.makeMockPlayer(helper),
            helper.getLevel().getGameTime());
        helper.setBlock(STONE_POS, Blocks.AIR);
        if (TimeWellStoneBlock.isWaiting(waiting)) {
            CompatGameTestHelper.fail(helper, "The waiting item kept its tag after the stone was removed");
            return;
        }
        helper.runAfterDelay(PICKUP_DELAY_WAIT_TICKS, () -> {
            if (!waiting.isAlive()) {
                CompatGameTestHelper.fail(helper, "The released item disappeared");
                return;
            }
            if (waiting.hasPickUpDelay()) {
                CompatGameTestHelper.fail(helper, "The released item still cannot be picked up");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * Test: breaking the stone gives the plain mossy temporal cobblestone, so the activator
     * cannot be collected.
     */
    private static void testDrops(GameTestHelper helper) {
        helper.setBlock(STONE_POS, ModBlocks.TIME_WELL_STONE.get());
        BlockPos pos = helper.absolutePos(STONE_POS);
        BlockState state = helper.getLevel().getBlockState(pos);
        LootParams.Builder builder = new LootParams.Builder(helper.getLevel())
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.TOOL, new ItemStack(Items.WOODEN_PICKAXE));
        List<ItemStack> drops = state.getDrops(builder);
        if (drops.size() == 1 && drops.get(0).is(ModBlocks.MOSSY_TEMPORAL_COBBLESTONE.get().asItem())) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "time_well_stone should drop one mossy_temporal_cobblestone, got " + drops);
        }
    }

    /**
     * Test: a risen item that falls back into the shaft is queued again as returned to a
     * thrower who is gone, and is raised again after the answer delay. That answer uses nobody's
     * cooldown, so the original thrower can still wish afterwards.
     */
    private static void testRaisesAgainRisenItemFallenBack(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos stone = helper.absolutePos(STONE_POS);
        ServerPlayer player = joinPlayer(helper);
        boolean handedOver = false;
        try {
            long thrownAt = level.getGameTime();
            ItemEntity thrown = spawnInWater(helper, new ItemStack(Items.DIRT, 5));
            CompatGameTestHelper.setThrower(thrown, player);
            TimeWellOutcome outcome = TimeWellStoneBlock.answer(level, stone, thrown, player);
            if (outcome != TimeWellOutcome.NOT_ACCEPTED) {
                CompatGameTestHelper.fail(helper, "Expected NOT_ACCEPTED, got " + outcome);
                return;
            }
            List<ItemEntity> risen = findRisen(helper, thrown, 3, thrownAt);
            if (risen.size() != 1 || !TimeWellStoneBlock.isRisen(risen.get(0))) {
                CompatGameTestHelper.fail(helper, "Expected one risen stack with the risen tag, got " + risen.size()
                    + (risen.isEmpty() ? "" : ", tagged=" + TimeWellStoneBlock.isRisen(risen.get(0))));
                return;
            }
            ItemEntity fallen = risen.get(0);
            BlockPos water = helper.absolutePos(WATER_POS);
            fallen.setPos(water.getX() + 0.5, water.getY() + 0.5, water.getZ() + 0.5);
            fallen.setDeltaMovement(Vec3.ZERO);
            long fellAt = level.getGameTime();
            TimeWellStoneBlock.tick(level, stone, well);
            if (!well.queue().contains(fallen.getUUID()) || !TimeWellStoneBlock.isWaiting(fallen)
                    || TimeWellStoneBlock.isRisen(fallen)) {
                CompatGameTestHelper.fail(helper, "The fallen item was not queued again: queued="
                    + well.queue().contains(fallen.getUUID()) + ", waiting=" + TimeWellStoneBlock.isWaiting(fallen)
                    + ", still tagged risen=" + TimeWellStoneBlock.isRisen(fallen));
                return;
            }
            handedOver = true;
            helper.runAfterDelay(ANSWER_WAIT_TICKS, () -> {
                try {
                    expectRaisedAgain(helper, player, fallen, fellAt);
                } finally {
                    CompatGameTestHelper.removeMockServerPlayer(helper, player);
                }
            });
        } finally {
            if (!handedOver) {
                CompatGameTestHelper.removeMockServerPlayer(helper, player);
            }
        }
    }

    /**
     * Checks that the fallen item was answered once, no sooner than the answer delay, by a
     * fresh tagged stack, and that the thrower's cooldown is still free.
     */
    private static void expectRaisedAgain(GameTestHelper helper, Player player, ItemEntity fallen, long fellAt) {
        ServerLevel level = helper.getLevel();
        if (!fallen.isRemoved()) {
            CompatGameTestHelper.fail(helper, "The fallen item was not raised again");
            return;
        }
        List<ItemEntity> again = new ArrayList<>(findRisen(helper, fallen, 3, fellAt));
        again.removeIf(e -> !e.getItem().is(Items.DIRT));
        if (again.size() != 1 || again.get(0).getItem().getCount() != 5) {
            CompatGameTestHelper.fail(helper, "Expected 5 dirt to rise again, got " + again.size() + " stacks");
            return;
        }
        ItemEntity raised = again.get(0);
        long raisedAt = level.getGameTime() - raised.getAge();
        if (raisedAt - fellAt < WellQueue.ANSWER_DELAY_TICKS) {
            CompatGameTestHelper.fail(helper, "The fallen item was raised again after " + (raisedAt - fellAt)
                + " ticks, before the answer delay");
            return;
        }
        if (!TimeWellStoneBlock.isRisen(raised) || TimeWellStoneBlock.isWaiting(raised)) {
            CompatGameTestHelper.fail(helper, "The stack raised again should carry only the risen tag: risen="
                + TimeWellStoneBlock.isRisen(raised) + ", waiting=" + TimeWellStoneBlock.isWaiting(raised));
            return;
        }
        raised.discard();
        if (!expectExchanged(helper, player, "Wish after the item was raised again")) {
            return;
        }
        helper.succeed();
    }

    /**
     * Test: in the real template, with the thrower standing on the roofed side, a returned item
     * still lands outside the well. Tossed straight at the thrower it would hit the roof slab
     * and fall back into the shaft. Runs once for each rotation the feature can place.
     */
    private static void testRoofSideReturnLandsOutside(GameTestHelper helper, Rotation rotation,
                                                        boolean blockAboveOpenSide) {
        ServerLevel level = helper.getLevel();
        // Ground level with the top of the template's lowest layer, as the feature's y_offset
        // of -1 places it.
        for (int x = 0; x < FLOOR_SIZE; x++) {
            for (int z = 0; z < FLOOR_SIZE; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
            }
        }
        Optional<StructureTemplate> template = CompatGameTestHelper.loadModStructureTemplate(helper, "time_well");
        if (template.isEmpty()) {
            CompatGameTestHelper.fail(helper, "Structure template 'time_well' could not be loaded");
            return;
        }
        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setRotation(rotation)
            .setRotationPivot(TEMPLATE_PIVOT);
        BlockPos origin = helper.absolutePos(FULL_WELL_ORIGIN);
        template.get().placeInWorld(level, origin, origin, settings, level.getRandom(), 2);

        BlockPos water = helper.absolutePos(FULL_WELL_WATER_POS);
        BlockPos stone = water.relative(rotation.rotate(TEMPLATE_STONE_SIDE));
        Direction roofSide = rotation.rotate(TEMPLATE_ROOF_SIDE);
        BlockPos roof = water.relative(roofSide).above(ROOF_HEIGHT_ABOVE_WATER);
        // With blockAboveOpenSide the thrower stands on a side that is open, with a solid block
        // at water + 4. That is above the toss (its top is at most water + 3.69), so the launch
        // must still go toward the thrower rather than to another side. The test runs unrotated
        // on the east side because a toss to the west would land past the edge of the floor.
        Direction throwSide = blockAboveOpenSide ? roofSide.getCounterClockWise() : roofSide;
        if (blockAboveOpenSide) {
            level.setBlock(water.relative(throwSide).above(ABOVE_TOSS_HEIGHT), Blocks.STONE.defaultBlockState(), 2);
        }
        if (!level.getBlockState(roof).is(ModBlocks.TIME_WOOD_SLAB.get())
                || !(level.getBlockEntity(stone) instanceof TimeWellStoneBlockEntity)) {
            CompatGameTestHelper.fail(helper, rotation + ": expected the roof slab at " + roof + " and the stone at "
                + stone + ", got " + level.getBlockState(roof) + " and " + level.getBlockState(stone));
            return;
        }
        ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
        boolean handedOver = false;
        try {
            Vec3 standAt = Vec3.atBottomCenterOf(water.relative(throwSide, ROOF_THROWER_DISTANCE));
            player.setPos(standAt.x, standAt.y, standAt.z);
            long thrownAt = level.getGameTime();
            ItemEntity thrown = new ItemEntity(level, water.getX() + 0.5, water.getY() + 0.5, water.getZ() + 0.5,
                new ItemStack(Items.DIRT, 5));
            level.addFreshEntity(thrown);
            CompatGameTestHelper.setThrower(thrown, player);
            TimeWellOutcome outcome = TimeWellStoneBlock.answer(level, stone, thrown, player);
            if (outcome != TimeWellOutcome.NOT_ACCEPTED) {
                CompatGameTestHelper.fail(helper, rotation + ": expected NOT_ACCEPTED, got " + outcome);
                return;
            }
            handedOver = true;
            helper.runAfterDelay(LANDING_WAIT_TICKS, () -> {
                try {
                    expectLandedOutside(helper, water, thrownAt, rotation,
                        blockAboveOpenSide ? throwSide : null);
                } finally {
                    CompatGameTestHelper.removeMockServerPlayer(helper, player);
                }
            });
        } finally {
            if (!handedOver) {
                CompatGameTestHelper.removeMockServerPlayer(helper, player);
            }
        }
    }

    /**
     * Checks that the dirt returned since the throw lies outside the well's rim, then removes
     * it so it is not left in the reused GameTest world.
     */
    private static void expectLandedOutside(GameTestHelper helper, BlockPos water, long thrownAt,
                                            Rotation rotation, Direction expectedSide) {
        ServerLevel level = helper.getLevel();
        List<ItemEntity> returned = level.getEntitiesOfClass(ItemEntity.class,
            new AABB(water).inflate(LANDING_SEARCH_RANGE),
            e -> e.isAlive() && e.getItem().is(Items.DIRT) && level.getGameTime() - e.getAge() >= thrownAt);
        try {
            if (returned.size() != 1) {
                CompatGameTestHelper.fail(helper, rotation + ": expected one returned stack, got " + returned.size());
                return;
            }
            ItemEntity item = returned.get(0);
            double fromCentreX = Math.abs(item.getX() - (water.getX() + 0.5));
            double fromCentreZ = Math.abs(item.getZ() - (water.getZ() + 0.5));
            if (item.blockPosition().equals(water)
                    || (fromCentreX < RIM_OUTER_HALF_WIDTH && fromCentreZ < RIM_OUTER_HALF_WIDTH)) {
                CompatGameTestHelper.fail(helper, rotation + ": the returned item fell back into the well, at "
                    + item.position().subtract(water.getX() + 0.5, water.getY(), water.getZ() + 0.5)
                    + " from the water surface centre");
                return;
            }
            if (expectedSide != null) {
                double along = (item.getX() - (water.getX() + 0.5)) * expectedSide.getStepX()
                    + (item.getZ() - (water.getZ() + 0.5)) * expectedSide.getStepZ();
                if (along < RIM_OUTER_HALF_WIDTH) {
                    CompatGameTestHelper.fail(helper, rotation + ": the item should land outside the " + expectedSide
                        + " side but is " + along + " blocks along it from the centre");
                    return;
                }
            }
            helper.succeed();
        } finally {
            returned.forEach(ItemEntity::discard);
        }
    }
}
