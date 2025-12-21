package com.chronodawn.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

/**
 * Platform abstraction layer using Architectury's @ExpectPlatform pattern.
 *
 * This class defines platform-specific operations that differ between NeoForge and Fabric.
 * Implementations must be provided in the fabric/ and neoforge/ modules.
 *
 * Naming Convention:
 * - Common interface: com.chronodawn.platform.ChronoDawnPlatform
 * - Fabric impl: com.chronodawn.fabric.platform.ChronoDawnPlatformImpl
 * - NeoForge impl: com.chronodawn.neoforge.platform.ChronoDawnPlatformImpl
 *
 * Reference: research.md (Decision 4: Multi-Loader Architecture)
 */
public class ChronoDawnPlatform {
    /**
     * Get the configuration directory for the current mod loader.
     *
     * @return Path to the config directory
     */
    @ExpectPlatform
    public static Path getConfigDirectory() {
        // This method will be replaced at compile-time with platform-specific implementations
        throw new AssertionError("@ExpectPlatform method not replaced");
    }

    /**
     * Check if the current environment is a development environment.
     *
     * @return true if running in a development environment, false otherwise
     */
    @ExpectPlatform
    public static boolean isDevelopmentEnvironment() {
        throw new AssertionError("@ExpectPlatform method not replaced");
    }

    /**
     * Get the name of the current mod loader (for logging purposes).
     *
     * @return "Fabric" or "NeoForge"
     */
    @ExpectPlatform
    public static String getPlatformName() {
        throw new AssertionError("@ExpectPlatform method not replaced");
    }
}
