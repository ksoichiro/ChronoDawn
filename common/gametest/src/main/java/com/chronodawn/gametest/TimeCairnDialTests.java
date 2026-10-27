package com.chronodawn.gametest;

import com.chronodawn.blocks.TimeCairnDialBlock;
import com.chronodawn.blocks.TimeCairnDialBlock.TurnResult;
import com.chronodawn.blocks.TimeCairnDialBlockEntity;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.compat.CompatRecipeManager;
import com.chronodawn.compat.CompatResourceLocation;
import com.chronodawn.core.wayside.CairnHour;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * GameTests for the Time Cairn wayside feature: the dial, the stones and the keystone.
 */
public class TimeCairnDialTests {

    private static final BlockPos DIAL_POS = new BlockPos(1, 2, 1);
    private static final long COOLDOWN = 24000;
    // Rolls per check. The reward pools are random, so one roll could miss a bad entry.
    private static final int ROLLS = 30;
    private static final int MAIN_INVENTORY_SLOTS = 36;
    // Day times that fall in the middle of each hour, in CairnHour order.
    private static final long[] HOUR_DAY_TIMES = {0L, 6000L, 12000L, 18000L};

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("recipe_loaded_cairn_keystone",
            TimeCairnDialTests::testKeystoneRecipeLoaded));
        tests.add(factory.create("time_cairn_dial_has_block_entity",
            TimeCairnDialTests::testHasBlockEntity));
        tests.add(factory.create("time_cairn_turn_gives_stone_for_each_hour",
            TimeCairnDialTests::testStoneForEachHour));
        tests.add(factory.create("time_cairn_turn_refuses_within_cooldown",
            TimeCairnDialTests::testRefusesWithinCooldown));
        tests.add(factory.create("time_cairn_turn_spawns_item_with_full_inventory",
            TimeCairnDialTests::testSpawnsItemWithFullInventory));
        tests.add(factory.create("time_cairn_keystone_in_either_hand",
            TimeCairnDialTests::testKeystoneInEitherHand));
        tests.add(factory.create("time_cairn_keystone_ignores_cooldown",
            TimeCairnDialTests::testKeystoneIgnoresCooldown));
        tests.add(factory.create("time_cairn_keystone_reward_table_rolls",
            TimeCairnDialTests::testKeystoneRewardRolls));
        tests.add(factory.create("time_cairn_dial_drops_clockwork_dial",
            TimeCairnDialTests::testDrops));
        return tests;
    }

    /**
     * Places the dial and returns its block entity, or null after failing the test.
     */
    private static TimeCairnDialBlockEntity placeDial(GameTestHelper helper) {
        helper.setBlock(DIAL_POS, ModBlocks.TIME_CAIRN_DIAL.get());
        if (helper.getLevel().getBlockEntity(helper.absolutePos(DIAL_POS)) instanceof TimeCairnDialBlockEntity dial) {
            return dial;
        }
        CompatGameTestHelper.fail(helper, "time_cairn_dial has no TimeCairnDialBlockEntity");
        return null;
    }

    /**
     * Collects the items that appeared at the dial since the given game time and removes them,
     * so the next step of the test starts from a clean dial. Older items are skipped: the
     * GameTest world is reused across runs, so items left by an earlier run can lie nearby.
     */
    private static List<ItemStack> takeDropped(GameTestHelper helper, long since) {
        ServerLevel level = helper.getLevel();
        List<ItemEntity> found = level.getEntitiesOfClass(ItemEntity.class,
            new AABB(helper.absolutePos(DIAL_POS)).inflate(3),
            e -> e.isAlive() && level.getGameTime() - e.getAge() >= since);
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : found) {
            stacks.add(entity.getItem().copy());
            entity.discard();
        }
        return stacks;
    }

    private static boolean isCairnStone(ItemStack stack) {
        for (CairnHour hour : CairnHour.values()) {
            if (stack.is(ModItems.cairnStone(hour))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCairnStone(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (isCairnStone(player.getInventory().getItem(slot))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test: the placed dial carries its block entity.
     */
    private static void testHasBlockEntity(GameTestHelper helper) {
        if (placeDial(helper) == null) {
            return;
        }
        helper.succeed();
    }

    /**
     * Test: a turn at each quarter of the day gives exactly one stone engraved with that hour.
     */
    private static void testStoneForEachHour(GameTestHelper helper) {
        if (placeDial(helper) == null) {
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(DIAL_POS);
        CairnHour[] hours = CairnHour.values();
        // The echo is rolled at midnight, so pin it off to keep the result GIVEN.
        TimeCairnDialBlock.forceEchoChance(0.0f);
        try {
            for (int i = 0; i < hours.length; i++) {
                ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
                try {
                    long since = level.getGameTime();
                    TurnResult result = TimeCairnDialBlock.turn(level, pos, player, HOUR_DAY_TIMES[i]);
                    List<ItemStack> dropped = takeDropped(helper, since);
                    if (result != TurnResult.GIVEN) {
                        CompatGameTestHelper.fail(helper, hours[i] + ": expected GIVEN, got " + result);
                        return;
                    }
                    if (dropped.size() != 1 || !dropped.get(0).is(ModItems.cairnStone(hours[i]))
                            || dropped.get(0).getCount() != 1) {
                        CompatGameTestHelper.fail(helper, hours[i] + ": expected one " + hours[i].stonePath()
                            + ", got " + dropped);
                        return;
                    }
                } finally {
                    CompatGameTestHelper.removeMockServerPlayer(helper, player);
                }
            }
        } finally {
            TimeCairnDialBlock.forceEchoChance(null);
        }
        helper.succeed();
    }

    /**
     * Test: the same player is refused within the cooldown and gets nothing, while another
     * player can still turn the dial.
     */
    private static void testRefusesWithinCooldown(GameTestHelper helper) {
        if (placeDial(helper) == null) {
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(DIAL_POS);
        ServerPlayer first = CompatGameTestHelper.makeMockServerPlayer(helper);
        ServerPlayer second = CompatGameTestHelper.makeMockServerPlayer(helper);
        TimeCairnDialBlock.forceEchoChance(0.0f);
        try {
            long since = level.getGameTime();
            TurnResult firstTurn = TimeCairnDialBlock.turn(level, pos, first, 6000L);
            takeDropped(helper, since);
            if (firstTurn != TurnResult.GIVEN) {
                CompatGameTestHelper.fail(helper, "First turn: expected GIVEN, got " + firstTurn);
                return;
            }
            TurnResult again = TimeCairnDialBlock.turn(level, pos, first, 6000L);
            List<ItemStack> droppedAgain = takeDropped(helper, since);
            if (again != TurnResult.COOLDOWN) {
                CompatGameTestHelper.fail(helper, "Second turn by the same player: expected COOLDOWN, got " + again);
                return;
            }
            if (!droppedAgain.isEmpty()) {
                CompatGameTestHelper.fail(helper, "A refused turn dropped " + droppedAgain);
                return;
            }
            TurnResult other = TimeCairnDialBlock.turn(level, pos, second, 6000L);
            List<ItemStack> droppedOther = takeDropped(helper, since);
            if (other != TurnResult.GIVEN || droppedOther.size() != 1) {
                CompatGameTestHelper.fail(helper, "Turn by another player: expected GIVEN and one stone, got "
                    + other + " and " + droppedOther);
                return;
            }
            helper.succeed();
        } finally {
            TimeCairnDialBlock.forceEchoChance(null);
            CompatGameTestHelper.removeMockServerPlayer(helper, first);
            CompatGameTestHelper.removeMockServerPlayer(helper, second);
        }
    }

    /**
     * Test: a player whose inventory is full still gets the stone as an item entity, and
     * nothing goes into the inventory.
     */
    private static void testSpawnsItemWithFullInventory(GameTestHelper helper) {
        if (placeDial(helper) == null) {
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(DIAL_POS);
        ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
        TimeCairnDialBlock.forceEchoChance(0.0f);
        try {
            for (int slot = 0; slot < MAIN_INVENTORY_SLOTS; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Items.STONE));
            }
            long since = level.getGameTime();
            TurnResult result = TimeCairnDialBlock.turn(level, pos, player, 6000L);
            List<ItemStack> dropped = takeDropped(helper, since);
            if (result != TurnResult.GIVEN) {
                CompatGameTestHelper.fail(helper, "Expected GIVEN, got " + result);
                return;
            }
            if (dropped.size() != 1 || !isCairnStone(dropped.get(0))) {
                CompatGameTestHelper.fail(helper, "Expected one cairn stone entity, got " + dropped);
                return;
            }
            if (hasCairnStone(player)) {
                CompatGameTestHelper.fail(helper, "The stone went into the inventory");
                return;
            }
            helper.succeed();
        } finally {
            TimeCairnDialBlock.forceEchoChance(null);
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: the keystone works from the main hand and from the off hand, leaves the other hand
     * alone, and does nothing when neither hand holds one.
     */
    private static void testKeystoneInEitherHand(GameTestHelper helper) {
        if (placeDial(helper) == null) {
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(DIAL_POS);
        Item keystone = ModItems.CAIRN_KEYSTONE.get();
        ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
        try {
            // Main hand, two keystones: one is consumed.
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(keystone, 2));
            long since = level.getGameTime();
            boolean mainSet = TimeCairnDialBlock.setKeystone(level, pos, player);
            List<ItemStack> mainDropped = takeDropped(helper, since);
            if (!mainSet || player.getMainHandItem().getCount() != 1 || mainDropped.isEmpty()
                    || mainDropped.stream().anyMatch(TimeCairnDialTests::isCairnStone)) {
                CompatGameTestHelper.fail(helper, "Main hand keystone: set=" + mainSet + ", left="
                    + player.getMainHandItem().getCount() + ", dropped=" + mainDropped);
                return;
            }
            // Off hand with a sword in the main hand: the sword is untouched.
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(keystone, 1));
            since = level.getGameTime();
            boolean offSet = TimeCairnDialBlock.setKeystone(level, pos, player);
            List<ItemStack> offDropped = takeDropped(helper, since);
            if (!offSet || !player.getOffhandItem().isEmpty() || !player.getMainHandItem().is(Items.IRON_SWORD)
                    || player.getMainHandItem().getCount() != 1 || offDropped.isEmpty()
                    || offDropped.stream().anyMatch(TimeCairnDialTests::isCairnStone)) {
                CompatGameTestHelper.fail(helper, "Off hand keystone: set=" + offSet + ", offhand="
                    + player.getOffhandItem() + ", mainhand=" + player.getMainHandItem() + ", dropped=" + offDropped);
                return;
            }
            // No keystone at all.
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            since = level.getGameTime();
            boolean noneSet = TimeCairnDialBlock.setKeystone(level, pos, player);
            List<ItemStack> noneDropped = takeDropped(helper, since);
            if (noneSet || !noneDropped.isEmpty()) {
                CompatGameTestHelper.fail(helper, "Without a keystone: set=" + noneSet + ", dropped=" + noneDropped);
                return;
            }
            helper.succeed();
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: setting a keystone does not touch the turn cooldown. The remaining time after a
     * turn is the same, or only shorter with time, once the keystone has been set.
     */
    private static void testKeystoneIgnoresCooldown(GameTestHelper helper) {
        TimeCairnDialBlockEntity dial = placeDial(helper);
        if (dial == null) {
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(DIAL_POS);
        ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
        TimeCairnDialBlock.forceEchoChance(0.0f);
        try {
            long since = level.getGameTime();
            TurnResult turn = TimeCairnDialBlock.turn(level, pos, player, 6000L);
            if (turn != TurnResult.GIVEN) {
                CompatGameTestHelper.fail(helper, "Turn: expected GIVEN, got " + turn);
                return;
            }
            // A refused call does not reset the cooldown, so it only reads the time left.
            long before = dial.tryActivate(player.getUUID(), level.getGameTime(), COOLDOWN);
            if (before <= 0) {
                CompatGameTestHelper.fail(helper, "The turn left no cooldown to compare, got " + before);
                return;
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CAIRN_KEYSTONE.get()));
            boolean set = TimeCairnDialBlock.setKeystone(level, pos, player);
            takeDropped(helper, since);
            if (!set) {
                CompatGameTestHelper.fail(helper, "The keystone was refused after a turn");
                return;
            }
            long after = dial.tryActivate(player.getUUID(), level.getGameTime(), COOLDOWN);
            if (after <= 0 || after > before) {
                CompatGameTestHelper.fail(helper, "The keystone changed the cooldown: before=" + before
                    + ", after=" + after);
                return;
            }
            helper.succeed();
        } finally {
            TimeCairnDialBlock.forceEchoChance(null);
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: every keystone reward is two or three stacks of the table's items.
     */
    private static void testKeystoneRewardRolls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(DIAL_POS);
        Set<Item> allowed = Set.of(
            ModItems.TIME_CRYSTAL.get(), ModItems.CLOCKSTONE.get(), ModItems.ENTROPY_CRYSTAL.get(),
            ModItems.ENHANCED_CLOCKSTONE.get(), ModItems.ANCIENT_GEAR.get(), ModItems.CLOCKWORK_COOKIE.get(),
            ModItems.BAKED_TEMPORAL_ROOT.get(), Items.TORCH);
        for (int roll = 0; roll < ROLLS; roll++) {
            List<ItemStack> reward = TimeCairnDialBlock.rollKeystoneReward(level, pos, null);
            if (reward.size() < 2 || reward.size() > 3) {
                CompatGameTestHelper.fail(helper, "Expected 2 to 3 stacks, got " + reward);
                return;
            }
            for (ItemStack stack : reward) {
                if (!allowed.contains(stack.getItem())) {
                    CompatGameTestHelper.fail(helper, "Unexpected keystone reward item: " + stack);
                    return;
                }
            }
        }
        helper.succeed();
    }

    /**
     * Test: breaking the dial gives the plain clockwork dial, so the activator cannot be collected.
     */
    private static void testDrops(GameTestHelper helper) {
        helper.setBlock(DIAL_POS, ModBlocks.TIME_CAIRN_DIAL.get());
        BlockPos pos = helper.absolutePos(DIAL_POS);
        BlockState state = helper.getLevel().getBlockState(pos);
        LootParams.Builder builder = new LootParams.Builder(helper.getLevel())
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.TOOL, new ItemStack(Items.IRON_PICKAXE));
        List<ItemStack> drops = state.getDrops(builder);
        if (drops.size() == 1 && drops.get(0).is(ModBlocks.CLOCKWORK_DIAL.get().asItem())) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "time_cairn_dial should drop one clockwork_dial, got " + drops);
        }
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
