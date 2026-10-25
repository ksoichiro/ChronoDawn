package com.chronodawn.gametest;

import com.chronodawn.blocks.WatchmakerChestBlock;
import com.chronodawn.blocks.WatchmakerChestBlockEntity;
import com.chronodawn.blocks.WatchmakerChestMenu;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.core.wayside.CampChestOutcome;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.worldgen.features.WatchmakerCampChests;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WatchmakerChestTests {

    private static final BlockPos CHEST_POS = new BlockPos(1, 2, 1);
    // A second chest for the persistence round trip, clear of the first.
    private static final BlockPos COPY_POS = new BlockPos(5, 2, 1);
    // Beside the chest, well within reach, so an open menu stays valid.
    private static final BlockPos PLAYER_POS = new BlockPos(1, 2, 2);
    private static final BlockPos SECOND_PLAYER_POS = new BlockPos(2, 2, 1);
    // Two blocks from the chest, so a vibration reaches it within a few ticks.
    private static final BlockPos SENSOR_POS = new BlockPos(3, 2, 1);
    // Enough for the vibration to travel and the sensor to switch to active.
    private static final int VIBRATION_TICKS = 5;
    // A sensor stays active for 30 ticks and then cools down for 10 before it listens again.
    private static final int SENSOR_RESET_TICKS = 50;
    // The camp is 5x3x5 and rotates around its centre column, so it stays inside the 10x10 area.
    private static final BlockPos CAMP_ORIGIN = new BlockPos(2, 1, 2);
    private static final long T0 = 1000L;
    private static final long CD = 72000L;
    private static final int DROP_SEARCH_RANGE = 3;
    private static final int DIRT_COUNT = 5;
    private static final int CHEST_SLOTS = WatchmakerChestBlockEntity.SLOTS;
    // Menu indices after the 27 chest slots: 27 main inventory slots, then the 9 hotbar slots.
    private static final int INVENTORY_START = CHEST_SLOTS;
    private static final int MENU_END = CHEST_SLOTS + 36;

    private WatchmakerChestTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("watchmaker_chest_first_open_fills",
            WatchmakerChestTests::testFirstOpenFills));
        tests.add(factory.create("watchmaker_chest_waits_when_empty",
            WatchmakerChestTests::testWaitsWhenEmpty));
        tests.add(factory.create("watchmaker_chest_refills_when_empty_after_cooldown",
            WatchmakerChestTests::testRefillsWhenEmptyAfterCooldown));
        tests.add(factory.create("watchmaker_chest_leftover_blocks_refill",
            WatchmakerChestTests::testLeftoverBlocksRefill));
        tests.add(factory.create("watchmaker_chest_spectator_does_not_fill",
            WatchmakerChestTests::testSpectatorDoesNotFill));
        tests.add(factory.create("watchmaker_chest_is_per_player",
            WatchmakerChestTests::testIsPerPlayer));
        tests.add(factory.create("watchmaker_chest_is_take_only",
            WatchmakerChestTests::testIsTakeOnly));
        tests.add(factory.create("watchmaker_chest_persists_slots_and_cooldown",
            WatchmakerChestTests::testPersistsSlotsAndCooldown));
        tests.add(factory.create("watchmaker_chest_breaking_drops_own_slots",
            WatchmakerChestTests::testBreakingDropsOwnSlots));
        tests.add(factory.create("watchmaker_chest_removal_without_player_drops_chest_only",
            WatchmakerChestTests::testRemovalWithoutPlayerDropsChestOnly));
        tests.add(factory.create("watchmaker_chest_is_not_a_container",
            WatchmakerChestTests::testIsNotAContainer));
        tests.add(factory.create("watchmaker_chest_open_and_close_trigger_sculk_sensor",
            WatchmakerChestTests::testOpenAndCloseTriggerSculkSensor));
        tests.add(factory.create("watchmaker_camp_swaps_chest_without_lootr",
            WatchmakerChestTests::testCampSwapsChestWithoutLootr));
        return tests;
    }

    /**
     * Places a Watchmaker's Chest and returns its block entity, or null after failing the test.
     */
    private static WatchmakerChestBlockEntity placeChest(GameTestHelper helper, BlockPos relativePos) {
        helper.setBlock(relativePos, ModBlocks.WATCHMAKER_CHEST.get());
        if (helper.getLevel().getBlockEntity(helper.absolutePos(relativePos))
                instanceof WatchmakerChestBlockEntity chest) {
            return chest;
        }
        CompatGameTestHelper.fail(helper, "watchmaker_chest has no WatchmakerChestBlockEntity");
        return null;
    }

    private static ServerPlayer joinPlayer(GameTestHelper helper, BlockPos relativePos) {
        ServerPlayer player = CompatGameTestHelper.makeMockServerPlayer(helper);
        Vec3 pos = Vec3.atBottomCenterOf(helper.absolutePos(relativePos));
        player.setPos(pos.x, pos.y, pos.z);
        return player;
    }

    private static CampChestOutcome use(GameTestHelper helper, ServerPlayer player, long gameTime) {
        return WatchmakerChestBlock.use(helper.getLevel(), helper.absolutePos(CHEST_POS), player, gameTime, CD);
    }

    /**
     * Opens the chest and closes the screen again, as a player looking in and walking away.
     */
    private static CampChestOutcome useAndClose(GameTestHelper helper, ServerPlayer player, long gameTime) {
        CampChestOutcome outcome = use(helper, player, gameTime);
        player.closeContainer();
        return outcome;
    }

    /**
     * @return copies of the non-empty stacks the player has in the chest, in slot order
     */
    private static List<ItemStack> stored(WatchmakerChestBlockEntity chest, UUID player) {
        Container slots = chest.slotsFor(player);
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < slots.getContainerSize(); i++) {
            if (!slots.getItem(i).isEmpty()) {
                result.add(slots.getItem(i).copy());
            }
        }
        return result;
    }

    private static String describe(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + " x" + stack.getCount();
    }

    /**
     * @return the stacks as sorted "id xcount" strings, so two lists compare regardless of order
     */
    private static List<String> describeSorted(List<ItemStack> stacks) {
        List<String> result = new ArrayList<>();
        for (ItemStack stack : stacks) {
            result.add(describe(stack));
        }
        result.sort(null);
        return result;
    }

    /**
     * Test: the first open of a player rolls the camp table into their slots and opens the menu.
     */
    private static void testFirstOpenFills(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            CampChestOutcome outcome = use(helper, player, T0);
            if (outcome != CampChestOutcome.FILLED) {
                CompatGameTestHelper.fail(helper, "Expected FILLED on the first open, got " + outcome);
                return;
            }
            List<ItemStack> items = stored(chest, player.getUUID());
            if (items.isEmpty()) {
                CompatGameTestHelper.fail(helper, "The first open stored no items");
                return;
            }
            for (ItemStack stack : items) {
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (!WatchmakerCampLootTests.STOCKPILE_ITEMS.contains(id)
                        && !WatchmakerCampLootTests.SUPPLY_ITEMS.contains(id)) {
                    CompatGameTestHelper.fail(helper, "Stored item " + id + " is not from the camp table");
                    return;
                }
            }
            if (!(player.containerMenu instanceof WatchmakerChestMenu)) {
                CompatGameTestHelper.fail(helper, "Expected an open WatchmakerChestMenu, got " + player.containerMenu);
                return;
            }
            helper.succeed();
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: an emptied chest during the cooldown refuses, stays empty and opens no menu.
     */
    private static void testWaitsWhenEmpty(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            useAndClose(helper, player, T0);
            chest.slotsFor(player.getUUID()).clearContent();
            CampChestOutcome outcome = use(helper, player, T0 + CD - 1);
            if (outcome != CampChestOutcome.WAITING) {
                CompatGameTestHelper.fail(helper, "Expected WAITING one tick before the cooldown ends, got " + outcome);
                return;
            }
            if (!chest.isEmptyFor(player.getUUID())) {
                CompatGameTestHelper.fail(helper, "A refused open added items: " + stored(chest, player.getUUID()));
                return;
            }
            if (player.containerMenu instanceof WatchmakerChestMenu) {
                CompatGameTestHelper.fail(helper, "A refused open opened the chest menu");
                return;
            }
            helper.succeed();
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: an emptied chest fills again once the cooldown has passed.
     */
    private static void testRefillsWhenEmptyAfterCooldown(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            useAndClose(helper, player, T0);
            chest.slotsFor(player.getUUID()).clearContent();
            CampChestOutcome outcome = useAndClose(helper, player, T0 + CD);
            if (outcome != CampChestOutcome.FILLED) {
                CompatGameTestHelper.fail(helper, "Expected FILLED once the cooldown has passed, got " + outcome);
                return;
            }
            if (chest.isEmptyFor(player.getUUID())) {
                CompatGameTestHelper.fail(helper, "The refill stored no items");
                return;
            }
            helper.succeed();
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: one item left in the chest blocks every refill, however long it has been, and does
     * not use up the cooldown, so the open after emptying it fills at once.
     */
    private static void testLeftoverBlocksRefill(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            UUID id = player.getUUID();
            useAndClose(helper, player, T0);
            List<ItemStack> filled = stored(chest, id);
            if (filled.isEmpty()) {
                CompatGameTestHelper.fail(helper, "The first open stored no items");
                return;
            }
            ItemStack kept = filled.get(0);
            Container slots = chest.slotsFor(id);
            slots.clearContent();
            slots.setItem(0, kept.copy());
            CampChestOutcome outcome = useAndClose(helper, player, T0 + 10 * CD);
            if (outcome != CampChestOutcome.OPENED) {
                CompatGameTestHelper.fail(helper, "Expected OPENED with a leftover item, got " + outcome);
                return;
            }
            List<ItemStack> after = stored(chest, id);
            if (after.size() != 1 || !ItemStack.matches(after.get(0), kept)) {
                CompatGameTestHelper.fail(helper, "Expected only the leftover " + describe(kept) + ", got "
                    + describeSorted(after));
                return;
            }
            chest.slotsFor(id).clearContent();
            outcome = useAndClose(helper, player, T0 + 10 * CD);
            if (outcome != CampChestOutcome.FILLED) {
                CompatGameTestHelper.fail(helper, "Expected FILLED after emptying the leftover, got " + outcome
                    + ". The leftover open must not use the cooldown");
                return;
            }
            helper.succeed();
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: calling use directly for a spectator fills nothing and leaves the cooldown, so the
     * player's first open in survival still fills. In game a spectator's click never reaches use
     * (vanilla asks getMenuProvider, which the block does not provide), so this covers the guard
     * for direct callers, not in-game spectator interaction.
     */
    private static void testSpectatorDoesNotFill(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            player.setGameMode(GameType.SPECTATOR);
            CampChestOutcome outcome = useAndClose(helper, player, T0);
            if (outcome == CampChestOutcome.FILLED || !chest.isEmptyFor(player.getUUID())) {
                CompatGameTestHelper.fail(helper, "A spectator's open filled the chest: " + outcome + ", "
                    + describeSorted(stored(chest, player.getUUID())));
                return;
            }
            player.setGameMode(GameType.SURVIVAL);
            outcome = useAndClose(helper, player, T0);
            if (outcome != CampChestOutcome.FILLED) {
                CompatGameTestHelper.fail(helper, "Expected FILLED on the first survival open, got " + outcome
                    + ". The spectator's open must not use the cooldown");
                return;
            }
            helper.succeed();
        } finally {
            player.setGameMode(GameType.SURVIVAL);
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: each player has their own slots and cooldown.
     */
    private static void testIsPerPlayer(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer a = joinPlayer(helper, PLAYER_POS);
        try {
            ServerPlayer b = joinPlayer(helper, SECOND_PLAYER_POS);
            try {
                CampChestOutcome outcomeA = useAndClose(helper, a, T0);
                CampChestOutcome outcomeB = useAndClose(helper, b, T0);
                if (outcomeA != CampChestOutcome.FILLED || outcomeB != CampChestOutcome.FILLED) {
                    CompatGameTestHelper.fail(helper, "Expected both players to fill, got " + outcomeA + " and "
                        + outcomeB);
                    return;
                }
                List<String> before = describeSorted(stored(chest, b.getUUID()));
                chest.slotsFor(a.getUUID()).clearContent();
                List<String> after = describeSorted(stored(chest, b.getUUID()));
                if (!before.equals(after)) {
                    CompatGameTestHelper.fail(helper, "Clearing A changed B's items from " + before + " to " + after);
                    return;
                }
                CampChestOutcome outcome = useAndClose(helper, b, T0 + 1);
                if (outcome != CampChestOutcome.OPENED) {
                    CompatGameTestHelper.fail(helper, "Expected B's open to be OPENED, got " + outcome);
                    return;
                }
                helper.succeed();
            } finally {
                CompatGameTestHelper.removeMockServerPlayer(helper, b);
            }
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, a);
        }
    }

    /**
     * Test: no chest slot takes a player's item, by placing, clicking, number-key swap or
     * shift-click, while shift-clicking a chest slot still moves its stack to the inventory.
     */
    private static void testIsTakeOnly(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            player.setGameMode(GameType.SURVIVAL);
            use(helper, player, T0);
            if (!(player.containerMenu instanceof WatchmakerChestMenu menu)) {
                CompatGameTestHelper.fail(helper, "Expected an open WatchmakerChestMenu, got " + player.containerMenu);
                return;
            }
            ItemStack dirt = new ItemStack(Items.DIRT, DIRT_COUNT);
            // Main inventory slot 9 is the first menu slot after the chest's 27.
            player.getInventory().setItem(9, dirt.copy());
            int dirtIndex = -1;
            for (int i = INVENTORY_START; i < MENU_END; i++) {
                if (menu.getSlot(i).getItem().is(Items.DIRT)) {
                    dirtIndex = i;
                    break;
                }
            }
            if (dirtIndex < 0) {
                CompatGameTestHelper.fail(helper, "The dirt stack is not in the menu's inventory slots");
                return;
            }
            if (menu.getSlot(0).mayPlace(dirt)) {
                CompatGameTestHelper.fail(helper, "A chest slot accepts a placed item");
                return;
            }
            ItemStack moved = menu.quickMoveStack(player, dirtIndex);
            if (!moved.isEmpty() || menu.getSlot(dirtIndex).getItem().getCount() != DIRT_COUNT) {
                CompatGameTestHelper.fail(helper, "Shift-clicking the dirt moved it: returned " + describe(moved)
                    + ", left " + describe(menu.getSlot(dirtIndex).getItem()));
                return;
            }
            int emptyIndex = -1;
            int filledIndex = -1;
            for (int i = 0; i < CHEST_SLOTS; i++) {
                if (menu.getSlot(i).getItem().isEmpty()) {
                    if (emptyIndex < 0) {
                        emptyIndex = i;
                    }
                } else if (filledIndex < 0) {
                    filledIndex = i;
                }
            }
            if (emptyIndex < 0 || filledIndex < 0) {
                CompatGameTestHelper.fail(helper, "Expected both an empty and a filled chest slot");
                return;
            }
            menu.setCarried(dirt.copy());
            CompatGameTestHelper.clickPickup(menu, emptyIndex, player);
            ItemStack carried = menu.getCarried();
            menu.setCarried(ItemStack.EMPTY);
            if (!menu.getSlot(emptyIndex).getItem().isEmpty()) {
                CompatGameTestHelper.fail(helper, "Clicking with dirt on the cursor put it into the chest");
                return;
            }
            if (!carried.is(Items.DIRT) || carried.getCount() != DIRT_COUNT) {
                CompatGameTestHelper.fail(helper, "The clicked dirt left the cursor: " + describe(carried));
                return;
            }
            // Number-key swap with hotbar slot 0.
            player.getInventory().setItem(0, dirt.copy());
            CompatGameTestHelper.clickSwap(menu, emptyIndex, 0, player);
            if (!menu.getSlot(emptyIndex).getItem().isEmpty() || !player.getInventory().getItem(0).is(Items.DIRT)) {
                CompatGameTestHelper.fail(helper, "A number-key swap put the dirt into the chest");
                return;
            }
            player.getInventory().setItem(0, ItemStack.EMPTY);
            ItemStack taken = menu.getSlot(filledIndex).getItem().copy();
            int before = player.getInventory().countItem(taken.getItem());
            menu.quickMoveStack(player, filledIndex);
            int after = player.getInventory().countItem(taken.getItem());
            if (!menu.getSlot(filledIndex).getItem().isEmpty() || after - before != taken.getCount()) {
                CompatGameTestHelper.fail(helper, "Shift-clicking " + describe(taken) + " out of the chest moved "
                    + (after - before) + " items and left " + describe(menu.getSlot(filledIndex).getItem()));
                return;
            }
            helper.succeed();
        } finally {
            player.getInventory().clearContent();
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: a save and reload keeps the player's slots and their cooldown.
     */
    private static void testPersistsSlotsAndCooldown(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            UUID id = player.getUUID();
            useAndClose(helper, player, T0);
            WatchmakerChestBlockEntity copy = placeChest(helper, COPY_POS);
            if (copy == null) {
                return;
            }
            CompatGameTestHelper.copyBlockEntityData(helper, chest, copy);
            List<String> original = describeSorted(stored(chest, id));
            List<String> copied = describeSorted(stored(copy, id));
            if (original.isEmpty() || !original.equals(copied)) {
                CompatGameTestHelper.fail(helper, "Expected the copy to hold " + original + ", got " + copied);
                return;
            }
            if (copy.tryActivate(id, T0 + 1, CD) <= 0) {
                CompatGameTestHelper.fail(helper, "The copy lost the player's cooldown");
                return;
            }
            helper.succeed();
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Finds the item entities around the chest that appeared since the given game time, so items
     * left in the reused GameTest world by an earlier run are not counted.
     */
    private static List<ItemEntity> findDrops(GameTestHelper helper, long since) {
        ServerLevel level = helper.getLevel();
        return level.getEntitiesOfClass(ItemEntity.class,
            new AABB(helper.absolutePos(CHEST_POS)).inflate(DROP_SEARCH_RANGE),
            e -> e.isAlive() && level.getGameTime() - e.getAge() >= since);
    }

    private static List<ItemStack> takeDrops(GameTestHelper helper, long since) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : findDrops(helper, since)) {
            stacks.add(entity.getItem().copy());
            entity.discard();
        }
        return stacks;
    }

    /**
     * Test: a player breaking the chest gets the plain chest and their own slots, and nobody
     * else's.
     */
    private static void testBreakingDropsOwnSlots(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer a = joinPlayer(helper, PLAYER_POS);
        try {
            ServerPlayer b = joinPlayer(helper, SECOND_PLAYER_POS);
            try {
                useAndClose(helper, a, T0);
                useAndClose(helper, b, T0);
                List<ItemStack> expected = stored(chest, a.getUUID());
                if (expected.isEmpty() || chest.isEmptyFor(b.getUUID())) {
                    CompatGameTestHelper.fail(helper, "Expected both players to hold items before the break");
                    return;
                }
                expected.add(new ItemStack(Items.CHEST));
                a.setGameMode(GameType.SURVIVAL);
                long since = helper.getLevel().getGameTime();
                a.gameMode.destroyBlock(helper.absolutePos(CHEST_POS));
                List<String> drops = describeSorted(takeDrops(helper, since));
                if (!drops.equals(describeSorted(expected))) {
                    CompatGameTestHelper.fail(helper, "Expected drops " + describeSorted(expected) + ", got " + drops);
                    return;
                }
                helper.succeed();
            } finally {
                CompatGameTestHelper.removeMockServerPlayer(helper, b);
            }
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, a);
        }
    }

    /**
     * Test: removal without a player drops the plain chest only.
     */
    private static void testRemovalWithoutPlayerDropsChestOnly(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            useAndClose(helper, player, T0);
            if (chest.isEmptyFor(player.getUUID())) {
                CompatGameTestHelper.fail(helper, "The first open stored no items");
                return;
            }
            long since = helper.getLevel().getGameTime();
            helper.getLevel().destroyBlock(helper.absolutePos(CHEST_POS), true);
            List<String> drops = describeSorted(takeDrops(helper, since));
            if (!drops.equals(List.of(describe(new ItemStack(Items.CHEST))))) {
                CompatGameTestHelper.fail(helper, "Expected a single chest, got " + drops);
                return;
            }
            helper.succeed();
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * Test: automation such as a hopper finds no container at the chest, even after a fill.
     */
    private static void testIsNotAContainer(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        try {
            useAndClose(helper, player, T0);
            Container container = HopperBlockEntity.getContainerAt(helper.getLevel(), helper.absolutePos(CHEST_POS));
            if (container != null) {
                CompatGameTestHelper.fail(helper, "A hopper sees a container at the chest: " + container);
                return;
            }
            helper.succeed();
        } finally {
            CompatGameTestHelper.removeMockServerPlayer(helper, player);
        }
    }

    /**
     * @return a failure message unless the sensor is active from a vibration of the given event
     */
    private static String sensorCheck(GameTestHelper helper, String action, int expectedFrequency) {
        BlockPos pos = helper.absolutePos(SENSOR_POS);
        BlockState state = helper.getLevel().getBlockState(pos);
        if (SculkSensorBlock.getPhase(state) != SculkSensorPhase.ACTIVE) {
            return "The sculk sensor did not react to the chest " + action + ": " + state;
        }
        if (!(helper.getLevel().getBlockEntity(pos) instanceof SculkSensorBlockEntity sensor)) {
            return "The sculk sensor has no block entity";
        }
        if (sensor.getLastVibrationFrequency() != expectedFrequency) {
            return "Expected the chest " + action + " frequency " + expectedFrequency + ", got "
                + sensor.getLastVibrationFrequency();
        }
        return null;
    }

    /**
     * Test: as with the vanilla chest, opening and closing the chest is a vibration a sculk
     * sensor detects.
     */
    private static void testOpenAndCloseTriggerSculkSensor(GameTestHelper helper) {
        WatchmakerChestBlockEntity chest = placeChest(helper, CHEST_POS);
        if (chest == null) {
            return;
        }
        helper.setBlock(SENSOR_POS, Blocks.SCULK_SENSOR);
        ServerPlayer player = joinPlayer(helper, PLAYER_POS);
        use(helper, player, T0);
        helper.runAfterDelay(VIBRATION_TICKS, () -> {
            String failure = sensorCheck(helper, "opening",
                VibrationSystem.getGameEventFrequency(GameEvent.CONTAINER_OPEN));
            if (failure != null) {
                CompatGameTestHelper.removeMockServerPlayer(helper, player);
                CompatGameTestHelper.fail(helper, failure);
                return;
            }
            helper.runAfterDelay(SENSOR_RESET_TICKS, () -> {
                player.closeContainer();
                helper.runAfterDelay(VIBRATION_TICKS, () -> {
                    CompatGameTestHelper.removeMockServerPlayer(helper, player);
                    String closeFailure = sensorCheck(helper, "closing",
                        VibrationSystem.getGameEventFrequency(GameEvent.CONTAINER_CLOSE));
                    if (closeFailure != null) {
                        CompatGameTestHelper.fail(helper, closeFailure);
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    private static StructurePlaceSettings campSettings(StructureTemplate template, Rotation rotation) {
        Vec3i size = template.getSize();
        return new StructurePlaceSettings()
            .setRotation(rotation)
            .setRotationPivot(new BlockPos(size.getX() / 2, 0, size.getZ() / 2));
    }

    /**
     * Test: without Lootr the camp's vanilla chest becomes a Watchmaker's Chest facing the same
     * way, in every rotation, and no loot spills while swapping. With Lootr the vanilla chest and
     * its loot table stay for Lootr to convert.
     */
    private static void testCampSwapsChestWithoutLootr(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Optional<StructureTemplate> loaded = CompatGameTestHelper.loadModStructureTemplate(helper, "watchmaker_camp");
        if (loaded.isEmpty()) {
            CompatGameTestHelper.fail(helper, "Structure template 'watchmaker_camp' could not be loaded");
            return;
        }
        StructureTemplate template = loaded.get();
        BlockPos origin = helper.absolutePos(CAMP_ORIGIN);
        long since = level.getGameTime();
        for (Rotation rotation : Rotation.values()) {
            StructurePlaceSettings settings = campSettings(template, rotation);
            template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);
            List<StructureTemplate.StructureBlockInfo> chests = template.filterBlocks(origin, settings, Blocks.CHEST);
            if (chests.size() != 1) {
                CompatGameTestHelper.fail(helper, rotation + ": expected one chest in the camp, got " + chests.size());
                return;
            }
            BlockPos chestPos = chests.get(0).pos();
            BlockState before = level.getBlockState(chestPos);
            if (!before.is(Blocks.CHEST)) {
                CompatGameTestHelper.fail(helper, rotation + ": expected a vanilla chest at " + chestPos + ", got "
                    + before);
                return;
            }
            Direction facing = before.getValue(ChestBlock.FACING);
            int swapped = WatchmakerCampChests.swapChests(level, template, origin, settings, false);
            BlockState after = level.getBlockState(chestPos);
            if (swapped != 1 || !after.is(ModBlocks.WATCHMAKER_CHEST.get())
                    || after.getValue(WatchmakerChestBlock.FACING) != facing) {
                CompatGameTestHelper.fail(helper, rotation + ": expected 1 swap to a watchmaker_chest facing " + facing
                    + ", got " + swapped + " and " + after);
                return;
            }
        }
        Vec3i size = template.getSize();
        AABB campArea = new AABB(origin.getX() - 1, origin.getY() - 1, origin.getZ() - 1,
            origin.getX() + size.getX() + 1, origin.getY() + size.getY() + 1, origin.getZ() + size.getZ() + 1);
        List<ItemEntity> spilled = level.getEntitiesOfClass(ItemEntity.class, campArea,
            e -> e.isAlive() && level.getGameTime() - e.getAge() >= since);
        if (!spilled.isEmpty()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemEntity entity : spilled) {
                stacks.add(entity.getItem().copy());
                entity.discard();
            }
            CompatGameTestHelper.fail(helper, "The swap spilled items: " + describeSorted(stacks));
            return;
        }
        for (Rotation rotation : Rotation.values()) {
            StructurePlaceSettings settings = campSettings(template, rotation);
            template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);
            BlockPos chestPos = template.filterBlocks(origin, settings, Blocks.CHEST).get(0).pos();
            int swapped = WatchmakerCampChests.swapChests(level, template, origin, settings, true);
            BlockState state = level.getBlockState(chestPos);
            boolean hasLootTable = CompatGameTestHelper.hasLootTable(level.getBlockEntity(chestPos));
            // Clear the loot table so placing the next rotation over this chest spills nothing.
            CompatGameTestHelper.clearLootTable(level.getBlockEntity(chestPos));
            if (swapped != 0 || !state.is(Blocks.CHEST) || !hasLootTable) {
                CompatGameTestHelper.fail(helper, rotation + ": with Lootr expected no swap and a vanilla chest with "
                    + "its loot table, got " + swapped + ", " + state + ", loot table " + hasLootTable);
                return;
            }
        }
        helper.succeed();
    }
}
