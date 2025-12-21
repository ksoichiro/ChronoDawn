package com.chronodawn.neoforge.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.gametest.ChronoDawnGameTestLogic;
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
 * GameTest tests for ChronoDawn mod on NeoForge.
 *
 * These tests run in an actual Minecraft server environment,
 * allowing for testing of entity spawning, block interactions,
 * and other gameplay mechanics that require the full game runtime.
 *
 * Test logic is shared via ChronoDawnGameTestLogic in common module.
 *
 * Reference: T173 [P] Run all GameTests on NeoForge loader
 */
@EventBusSubscriber(modid = ChronoDawn.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ChronoDawnGameTestsNeoForge {

    private static final String BATCH = "chronodawn";
    private static final String TEMPLATE = "chronodawn:ancient_ruins";
    private static final int DEFAULT_TIMEOUT = 100;

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        event.register(ChronoDawnGameTestsNeoForge.class);
        ChronoDawn.LOGGER.info("Registered ChronoDawn GameTests for NeoForge");
    }

    /**
     * Generate test functions dynamically using shared test logic from common module.
     */
    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        List<TestFunction> tests = new ArrayList<>();

        // Entity spawn tests
        tests.add(createTest("testTimeGuardianCanSpawn",
            ChronoDawnGameTestLogic.TEST_TIME_GUARDIAN_CAN_SPAWN));

        tests.add(createTest("testTimeTyrantCanSpawn",
            ChronoDawnGameTestLogic.TEST_TIME_TYRANT_CAN_SPAWN));

        tests.add(createTest("testTemporalWraithCanSpawn",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_WRAITH_CAN_SPAWN));

        tests.add(createTest("testClockworkSentinelCanSpawn",
            ChronoDawnGameTestLogic.TEST_CLOCKWORK_SENTINEL_CAN_SPAWN));

        tests.add(createTest("testChronoDawnBoatCanSpawn",
            ChronoDawnGameTestLogic.TEST_CHRONOSPHERE_BOAT_CAN_SPAWN));

        tests.add(createTest("testChronoDawnChestBoatCanSpawn",
            ChronoDawnGameTestLogic.TEST_CHRONOSPHERE_CHEST_BOAT_CAN_SPAWN));

        tests.add(createTest("testChronosWardenCanSpawn",
            ChronoDawnGameTestLogic.TEST_CHRONOS_WARDEN_CAN_SPAWN));

        tests.add(createTest("testClockworkColossusCanSpawn",
            ChronoDawnGameTestLogic.TEST_CLOCKWORK_COLOSSUS_CAN_SPAWN));

        tests.add(createTest("testTimeKeeperCanSpawn",
            ChronoDawnGameTestLogic.TEST_TIME_KEEPER_CAN_SPAWN));

        tests.add(createTest("testFloqCanSpawn",
            ChronoDawnGameTestLogic.TEST_FLOQ_CAN_SPAWN));

        tests.add(createTest("testTemporalPhantomCanSpawn",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_PHANTOM_CAN_SPAWN));

        tests.add(createTest("testEntropyKeeperCanSpawn",
            ChronoDawnGameTestLogic.TEST_ENTROPY_KEEPER_CAN_SPAWN));

        // Block placement tests
        tests.add(createTest("testTimeWoodLogCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WOOD_LOG_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodPlanksCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WOOD_PLANKS_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneBlockCanBePlaced",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_BLOCK_CAN_BE_PLACED));

        tests.add(createTest("testTimeCrystalBlockCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_CRYSTAL_BLOCK_CAN_BE_PLACED));

        tests.add(createTest("testTemporalBricksCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_BRICKS_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneOreCanBePlaced",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_ORE_CAN_BE_PLACED));

        tests.add(createTest("testTimeCrystalOreCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_CRYSTAL_ORE_CAN_BE_PLACED));

        tests.add(createTest("testClockworkBlockCanBePlaced",
            ChronoDawnGameTestLogic.TEST_CLOCKWORK_BLOCK_CAN_BE_PLACED));

        tests.add(createTest("testDarkTimeWoodLogCanBePlaced",
            ChronoDawnGameTestLogic.TEST_DARK_TIME_WOOD_LOG_CAN_BE_PLACED));

        tests.add(createTest("testDarkTimeWoodPlanksCanBePlaced",
            ChronoDawnGameTestLogic.TEST_DARK_TIME_WOOD_PLANKS_CAN_BE_PLACED));

        tests.add(createTest("testAncientTimeWoodLogCanBePlaced",
            ChronoDawnGameTestLogic.TEST_ANCIENT_TIME_WOOD_LOG_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodStairsCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WOOD_STAIRS_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodSlabCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WOOD_SLAB_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodFenceCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WOOD_FENCE_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodDoorCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WOOD_DOOR_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodTrapdoorCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WOOD_TRAPDOOR_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneStairsCanBePlaced",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_STAIRS_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneSlabCanBePlaced",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_SLAB_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneWallCanBePlaced",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_WALL_CAN_BE_PLACED));

        tests.add(createTest("testTemporalBricksStairsCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_BRICKS_STAIRS_CAN_BE_PLACED));

        tests.add(createTest("testTemporalBricksSlabCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_BRICKS_SLAB_CAN_BE_PLACED));

        tests.add(createTest("testTemporalBricksWallCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_BRICKS_WALL_CAN_BE_PLACED));

        tests.add(createTest("testReversingTimeSandstoneCanBePlaced",
            ChronoDawnGameTestLogic.TEST_REVERSING_TIME_SANDSTONE_CAN_BE_PLACED));

        tests.add(createTest("testFrozenTimeIceCanBePlaced",
            ChronoDawnGameTestLogic.TEST_FROZEN_TIME_ICE_CAN_BE_PLACED));

        tests.add(createTest("testTemporalMossCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_MOSS_CAN_BE_PLACED));

        tests.add(createTest("testTimeWheatBaleCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WHEAT_BALE_CAN_BE_PLACED));

        tests.add(createTest("testChronoMelonCanBePlaced",
            ChronoDawnGameTestLogic.TEST_CHRONO_MELON_CAN_BE_PLACED));

        tests.add(createTest("testAncientTimeWoodPlanksCanBePlaced",
            ChronoDawnGameTestLogic.TEST_ANCIENT_TIME_WOOD_PLANKS_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodLeavesCanBePlaced",
            ChronoDawnGameTestLogic.TEST_TIME_WOOD_LEAVES_CAN_BE_PLACED));

        // Entity health tests
        tests.add(createTest("testTimeGuardianInitialHealth",
            ChronoDawnGameTestLogic.TEST_TIME_GUARDIAN_INITIAL_HEALTH));

        tests.add(createTest("testTimeTyrantInitialHealth",
            ChronoDawnGameTestLogic.TEST_TIME_TYRANT_INITIAL_HEALTH));

        // Entity attribute tests (migrated from @Disabled)
        tests.add(createTest("testTimeGuardianArmor",
            ChronoDawnGameTestLogic.TEST_TIME_GUARDIAN_ARMOR));

        tests.add(createTest("testTimeGuardianKnockbackResistance",
            ChronoDawnGameTestLogic.TEST_TIME_GUARDIAN_KNOCKBACK_RESISTANCE));

        // Item attribute tests (migrated from @Disabled)
        tests.add(createTest("testChronobladeDurability",
            ChronoDawnGameTestLogic.TEST_CHRONOBLADE_DURABILITY));

        // Time Tyrant attribute tests
        tests.add(createTest("testTimeTyrantArmor",
            ChronoDawnGameTestLogic.TEST_TIME_TYRANT_ARMOR));

        tests.add(createTest("testTimeTyrantKnockbackResistance",
            ChronoDawnGameTestLogic.TEST_TIME_TYRANT_KNOCKBACK_RESISTANCE));

        tests.add(createTest("testTimeTyrantAttackDamage",
            ChronoDawnGameTestLogic.TEST_TIME_TYRANT_ATTACK_DAMAGE));

        // Temporal Wraith attribute tests
        tests.add(createTest("testTemporalWraithInitialHealth",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_WRAITH_INITIAL_HEALTH));

        tests.add(createTest("testTemporalWraithAttackDamage",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_WRAITH_ATTACK_DAMAGE));

        // Clockwork Sentinel attribute tests
        tests.add(createTest("testClockworkSentinelInitialHealth",
            ChronoDawnGameTestLogic.TEST_CLOCKWORK_SENTINEL_INITIAL_HEALTH));

        tests.add(createTest("testClockworkSentinelAttackDamage",
            ChronoDawnGameTestLogic.TEST_CLOCKWORK_SENTINEL_ATTACK_DAMAGE));

        tests.add(createTest("testClockworkSentinelArmor",
            ChronoDawnGameTestLogic.TEST_CLOCKWORK_SENTINEL_ARMOR));

        // Mini-boss attribute tests
        tests.add(createTest("testChronosWardenInitialHealth",
            ChronoDawnGameTestLogic.TEST_CHRONOS_WARDEN_INITIAL_HEALTH));

        tests.add(createTest("testChronosWardenArmor",
            ChronoDawnGameTestLogic.TEST_CHRONOS_WARDEN_ARMOR));

        tests.add(createTest("testClockworkColossusInitialHealth",
            ChronoDawnGameTestLogic.TEST_CLOCKWORK_COLOSSUS_INITIAL_HEALTH));

        tests.add(createTest("testClockworkColossusKnockbackResistance",
            ChronoDawnGameTestLogic.TEST_CLOCKWORK_COLOSSUS_KNOCKBACK_RESISTANCE));

        tests.add(createTest("testTemporalPhantomInitialHealth",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_PHANTOM_INITIAL_HEALTH));

        tests.add(createTest("testTemporalPhantomAttackDamage",
            ChronoDawnGameTestLogic.TEST_TEMPORAL_PHANTOM_ATTACK_DAMAGE));

        tests.add(createTest("testEntropyKeeperInitialHealth",
            ChronoDawnGameTestLogic.TEST_ENTROPY_KEEPER_INITIAL_HEALTH));

        tests.add(createTest("testEntropyKeeperArmor",
            ChronoDawnGameTestLogic.TEST_ENTROPY_KEEPER_ARMOR));

        // Other mob attribute tests
        tests.add(createTest("testFloqInitialHealth",
            ChronoDawnGameTestLogic.TEST_FLOQ_INITIAL_HEALTH));

        tests.add(createTest("testFloqAttackDamage",
            ChronoDawnGameTestLogic.TEST_FLOQ_ATTACK_DAMAGE));

        tests.add(createTest("testTimeKeeperInitialHealth",
            ChronoDawnGameTestLogic.TEST_TIME_KEEPER_INITIAL_HEALTH));

        // Tool durability tests
        tests.add(createTest("testClockstoneSwordDurability",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_SWORD_DURABILITY));

        tests.add(createTest("testEnhancedClockstoneSwordDurability",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_SWORD_DURABILITY));

        tests.add(createTest("testClockstonePickaxeDurability",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_PICKAXE_DURABILITY));

        tests.add(createTest("testEnhancedClockstonePickaxeDurability",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_PICKAXE_DURABILITY));

        tests.add(createTest("testSpatiallyLinkedPickaxeDurability",
            ChronoDawnGameTestLogic.TEST_SPATIALLY_LINKED_PICKAXE_DURABILITY));

        // Additional boss attribute tests
        tests.add(createTest("testTimeGuardianAttackDamage",
            ChronoDawnGameTestLogic.TEST_TIME_GUARDIAN_ATTACK_DAMAGE));

        // Armor defense tests
        tests.add(createTest("testClockstoneChestplateDefense",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_CHESTPLATE_DEFENSE));

        tests.add(createTest("testEnhancedClockstoneChestplateDefense",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_CHESTPLATE_DEFENSE));

        // Additional armor defense tests
        tests.add(createTest("testClockstoneHelmetDefense",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_HELMET_DEFENSE));

        tests.add(createTest("testClockstoneLeggingsDefense",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_LEGGINGS_DEFENSE));

        tests.add(createTest("testClockstoneBootsDefense",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_BOOTS_DEFENSE));

        tests.add(createTest("testEnhancedClockstoneHelmetDefense",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_HELMET_DEFENSE));

        tests.add(createTest("testEnhancedClockstoneLeggingsDefense",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_LEGGINGS_DEFENSE));

        tests.add(createTest("testEnhancedClockstoneBootsDefense",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_BOOTS_DEFENSE));

        // Additional tool durability tests
        tests.add(createTest("testClockstoneAxeDurability",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_AXE_DURABILITY));

        tests.add(createTest("testClockstoneShovelDurability",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_SHOVEL_DURABILITY));

        tests.add(createTest("testClockstoneHoeDurability",
            ChronoDawnGameTestLogic.TEST_CLOCKSTONE_HOE_DURABILITY));

        tests.add(createTest("testEnhancedClockstoneAxeDurability",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_AXE_DURABILITY));

        tests.add(createTest("testEnhancedClockstoneShovelDurability",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_SHOVEL_DURABILITY));

        tests.add(createTest("testEnhancedClockstoneHoeDurability",
            ChronoDawnGameTestLogic.TEST_ENHANCED_CLOCKSTONE_HOE_DURABILITY));

        // Player input tests
        tests.add(createTest("testMockPlayerCanBeCreated",
            ChronoDawnGameTestLogic.TEST_MOCK_PLAYER_CAN_BE_CREATED));

        tests.add(createTest("testPlayerCanEquipChestplate",
            ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_CHESTPLATE));

        tests.add(createTest("testTimeTyrantMailCanBeEquipped",
            ChronoDawnGameTestLogic.TEST_TIME_TYRANT_MAIL_CAN_BE_EQUIPPED));

        tests.add(createTest("testPlayerCanHoldChronoblade",
            ChronoDawnGameTestLogic.TEST_PLAYER_CAN_HOLD_CHRONOBLADE));

        tests.add(createTest("testPlayerCanEquipFullArmorSet",
            ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_FULL_ARMOR_SET));

        tests.add(createTest("testPlayerInventoryCanReceiveItems",
            ChronoDawnGameTestLogic.TEST_PLAYER_INVENTORY_CAN_RECEIVE_ITEMS));

        ChronoDawn.LOGGER.info("Generated {} GameTest functions for NeoForge", tests.size());
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
