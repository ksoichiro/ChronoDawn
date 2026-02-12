package com.chronodawn.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Stub class for 1.21.5 gametest compatibility.
 *
 * In 1.21.5, the GameTest system was completely overhauled to be registry-based.
 * This stub provides the methods needed by RegistryDrivenTestGenerator.
 *
 * TODO: Implement full test generation for 1.21.5 registry-based gametest system.
 */
public final class PortalTests {

    private PortalTests() {
        // Utility class
    }

    /**
     * Stub: Returns empty list for 1.21.5.
     * TODO: Implement for 1.21.5 registry-based gametest system.
     */
    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        return new ArrayList<>();
    }
}
