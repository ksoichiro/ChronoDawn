package com.chronodawn.gametest;

import com.chronodawn.registry.ModBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * GameTest tests for ChronoDawn mod on Fabric (1.21.8 version).
 *
 * In 1.21.5+, Fabric API provides its own @GameTest annotation that replaces
 * the old FabricGameTest interface and @GameTestGenerator.
 *
 * Manual player input tests and Faded Plains block tests use @GameTest annotation directly.
 * Registry-driven tests are handled separately (test function registry + test instance JSON).
 */
public class ChronoDawnGameTests {

    private static final String STRUCTURE = "chronodawn:empty_test";

    private static final BlockPos BASE_POS = new BlockPos(1, 2, 1);
    private static final BlockPos TOP_POS = new BlockPos(1, 3, 1);

    // ============== Player Input Tests ==============

    @GameTest(structure = STRUCTURE, maxTicks = 100)
    public void testMockPlayerCanBeCreated(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_MOCK_PLAYER_CAN_BE_CREATED.accept(helper);
    }

    @GameTest(structure = STRUCTURE, maxTicks = 100)
    public void testPlayerCanEquipChestplate(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_CHESTPLATE.accept(helper);
    }

    @GameTest(structure = STRUCTURE, maxTicks = 100)
    public void testTimeTyrantMailCanBeEquipped(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_TIME_TYRANT_MAIL_CAN_BE_EQUIPPED.accept(helper);
    }

    @GameTest(structure = STRUCTURE, maxTicks = 100)
    public void testPlayerCanHoldChronoblade(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_PLAYER_CAN_HOLD_CHRONOBLADE.accept(helper);
    }

    @GameTest(structure = STRUCTURE, maxTicks = 100)
    public void testPlayerCanEquipFullArmorSet(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_FULL_ARMOR_SET.accept(helper);
    }

    @GameTest(structure = STRUCTURE, maxTicks = 100)
    public void testPlayerInventoryCanReceiveItems(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_PLAYER_INVENTORY_CAN_RECEIVE_ITEMS.accept(helper);
    }

    // ============== Faded Plains Block Tests ==============

    /**
     * Test: TEMPORAL_DEAD_BUSH canSurvive() returns true when the block below is PARCHED_TEMPORAL_DIRT.
     */
    @GameTest(structure = STRUCTURE, maxTicks = 100)
    public void testDeadBushSurvivesOnParchedDirt(GameTestHelper helper) {
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
    }

    /**
     * Test: TEMPORAL_DEAD_BUSH canSurvive() returns false when the block below is TEMPORAL_GRASS_BLOCK.
     */
    @GameTest(structure = STRUCTURE, maxTicks = 100)
    public void testDeadBushBreaksOnGrass(GameTestHelper helper) {
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
    }
}
