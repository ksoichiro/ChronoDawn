package com.chronodawn.fabric.platform;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Fabric-specific implementation of ChronoDawnPlatform.
 *
 * This class provides Fabric Loader-specific implementations for platform abstraction methods.
 * It is automatically linked to com.chronodawn.platform.ChronoDawnPlatform via Architectury's
 * @ExpectPlatform annotation system.
 *
 * Do NOT call this class directly - use ChronoDawnPlatform instead.
 */
public class ChronoDawnPlatformImpl {
    /**
     * Get the configuration directory using Fabric Loader API.
     *
     * @return Path to the config directory
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    /**
     * Check if running in a development environment using Fabric Loader API.
     *
     * @return true if running in a development environment, false otherwise
     */
    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    /**
     * Get the platform name.
     *
     * @return "Fabric"
     */
    public static String getPlatformName() {
        return "Fabric";
    }
}
