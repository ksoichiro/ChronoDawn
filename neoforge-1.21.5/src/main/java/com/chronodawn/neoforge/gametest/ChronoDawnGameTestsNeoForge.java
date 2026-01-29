package com.chronodawn.neoforge.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.gametest.ChronoDawnGameTestLogic;
import com.chronodawn.gametest.RegistryDrivenTestGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ChronoDawn GameTests for NeoForge (1.21.5 version)
 *
 * In Minecraft 1.21.5, the GameTest system was completely rewritten to be registry-based.
 * Test functions are registered via DeferredRegister to BuiltInRegistries.TEST_FUNCTION.
 *
 * To run tests, test instances must also be defined via:
 * 1. JSON datapacks (data/<namespace>/test_instance/<name>.json), or
 * 2. RegisterGameTestsEvent (programmatic registration)
 *
 * Currently, only test functions are registered. Test instances require additional
 * JSON files or RegisterGameTestsEvent implementation for full functionality.
 *
 * Test functions can be invoked via the /test command once test instances are defined.
 *
 * Reference: https://docs.neoforged.net/docs/misc/gametest/
 */
public class ChronoDawnGameTestsNeoForge {

    /**
     * DeferredRegister for test functions.
     * These are registered to BuiltInRegistries.TEST_FUNCTION.
     */
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
        DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, ChronoDawn.MOD_ID);

    // Store all registered test holders for reference
    private static final List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> REGISTERED_TESTS = new ArrayList<>();

    // Static block to register all test functions
    static {
        // Registry-driven tests from RegistryDrivenTestGenerator
        for (var namedTest : RegistryDrivenTestGenerator.generateAllTests()) {
            String sanitizedName = sanitizeTestName(namedTest.name());
            var holder = TEST_FUNCTIONS.register(sanitizedName, () -> namedTest.test());
            REGISTERED_TESTS.add(holder);
        }

        // Player input tests
        registerTest("test_mock_player_can_be_created", ChronoDawnGameTestLogic.TEST_MOCK_PLAYER_CAN_BE_CREATED);
        registerTest("test_player_can_equip_chestplate", ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_CHESTPLATE);
        registerTest("test_time_tyrant_mail_can_be_equipped", ChronoDawnGameTestLogic.TEST_TIME_TYRANT_MAIL_CAN_BE_EQUIPPED);
        registerTest("test_player_can_hold_chronoblade", ChronoDawnGameTestLogic.TEST_PLAYER_CAN_HOLD_CHRONOBLADE);
        registerTest("test_player_can_equip_full_armor_set", ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_FULL_ARMOR_SET);
        registerTest("test_player_inventory_can_receive_items", ChronoDawnGameTestLogic.TEST_PLAYER_INVENTORY_CAN_RECEIVE_ITEMS);

        ChronoDawn.LOGGER.debug("Prepared {} GameTest functions for NeoForge 1.21.5", REGISTERED_TESTS.size());
    }

    private static void registerTest(String name, Consumer<GameTestHelper> test) {
        var holder = TEST_FUNCTIONS.register(name, () -> test);
        REGISTERED_TESTS.add(holder);
    }

    /**
     * Sanitize test name for registry (lowercase, no special characters except underscore).
     */
    private static String sanitizeTestName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    /**
     * Register the DeferredRegister to the mod event bus.
     * This should be called from the main mod class constructor.
     *
     * @param modEventBus The mod event bus
     */
    public static void register(IEventBus modEventBus) {
        TEST_FUNCTIONS.register(modEventBus);
        ChronoDawn.LOGGER.info("ChronoDawn GameTests: {} test functions registered for NeoForge 1.21.5", REGISTERED_TESTS.size());
        ChronoDawn.LOGGER.info("Note: Test instances (JSON or RegisterGameTestsEvent) needed to run tests");
    }

    /**
     * Get the number of registered test functions.
     */
    public static int getTestCount() {
        return REGISTERED_TESTS.size();
    }
}
