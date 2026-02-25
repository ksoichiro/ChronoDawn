package com.chronodawn.neoforge.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.gametest.ChronoDawnGameTestLogic;
import com.chronodawn.gametest.RegistryDrivenTestGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ChronoDawn GameTests for NeoForge (1.21.5 version)
 *
 * In Minecraft 1.21.5, the GameTest system was completely rewritten to be registry-based.
 * Test functions are registered via DeferredRegister to BuiltInRegistries.TEST_FUNCTION,
 * and test instances are registered via RegisterGameTestsEvent.
 *
 * Reference: https://docs.neoforged.net/docs/misc/gametest/
 */
public class ChronoDawnGameTestsNeoForge {

    private static final ResourceLocation EMPTY_STRUCTURE =
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "empty_test");

    /**
     * DeferredRegister for test functions.
     * These are registered to BuiltInRegistries.TEST_FUNCTION.
     */
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
        DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, ChronoDawn.MOD_ID);

    // Store all registered test holders with their timeout
    private static final List<RegisteredTest> REGISTERED_TESTS = new ArrayList<>();

    // Guard against duplicate event invocations (event fires during data loading and server startup)
    private static boolean testInstancesRegistered = false;

    private record RegisteredTest(
        DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> holder,
        int timeoutTicks
    ) {}

    // Static block to register all test functions
    static {
        // Registry-driven tests from RegistryDrivenTestGenerator
        for (var namedTest : RegistryDrivenTestGenerator.generateAllTests()) {
            String sanitizedName = sanitizeTestName(namedTest.name());
            var holder = TEST_FUNCTIONS.register(sanitizedName, () -> namedTest.test());
            REGISTERED_TESTS.add(new RegisteredTest(holder, namedTest.timeoutTicks()));
        }

        // Player input tests
        registerTest("test_mock_player_can_be_created", ChronoDawnGameTestLogic.TEST_MOCK_PLAYER_CAN_BE_CREATED, 100);
        registerTest("test_player_can_equip_chestplate", ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_CHESTPLATE, 100);
        registerTest("test_time_tyrant_mail_can_be_equipped", ChronoDawnGameTestLogic.TEST_TIME_TYRANT_MAIL_CAN_BE_EQUIPPED, 100);
        registerTest("test_player_can_hold_chronoblade", ChronoDawnGameTestLogic.TEST_PLAYER_CAN_HOLD_CHRONOBLADE, 100);
        registerTest("test_player_can_equip_full_armor_set", ChronoDawnGameTestLogic.TEST_PLAYER_CAN_EQUIP_FULL_ARMOR_SET, 100);
        registerTest("test_player_inventory_can_receive_items", ChronoDawnGameTestLogic.TEST_PLAYER_INVENTORY_CAN_RECEIVE_ITEMS, 100);

        ChronoDawn.LOGGER.debug("Prepared {} GameTest functions for NeoForge 1.21.5", REGISTERED_TESTS.size());
    }

    private static void registerTest(String name, Consumer<GameTestHelper> test, int timeoutTicks) {
        var holder = TEST_FUNCTIONS.register(name, () -> test);
        REGISTERED_TESTS.add(new RegisteredTest(holder, timeoutTicks));
    }

    /**
     * Sanitize test name for registry (lowercase, no special characters except underscore).
     */
    private static String sanitizeTestName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    /**
     * Register the DeferredRegister and event listener to the mod event bus.
     * This should be called from the main mod class constructor.
     *
     * @param modEventBus The mod event bus
     */
    public static void register(IEventBus modEventBus) {
        TEST_FUNCTIONS.register(modEventBus);
        modEventBus.addListener(ChronoDawnGameTestsNeoForge::onRegisterGameTests);
        ChronoDawn.LOGGER.info("ChronoDawn GameTests: {} test functions prepared for NeoForge 1.21.5", REGISTERED_TESTS.size());
    }

    /**
     * Register test instances via RegisterGameTestsEvent.
     * Each test function needs a corresponding test instance to be runnable.
     */
    @SubscribeEvent
    public static void onRegisterGameTests(RegisterGameTestsEvent event) {
        // Guard: RegisterGameTestsEvent may fire multiple times (data loading + server startup).
        // Only register on the first invocation when registries are still writable.
        if (testInstancesRegistered) {
            return;
        }
        testInstancesRegistered = true;

        // Register a default environment for ChronoDawn tests (empty AllOf = no special setup)
        Holder<TestEnvironmentDefinition> defaultEnv =
            event.registerEnvironment(
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "default")
            );

        int registered = 0;
        for (var test : REGISTERED_TESTS) {
            var holder = test.holder();
            if (!holder.isBound()) {
                ChronoDawn.LOGGER.warn("Test function not bound: {}", holder.getId());
                continue;
            }
            var testData = new TestData<>(
                defaultEnv,
                EMPTY_STRUCTURE,
                test.timeoutTicks(),
                0,     // setupTicks
                true   // required
            );
            var instance = new FunctionGameTestInstance(holder.getKey(), testData);
            event.registerTest(holder.getId(), instance);
            registered++;
        }
        ChronoDawn.LOGGER.info("ChronoDawn GameTests: registered {} test instances for NeoForge 1.21.5", registered);
    }

    /**
     * Get the number of registered test functions.
     */
    public static int getTestCount() {
        return REGISTERED_TESTS.size();
    }
}
