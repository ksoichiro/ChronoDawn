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

        // Block placement tests
        tests.add(createTest("testTimeWoodLogCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_LOG_CAN_BE_PLACED));

        tests.add(createTest("testTimeWoodPlanksCanBePlaced",
            ChronosphereGameTestLogic.TEST_TIME_WOOD_PLANKS_CAN_BE_PLACED));

        tests.add(createTest("testClockstoneBlockCanBePlaced",
            ChronosphereGameTestLogic.TEST_CLOCKSTONE_BLOCK_CAN_BE_PLACED));

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
