package com.chronosphere;

import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class for Chronosphere mod tests.
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
public abstract class ChronosphereTestBase {
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
}
