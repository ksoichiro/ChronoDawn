package com.chronodawn.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;

import java.util.Collection;

/**
 * GameTest tests for ChronoDawn mod on Fabric.
 *
 * Registry-driven tests are generated automatically via @GameTestGenerator.
 * Player input tests remain as individual @GameTest methods due to their
 * complex setup requirements.
 *
 * Reference: T172 [P] Run all GameTests on Fabric loader
 */
public class ChronoDawnGameTests implements FabricGameTest {

    /**
     * Generate all registry-driven tests: blocks, entities, tools, armor,
     * attributes, and boss fight mechanics.
     */
    @GameTestGenerator
    public Collection<TestFunction> generateRegistryTests() {
        return RegistryDrivenTestGenerator.generateAllTests().stream()
            .map(t -> new TestFunction(
                "chronodawn",           // batch name
                t.name(),               // test name
                EMPTY_STRUCTURE,        // structure template
                Rotation.NONE,          // rotation
                t.timeoutTicks(),       // timeout in ticks
                0L,                     // setup ticks
                true,                   // required
                t.test()                // test function
            ))
            .toList();
    }

    // ============== Player Input Tests ==============

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testMockPlayerCanBeCreated(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_MOCK_PLAYER_CAN_BE_CREATED.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testPlayerCanEquipChestplate(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_CHESTPLATE.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testTimeTyrantMailCanBeEquipped(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_TIME_TYRANT_MAIL_CAN_BE_EQUIPPED.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testPlayerCanHoldChronoblade(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_PLAYER_CAN_HOLD_CHRONOBLADE.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testPlayerCanEquipFullArmorSet(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_FULL_ARMOR_SET.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testPlayerInventoryCanReceiveItems(GameTestHelper helper) {
        ChronoDawnGameTestLogic.TEST_PLAYER_INVENTORY_CAN_RECEIVE_ITEMS.accept(helper);
    }
}
