package com.chronodawn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;

/**
 * Base test class for ChronoDawn mod tests.
 *
 * This class provides common setup and utility methods for all tests in the mod.
 * Subclasses should extend this class to inherit common test infrastructure.
 *
 * Usage:
 * - Unit tests: For isolated component testing (calculations, logic, algorithms)
 * - Integration tests: For testing interactions between components
 * - GameTest: For in-world scenario testing (to be set up in T034)
 *
 * Reference: research.md (Decision 3: Testing Framework)
 */
public abstract class ChronoDawnTestBase {
    /**
     * Common setup for all tests.
     * This method is called before each test method.
     */
    @BeforeEach
    public void setUp() {
        // Common setup logic for all tests
        // Can be overridden by subclasses
    }

    /**
     * Helper method to log test information.
     *
     * @param message Message to log
     */
    protected void logTest(String message) {
        System.out.println("[TEST] " + message);
    }

    /**
     * Helper method to create a standardized test failure message.
     *
     * @param testName Name of the test
     * @param expected Expected value
     * @param actual Actual value
     * @return Formatted failure message
     */
    protected String failureMessage(String testName, Object expected, Object actual) {
        return String.format("%s failed: expected=%s, actual=%s", testName, expected, actual);
    }

    /**
     * Get the target Minecraft version from system property.
     *
     * @return Minecraft version (e.g., "1.20.1", "1.21.1")
     */
    protected String getMinecraftVersion() {
        return System.getProperty("chronodawn.minecraft.version", "unknown");
    }

    /**
     * Check if the current Minecraft version is 1.21.1 or higher.
     *
     * @return true if version is 1.21.1+, false otherwise
     */
    protected boolean isMinecraft121OrHigher() {
        String version = getMinecraftVersion();
        return version.startsWith("1.21") || version.compareTo("1.21") >= 0;
    }

    /**
     * Skip test if Minecraft version is below 1.21.1.
     * Use this for tests that require Minecraft 1.21.1+ registry system.
     *
     * @param reason Reason for skipping (will be logged)
     */
    protected void requireMinecraft121(String reason) {
        Assumptions.assumeTrue(
            isMinecraft121OrHigher(),
            "Test requires Minecraft 1.21.1+ (current: " + getMinecraftVersion() + "): " + reason
        );
    }
}
