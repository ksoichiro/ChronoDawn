package com.chronodawn.forge.platform;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * Forge-specific implementation of ChronoDawnPlatform.
 *
 * This class provides Forge Mod Loader-specific implementations for platform abstraction methods.
 * It is automatically linked to com.chronodawn.platform.ChronoDawnPlatform via Architectury's
 * @ExpectPlatform annotation system.
 *
 * Do NOT call this class directly - use ChronoDawnPlatform instead.
 */
public class ChronoDawnPlatformImpl {
    /**
     * Get the configuration directory using Forge FMLPaths.
     *
     * @return Path to the config directory
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    /**
     * Check if running in a development environment using Forge FMLLoader.
     *
     * @return true if running in a development environment, false otherwise
     */
    public static boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    /**
     * Get the platform name.
     *
     * @return "Forge"
     */
    public static String getPlatformName() {
        return "Forge";
    }
}
