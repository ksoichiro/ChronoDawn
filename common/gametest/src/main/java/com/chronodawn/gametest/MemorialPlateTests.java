package com.chronodawn.gametest;

import com.chronodawn.blocks.MemorialPlateBlock;
import com.chronodawn.blocks.MemorialPlateBlockEntity;
import com.chronodawn.compat.CompatGameTestHelper;
import com.chronodawn.core.wayside.StepOnTracker;
import com.chronodawn.items.JournalPageItem;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MemorialPlateTests {

    private static final BlockPos PLATE_POS = new BlockPos(1, 2, 1);
    private static final long COOLDOWN = 24000;
    // Rolls per check. The supplies pool is random, so one roll could miss a bad entry.
    private static final int ROLLS = 30;

    private MemorialPlateTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        List<T> tests = new ArrayList<>();
        tests.add(factory.create("memorial_plate_refuses_same_player_within_cooldown",
            MemorialPlateTests::testCooldownPerPlayer));
        tests.add(factory.create("memorial_plate_rewards_once_per_visit",
            MemorialPlateTests::testRewardsOncePerVisit));
        tests.add(factory.create("memorial_offering_has_one_page_and_supplies",
            MemorialPlateTests::testOffering));
        tests.add(factory.create("memorial_offering_from_missing_table_is_empty",
            MemorialPlateTests::testMissingTable));
        tests.add(factory.create("memorial_plate_drops_plain_pressure_plate",
            MemorialPlateTests::testDrops));
        return tests;
    }

    /**
     * Test: the placed plate carries a block entity that refuses a second prayer by
     * the same player within the cooldown and accepts another player.
     */
    private static void testCooldownPerPlayer(GameTestHelper helper) {
        helper.setBlock(PLATE_POS, ModBlocks.MEMORIAL_PLATE.get());
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(PLATE_POS)) instanceof MemorialPlateBlockEntity plate)) {
            CompatGameTestHelper.fail(helper, "memorial_plate has no MemorialPlateBlockEntity");
            return;
        }
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        long now = helper.getLevel().getGameTime();
        if (plate.tryActivate(first, now, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "First prayer was refused");
            return;
        }
        if (plate.tryActivate(first, now + 100, COOLDOWN) <= 0) {
            CompatGameTestHelper.fail(helper, "Second prayer by the same player was accepted within the cooldown");
            return;
        }
        if (plate.tryActivate(second, now + 100, COOLDOWN) != 0) {
            CompatGameTestHelper.fail(helper, "Prayer by a different player was refused");
            return;
        }
        helper.succeed();
    }

    /**
     * Test: stepping onto the plate drops an offering once. Standing on it, and stepping off
     * and back on within the cooldown, drop nothing more.
     */
    private static void testRewardsOncePerVisit(GameTestHelper helper) {
        helper.setBlock(PLATE_POS, ModBlocks.MEMORIAL_PLATE.get());
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(PLATE_POS);
        Player player = CompatGameTestHelper.makeMockPlayer(helper);
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

        MemorialPlateBlock.onPlayerInside(level, pos, player);
        int afterFirstStep = countDroppedItems(level, pos);
        if (afterFirstStep < 2) {
            CompatGameTestHelper.fail(helper, "First step should drop a page and supplies, found " + afterFirstStep);
            return;
        }
        MemorialPlateBlock.onPlayerInside(level, pos, player);
        if (countDroppedItems(level, pos) > afterFirstStep) {
            CompatGameTestHelper.fail(helper, "Standing on the plate prayed again");
            return;
        }
        // Wait past the step-off window so the next call is a new visit, which the cooldown must refuse.
        helper.runAfterDelay(StepOnTracker.STEP_OFF_TICKS + 2, () -> {
            MemorialPlateBlock.onPlayerInside(level, pos, player);
            // The mock player may pick items up in the meantime, so only an increase is a failure.
            if (countDroppedItems(level, pos) > afterFirstStep) {
                CompatGameTestHelper.fail(helper, "Stepping back on within the cooldown prayed again");
                return;
            }
            helper.succeed();
        });
    }

    private static int countDroppedItems(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).size();
    }

    /**
     * Test: every offering holds exactly one journal page and one or two supply stacks.
     */
    private static void testOffering(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(PLATE_POS);
        for (int roll = 0; roll < ROLLS; roll++) {
            List<ItemStack> offering = MemorialPlateBlock.rollOffering(level, pos);
            int pages = 0;
            int supplies = 0;
            for (ItemStack stack : offering) {
                if (stack.getItem() instanceof JournalPageItem) {
                    pages++;
                } else if (stack.is(Items.BREAD) || stack.is(Items.ARROW) || stack.is(Items.TORCH)) {
                    supplies++;
                } else {
                    CompatGameTestHelper.fail(helper, "Unexpected offering item: " + stack);
                    return;
                }
            }
            if (pages != 1 || supplies < 1 || supplies > 2) {
                CompatGameTestHelper.fail(helper, "Expected 1 page and 1-2 supplies, got " + offering);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * Test: a loot table removed by a datapack yields nothing instead of throwing.
     */
    private static void testMissingTable(GameTestHelper helper) {
        List<ItemStack> offering = MemorialPlateBlock.rollOffering(
            helper.getLevel(), helper.absolutePos(PLATE_POS), "gameplay/does_not_exist");
        if (!offering.isEmpty()) {
            CompatGameTestHelper.fail(helper, "Missing loot table produced " + offering);
            return;
        }
        helper.succeed();
    }

    /**
     * Test: breaking the plate gives the plain pressure plate, so the activator cannot be farmed.
     */
    private static void testDrops(GameTestHelper helper) {
        helper.setBlock(PLATE_POS, ModBlocks.MEMORIAL_PLATE.get());
        BlockPos pos = helper.absolutePos(PLATE_POS);
        BlockState state = helper.getLevel().getBlockState(pos);
        LootParams.Builder builder = new LootParams.Builder(helper.getLevel())
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
            .withParameter(LootContextParams.TOOL, new ItemStack(Items.WOODEN_PICKAXE));
        List<ItemStack> drops = state.getDrops(builder);
        if (drops.size() == 1 && drops.get(0).is(ModBlocks.TEMPORAL_STONE_PRESSURE_PLATE.get().asItem())) {
            helper.succeed();
        } else {
            CompatGameTestHelper.fail(helper, "memorial_plate should drop one temporal_stone_pressure_plate, got " + drops);
        }
    }
}
