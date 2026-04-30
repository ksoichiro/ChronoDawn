package com.chronodawn.gametest;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub class for 1.21.5+ gametest compatibility.
 *
 * Faded Plains tests for 1.21.5+ are registered directly as @GameTest methods
 * in the version-specific ChronoDawnGameTests class. The registry-driven
 * generator returns empty here to avoid duplicate registration.
 */
public final class FadedPlainsTests {

    private FadedPlainsTests() {
        // Utility class
    }

    public static <T> List<T> generateTests(MobBehaviorTests.TestFactory<T> factory) {
        return new ArrayList<>();
    }
}
