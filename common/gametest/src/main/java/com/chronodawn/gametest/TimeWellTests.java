package com.chronodawn.gametest;

import com.chronodawn.blocks.TimeWellStoneBlock;
import com.chronodawn.blocks.TimeWellStoneBlockEntity;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.core.wayside.TimeWellOutcome;
import com.chronodawn.core.wayside.WellQueue;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
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
            if (!waiting.hasPickUpDelay()) {
                CompatGameTestHelper.fail(helper, "The waiting item can be picked up");
                return;
            }
            if (!waiting.isAlive() || !other.isAlive()) {
                CompatGameTestHelper.fail(helper, "The waiting item merged: waiting alive=" + waiting.isAlive()
                    + ", other alive=" + other.isAlive());
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
     * Test: removing the water during the wait does not strand the waiting item. It is still
     * answered and rises back out.
     */
    private static void testAnswersAfterWaterRemoved(GameTestHelper helper) {
        TimeWellStoneBlockEntity well = placeWell(helper);
        if (well == null) {
            return;
        }
        long thrownAt = helper.getLevel().getGameTime();
        ItemEntity waiting = spawnInWater(helper, new ItemStack(ModItems.CLOCKSTONE.get()));
        TimeWellStoneBlock.markWaiting(well, waiting, CompatGameTestHelper.makeMockPlayer(helper), thrownAt);
        helper.setBlock(WATER_POS, Blocks.AIR);
        helper.runAfterDelay(ANSWER_WAIT_TICKS, () -> {
            if (!waiting.isRemoved()) {
                CompatGameTestHelper.fail(helper, "The waiting item was not answered after the water was removed");
                return;
            }
            List<ItemEntity> risen = findRisen(helper, waiting, 4, thrownAt);
            if (risen.size() != 1 || !risen.get(0).getItem().is(ModItems.CLOCKSTONE.get())
                    || TimeWellStoneBlock.isWaiting(risen.get(0))) {
                CompatGameTestHelper.fail(helper, "Expected 1 plain clockstone to rise back, found " + risen.size());
                return;
            }
            helper.succeed();
        });
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
}
