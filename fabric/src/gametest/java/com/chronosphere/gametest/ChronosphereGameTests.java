package com.chronosphere.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTest tests for Chronosphere mod on Fabric.
 *
 * These tests run in an actual Minecraft server environment,
 * allowing for testing of entity spawning, block interactions,
 * and other gameplay mechanics that require the full game runtime.
 *
 * Test logic is shared via ChronosphereGameTestLogic in common module.
 *
 * Reference: T172 [P] Run all GameTests on Fabric loader
 */
public class ChronosphereGameTests implements FabricGameTest {

    // ============== Entity Spawn Tests ==============

    @GameTest(template = EMPTY_STRUCTURE)
    public void testTimeGuardianCanSpawn(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_CAN_SPAWN.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testTimeTyrantCanSpawn(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TIME_TYRANT_CAN_SPAWN.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testTemporalWraithCanSpawn(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TEMPORAL_WRAITH_CAN_SPAWN.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testClockworkSentinelCanSpawn(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_CLOCKWORK_SENTINEL_CAN_SPAWN.accept(helper);
    }

    // ============== Block Placement Tests ==============

    @GameTest(template = EMPTY_STRUCTURE)
    public void testTimeWoodLogCanBePlaced(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TIME_WOOD_LOG_CAN_BE_PLACED.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testTimeWoodPlanksCanBePlaced(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TIME_WOOD_PLANKS_CAN_BE_PLACED.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void testClockstoneBlockCanBePlaced(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_CLOCKSTONE_BLOCK_CAN_BE_PLACED.accept(helper);
    }

    // ============== Entity Health Tests ==============

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testTimeGuardianInitialHealth(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_INITIAL_HEALTH.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testTimeTyrantInitialHealth(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TIME_TYRANT_INITIAL_HEALTH.accept(helper);
    }

    // ============== Entity Attribute Tests (Migrated from @Disabled) ==============

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testTimeGuardianArmor(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_ARMOR.accept(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testTimeGuardianKnockbackResistance(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_KNOCKBACK_RESISTANCE.accept(helper);
    }

    // ============== Item Attribute Tests (Migrated from @Disabled) ==============

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void testChronobladeDurability(GameTestHelper helper) {
        ChronosphereGameTestLogic.TEST_CHRONOBLADE_DURABILITY.accept(helper);
    }
}
