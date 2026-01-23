package com.chronodawn.neoforge.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.gametest.ChronoDawnGameTestLogic;
import com.chronodawn.gametest.RegistryDrivenTestGenerator;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * GameTest tests for ChronoDawn mod on NeoForge.
 *
 * Registry-driven tests are generated automatically via @GameTestGenerator.
 * Player input tests are included as additional generated tests.
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
     * Generate all test functions: registry-driven tests + player input tests.
     */
    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        List<TestFunction> tests = new ArrayList<>();

        // Registry-driven tests (blocks, entities, tools, armor, attributes, boss fights)
        for (var namedTest : RegistryDrivenTestGenerator.generateAllTests()) {
            tests.add(new TestFunction(
                BATCH,
                namedTest.name(),
                TEMPLATE,
                Rotation.NONE,
                namedTest.timeoutTicks(),
                0L,
                true,
                namedTest.test()
            ));
        }

        // Player input tests (kept as individual tests for complex setup)
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
            BATCH,
            name,
            TEMPLATE,
            Rotation.NONE,
            DEFAULT_TIMEOUT,
            0L,
            true,
            testFunc
        );
    }
}
