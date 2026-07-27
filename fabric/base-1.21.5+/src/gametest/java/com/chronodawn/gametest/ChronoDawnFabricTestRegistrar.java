package com.chronodawn.gametest;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registers the registry-driven GameTests on Fabric for Minecraft 1.21.5 and later.
 *
 * <p>1.21.5 rewrote GameTest to be registry-based and Fabric API dropped {@code FabricGameTest}
 * and {@code @GameTestGenerator} along with it. The replacement {@code @GameTest} annotation only
 * covers tests that exist as Java methods, so the ~2200 tests {@link RegistryDrivenTestGenerator}
 * produces at runtime have no annotation to hang off. NeoForge has {@code RegisterGameTestsEvent}
 * for this; Fabric exposes no equivalent, so this class does what Fabric API does internally:
 *
 * <ol>
 *   <li>register each test body in {@code BuiltInRegistries.TEST_FUNCTION} during mod init, and</li>
 *   <li>add a matching {@link FunctionGameTestInstance} to the {@code test_instance} dynamic
 *       registry while it is still being loaded.</li>
 * </ol>
 *
 * <p>Step 2 uses {@link DynamicRegistrySetupCallback}, which fires at the same point Fabric's own
 * {@code RegistryDataLoader} mixin injects its annotation-derived instances, so no mixin of our own
 * is needed.
 *
 * <p>The tests declared as {@code @GameTest} methods in {@code ChronoDawnGameTests} (player input
 * and Faded Plains) are deliberately NOT registered here — {@code generateAllTests()} excludes them
 * on 1.21.5+, and registering them again would fail on the duplicate registry id.
 */
public final class ChronoDawnFabricTestRegistrar implements ModInitializer {

    /** Structure every generated test runs in; matches the NeoForge registrar. */
    private static final String EMPTY_STRUCTURE_PATH = "empty_test";

    /** Environment id for the generated tests. Registered as an empty AllOf, i.e. no special setup. */
    private static final String ENVIRONMENT_PATH = "default";

    /**
     * Holds the sanitized id path rather than the resource key itself: 1.21.11 renamed both
     * ResourceLocation and the ResourceKey accessor that returns it, so the id is rebuilt through
     * {@link CompatResourceLocation} on each side instead of being carried across.
     */
    private record RegisteredTest(String idPath, int timeoutTicks) {}

    private static final List<RegisteredTest> REGISTERED_TESTS = new ArrayList<>();

    @Override
    public void onInitialize() {
        registerTestFunctions();
        DynamicRegistrySetupCallback.EVENT.register(ChronoDawnFabricTestRegistrar::registerTestInstances);
    }

    private static void registerTestFunctions() {
        for (var namedTest : RegistryDrivenTestGenerator.generateAllTests()) {
            var idPath = sanitizeTestName(namedTest.name());
            Consumer<GameTestHelper> testFunction = namedTest.test();
            Registry.register(
                BuiltInRegistries.TEST_FUNCTION,
                CompatResourceLocation.create(ChronoDawn.MOD_ID, idPath),
                testFunction);
            REGISTERED_TESTS.add(new RegisteredTest(idPath, namedTest.timeoutTicks()));
        }
        ChronoDawn.LOGGER.info(
            "ChronoDawn GameTests: registered {} test functions for Fabric", REGISTERED_TESTS.size());
    }

    private static void registerTestInstances(DynamicRegistryView view) {
        var testInstances = view.getOptional(Registries.TEST_INSTANCE).orElse(null);
        if (testInstances == null) {
            // The callback fires once per registry layer; only one layer holds test_instance.
            return;
        }
        var environments = view.getOptional(Registries.TEST_ENVIRONMENT).orElseThrow(
            () -> new IllegalStateException("test_environment registry missing while test_instance is loading"));

        var environmentId = CompatResourceLocation.create(ChronoDawn.MOD_ID, ENVIRONMENT_PATH);
        // Declared as Holder, not the Holder.Reference that registerForHolder returns, so that
        // TestData infers the type parameter FunctionGameTestInstance expects.
        Holder<TestEnvironmentDefinition> environment = Registry.registerForHolder(
            environments, environmentId, new TestEnvironmentDefinition.AllOf(List.of()));
        var structure = CompatResourceLocation.create(ChronoDawn.MOD_ID, EMPTY_STRUCTURE_PATH);

        for (var test : REGISTERED_TESTS) {
            var testData = new TestData<>(
                environment,
                structure,
                test.timeoutTicks(),
                0,     // setupTicks
                true   // required
            );
            var id = CompatResourceLocation.create(ChronoDawn.MOD_ID, test.idPath());
            ResourceKey<Consumer<GameTestHelper>> functionKey =
                ResourceKey.create(Registries.TEST_FUNCTION, id);
            Registry.register(testInstances, id, new FunctionGameTestInstance(functionKey, testData));
        }
        ChronoDawn.LOGGER.info(
            "ChronoDawn GameTests: registered {} test instances for Fabric", REGISTERED_TESTS.size());
    }

    /**
     * Registry ids allow only lowercase alphanumerics and a few separators, but generated test
     * names come from human-readable sources. Matches the NeoForge registrar's sanitizer so the
     * same test has the same id on both loaders.
     */
    private static String sanitizeTestName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }
}
