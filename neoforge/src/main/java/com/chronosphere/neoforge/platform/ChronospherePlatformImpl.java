package com.chronosphere.neoforge.platform;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * NeoForge-specific implementation of ChronospherePlatform.
 *
 * This class provides NeoForge Mod Loader-specific implementations for platform abstraction methods.
 * It is automatically linked to com.chronosphere.platform.ChronospherePlatform via Architectury's
 * @ExpectPlatform annotation system.
 *
 * Do NOT call this class directly - use ChronospherePlatform instead.
 */
public class ChronospherePlatformImpl {
    /**
     * Get the configuration directory using NeoForge FMLPaths.
     *
     * @return Path to the config directory
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    /**
     * Check if running in a development environment using NeoForge FMLLoader.
     *
     * @return true if running in a development environment, false otherwise
     */
    public static boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    /**
     * Get the platform name.
     *
     * @return "NeoForge"
     */
    public static String getPlatformName() {
        return "NeoForge";
    }
}
