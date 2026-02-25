package com.chronodawn.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Stub class for 1.21.5 gametest compatibility.
 *
 * In 1.21.5, the GameTest system was completely overhauled to be registry-based.
 * This stub provides the record types and generator methods needed by RegistryDrivenTestData.
 *
 * TODO: Implement full test generation for 1.21.5 registry-based gametest system.
 */
public final class AdvancementTests {

    private AdvancementTests() {
        // Utility class
    }

    /**
     * Advancement specification: advancement ID + expected parent ID.
     */
    public record AdvancementSpec(
        String id,
        String parentId
    ) {
        public AdvancementSpec(String id) {
            this(id, null);
        }
    }

    /**
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateAdvancementLoadedTests(
            List<AdvancementSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        return new ArrayList<>();
    }

    /**
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateAdvancementGrantTests(
            List<AdvancementSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        return new ArrayList<>();
    }

    /**
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateAdvancementParentTests(
            List<AdvancementSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        return new ArrayList<>();
    }

    /**
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateAdvancementIsolationTests(
            List<AdvancementSpec> specs,
            MobBehaviorTests.TestFactory<T> factory) {
        return new ArrayList<>();
    }
}
