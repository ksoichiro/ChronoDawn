package com.chronodawn.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTest tests for ChronoDawn mod on Fabric (1.21.5 version).
 *
 * In 1.21.5, Fabric API provides its own @GameTest annotation that replaces
 * the old FabricGameTest interface and @GameTestGenerator.
 *
 * Manual player input tests use @GameTest annotation directly.
 * Registry-driven tests are handled separately (test function registry + test instance JSON).
 */
public class ChronoDawnGameTests {

    private static final String STRUCTURE = "chronodawn:empty_test";

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
}
