package com.chronosphere.neoforge.gametest;

import com.chronosphere.Chronosphere;
import com.chronosphere.gametest.ChronosphereGameTestLogic;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * GameTest tests for Chronosphere mod on NeoForge.
 *
 * These tests run in an actual Minecraft server environment,
 * allowing for testing of entity spawning, block interactions,
 * and other gameplay mechanics that require the full game runtime.
 *
 * Test logic is shared via ChronosphereGameTestLogic in common module.
 *
 * Reference: T173 [P] Run all GameTests on NeoForge loader
 */
@EventBusSubscriber(modid = Chronosphere.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ChronosphereGameTestsNeoForge {

    private static final String BATCH = "chronosphere";
    private static final String TEMPLATE = "chronosphere:ancient_ruins";
    private static final int DEFAULT_TIMEOUT = 100;

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(ChronosphereGameTestsNeoForge.class);
        Chronosphere.LOGGER.info("Registered Chronosphere GameTests for NeoForge");
    }

    /**
     * Generate test functions dynamically using shared test logic from common module.
     */
    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        List<TestFunction> tests = new ArrayList<>();

        // Entity spawn tests
        tests.add(createTest("testTimeGuardianCanSpawn",
            ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_CAN_SPAWN));

        tests.add(createTest("testTimeTyrantCanSpawn",
            ChronosphereGameTestLogic.TEST_TIME_TYRANT_CAN_SPAWN));

        tests.add(createTest("testTemporalWraithCanSpawn",
            ChronosphereGameTestLogic.TEST_TEMPORAL_WRAITH_CAN_SPAWN));

        tests.add(createTest("testClockworkSentinelCanSpawn",
            ChronosphereGameTestLogic.TEST_CLOCKWORK_SENTINEL_CAN_SPAWN));

        tests.add(createTest("testChronosphereBoatCanSpawn",
            ChronosphereGameTestLogic.TEST_CHRONOSPHERE_BOAT_CAN_SPAWN));

        tests.add(createTest("testChronosphereChestBoatCanSpawn",
            ChronosphereGameTestLogic.TEST_CHRONOSPHERE_CHEST_BOAT_CAN_SPAWN));

        tests.add(createTest("testChronosWardenCanSpawn",
            ChronosphereGameTestLogic.TEST_CHRONOS_WARDEN_CAN_SPAWN));

        tests.add(createTest("testClockworkColossusCanSpawn",
            ChronosphereGameTestLogic.TEST_CLOCKWORK_COLOSSUS_CAN_SPAWN));

        tests.add(createTest("testTimeKeeperCanSpawn",
            ChronosphereGameTestLogic.TEST_TIME_KEEPER_CAN_SPAWN));

        tests.add(createTest("testFloqCanSpawn",
            ChronosphereGameTestLogic.TEST_FLOQ_CAN_SPAWN));

        tests.add(createTest("testTemporalPhantomCanSpawn",
            ChronosphereGameTestLogic.TEST_TEMPORAL_PHANTOM_CAN_SPAWN));

        tests.add(createTest("testEntropyKeeperCanSpawn",
            ChronosphereGameTestLogic.TEST_ENTROPY_KEEPER_CAN_SPAWN));

        // Block placement tests
        tests.add(createTest("testTimeWoodLogCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_LOG_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodPlanksCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_PLANKS_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneBlockCanBePlaced",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_BLOCK_CAN_BE_PLACED));

        tests.add(createTest("testTimeCrystalBlockCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_CRYSTAL_BLOCK_CAN_BE_PLACED));

        tests.add(createTest("testTemporalBricksCanBePlaced",
            ChronosphereGameTestLogic.TEST_TEMPORAL_BRICKS_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneOreCanBePlaced",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_ORE_CAN_BE_PLACED));

        tests.add(createTest("testTimeCrystalOreCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_CRYSTAL_ORE_CAN_BE_PLACED));

        tests.add(createTest("testClockworkBlockCanBePlaced",
            ChronosphereGameTestLogic.TEST_CLOCKWORK_BLOCK_CAN_BE_PLACED));

        tests.add(createTest("testDarkTimeWoodLogCanBePlaced",
            ChronosphereGameTestLogic.TEST_DARK_TIME_WOOD_LOG_CAN_BE_PLACED));

        tests.add(createTest("testDarkTimeWoodPlanksCanBePlaced",
            ChronosphereGameTestLogic.TEST_DARK_TIME_WOOD_PLANKS_CAN_BE_PLACED));

        tests.add(createTest("testAncientTimeWoodLogCanBePlaced",
            ChronosphereGameTestLogic.TEST_ANCIENT_TIME_WOOD_LOG_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodStairsCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_STAIRS_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodSlabCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_SLAB_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodFenceCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_FENCE_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodDoorCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_DOOR_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodTrapdoorCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_TRAPDOOR_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneStairsCanBePlaced",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_STAIRS_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneSlabCanBePlaced",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_SLAB_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneWallCanBePlaced",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_WALL_CAN_BE_PLACED));

        tests.add(createTest("testTemporalBricksStairsCanBePlaced",
            ChronosphereGameTestLogic.TEST_TEMPORAL_BRICKS_STAIRS_CAN_BE_PLACED));

        tests.add(createTest("testTemporalBricksSlabCanBePlaced",
            ChronosphereGameTestLogic.TEST_TEMPORAL_BRICKS_SLAB_CAN_BE_PLACED));

        tests.add(createTest("testTemporalBricksWallCanBePlaced",
            ChronosphereGameTestLogic.TEST_TEMPORAL_BRICKS_WALL_CAN_BE_PLACED));

        tests.add(createTest("testReversingTimeSandstoneCanBePlaced",
            ChronosphereGameTestLogic.TEST_REVERSING_TIME_SANDSTONE_CAN_BE_PLACED));

        tests.add(createTest("testFrozenTimeIceCanBePlaced",
            ChronosphereGameTestLogic.TEST_FROZEN_TIME_ICE_CAN_BE_PLACED));

        tests.add(createTest("testTemporalMossCanBePlaced",
            ChronosphereGameTestLogic.TEST_TEMPORAL_MOSS_CAN_BE_PLACED));

        tests.add(createTest("testTimeWheatBaleCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WHEAT_BALE_CAN_BE_PLACED));

        tests.add(createTest("testChronoMelonCanBePlaced",
            ChronosphereGameTestLogic.TEST_CHRONO_MELON_CAN_BE_PLACED));

        tests.add(createTest("testAncientTimeWoodPlanksCanBePlaced",
            ChronosphereGameTestLogic.TEST_ANCIENT_TIME_WOOD_PLANKS_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodLeavesCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_LEAVES_CAN_BE_PLACED));

        // Entity health tests
        tests.add(createTest("testTimeGuardianInitialHealth",
            ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_INITIAL_HEALTH));

        tests.add(createTest("testTimeTyrantInitialHealth",
            ChronosphereGameTestLogic.TEST_TIME_TYRANT_INITIAL_HEALTH));

        // Entity attribute tests (migrated from @Disabled)
        tests.add(createTest("testTimeGuardianArmor",
            ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_ARMOR));

        tests.add(createTest("testTimeGuardianKnockbackResistance",
            ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_KNOCKBACK_RESISTANCE));

        // Item attribute tests (migrated from @Disabled)
        tests.add(createTest("testChronobladeDurability",
            ChronosphereGameTestLogic.TEST_CHRONOBLADE_DURABILITY));

        // Time Tyrant attribute tests
        tests.add(createTest("testTimeTyrantArmor",
            ChronosphereGameTestLogic.TEST_TIME_TYRANT_ARMOR));

        tests.add(createTest("testTimeTyrantKnockbackResistance",
            ChronosphereGameTestLogic.TEST_TIME_TYRANT_KNOCKBACK_RESISTANCE));

        tests.add(createTest("testTimeTyrantAttackDamage",
            ChronosphereGameTestLogic.TEST_TIME_TYRANT_ATTACK_DAMAGE));

        // Temporal Wraith attribute tests
        tests.add(createTest("testTemporalWraithInitialHealth",
            ChronosphereGameTestLogic.TEST_TEMPORAL_WRAITH_INITIAL_HEALTH));

        tests.add(createTest("testTemporalWraithAttackDamage",
            ChronosphereGameTestLogic.TEST_TEMPORAL_WRAITH_ATTACK_DAMAGE));

        // Clockwork Sentinel attribute tests
        tests.add(createTest("testClockworkSentinelInitialHealth",
            ChronosphereGameTestLogic.TEST_CLOCKWORK_SENTINEL_INITIAL_HEALTH));

        tests.add(createTest("testClockworkSentinelAttackDamage",
            ChronosphereGameTestLogic.TEST_CLOCKWORK_SENTINEL_ATTACK_DAMAGE));

        tests.add(createTest("testClockworkSentinelArmor",
            ChronosphereGameTestLogic.TEST_CLOCKWORK_SENTINEL_ARMOR));

        // Mini-boss attribute tests
        tests.add(createTest("testChronosWardenInitialHealth",
            ChronosphereGameTestLogic.TEST_CHRONOS_WARDEN_INITIAL_HEALTH));

        tests.add(createTest("testChronosWardenArmor",
            ChronosphereGameTestLogic.TEST_CHRONOS_WARDEN_ARMOR));

        tests.add(createTest("testClockworkColossusInitialHealth",
            ChronosphereGameTestLogic.TEST_CLOCKWORK_COLOSSUS_INITIAL_HEALTH));

        tests.add(createTest("testClockworkColossusKnockbackResistance",
            ChronosphereGameTestLogic.TEST_CLOCKWORK_COLOSSUS_KNOCKBACK_RESISTANCE));

        tests.add(createTest("testTemporalPhantomInitialHealth",
            ChronosphereGameTestLogic.TEST_TEMPORAL_PHANTOM_INITIAL_HEALTH));

        tests.add(createTest("testTemporalPhantomAttackDamage",
            ChronosphereGameTestLogic.TEST_TEMPORAL_PHANTOM_ATTACK_DAMAGE));

        tests.add(createTest("testEntropyKeeperInitialHealth",
            ChronosphereGameTestLogic.TEST_ENTROPY_KEEPER_INITIAL_HEALTH));

        tests.add(createTest("testEntropyKeeperArmor",
            ChronosphereGameTestLogic.TEST_ENTROPY_KEEPER_ARMOR));

        // Other mob attribute tests
        tests.add(createTest("testFloqInitialHealth",
            ChronosphereGameTestLogic.TEST_FLOQ_INITIAL_HEALTH));

        tests.add(createTest("testFloqAttackDamage",
            ChronosphereGameTestLogic.TEST_FLOQ_ATTACK_DAMAGE));

        tests.add(createTest("testTimeKeeperInitialHealth",
            ChronosphereGameTestLogic.TEST_TIME_KEEPER_INITIAL_HEALTH));

        // Tool durability tests
        tests.add(createTest("testClockstoneSwordDurability",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_SWORD_DURABILITY));

        tests.add(createTest("testEnhancedClockstoneSwordDurability",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_SWORD_DURABILITY));

        tests.add(createTest("testClockstonePickaxeDurability",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_PICKAXE_DURABILITY));

        tests.add(createTest("testEnhancedClockstonePickaxeDurability",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_PICKAXE_DURABILITY));

        tests.add(createTest("testSpatiallyLinkedPickaxeDurability",
            ChronosphereGameTestLogic.TEST_SPATIALLY_LINKED_PICKAXE_DURABILITY));

        // Additional boss attribute tests
        tests.add(createTest("testTimeGuardianAttackDamage",
            ChronosphereGameTestLogic.TEST_TIME_GUARDIAN_ATTACK_DAMAGE));

        // Armor defense tests
        tests.add(createTest("testClockstoneChestplateDefense",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_CHESTPLATE_DEFENSE));

        tests.add(createTest("testEnhancedClockstoneChestplateDefense",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_CHESTPLATE_DEFENSE));

        // Additional armor defense tests
        tests.add(createTest("testClockstoneHelmetDefense",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_HELMET_DEFENSE));

        tests.add(createTest("testClockstoneLeggingsDefense",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_LEGGINGS_DEFENSE));

        tests.add(createTest("testClockstoneBootsDefense",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_BOOTS_DEFENSE));

        tests.add(createTest("testEnhancedClockstoneHelmetDefense",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_HELMET_DEFENSE));

        tests.add(createTest("testEnhancedClockstoneLeggingsDefense",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_LEGGINGS_DEFENSE));

        tests.add(createTest("testEnhancedClockstoneBootsDefense",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_BOOTS_DEFENSE));

        // Additional tool durability tests
        tests.add(createTest("testClockstoneAxeDurability",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_AXE_DURABILITY));

        tests.add(createTest("testClockstoneShovelDurability",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_SHOVEL_DURABILITY));

        tests.add(createTest("testClockstoneHoeDurability",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_HOE_DURABILITY));

        tests.add(createTest("testEnhancedClockstoneAxeDurability",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_AXE_DURABILITY));

        tests.add(createTest("testEnhancedClockstoneShovelDurability",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_SHOVEL_DURABILITY));

        tests.add(createTest("testEnhancedClockstoneHoeDurability",
            ChronosphereGameTestLogic.TEST_ENHANCED_CLOCKSTONE_HOE_DURABILITY));

        Chronosphere.LOGGER.info("Generated {} GameTest functions for NeoForge", tests.size());
        return tests;
    }

    private static TestFunction createTest(String name, Consumer<GameTestHelper> testFunc) {
        return new TestFunction(
            BATCH,                      // batch name
            name,                       // test name
            TEMPLATE,                   // structure template
            Rotation.NONE,              // rotation
            DEFAULT_TIMEOUT,            // timeout in ticks
            0L,                         // setup ticks
            true,                       // required
            testFunc                    // test function
        );
    }
}
