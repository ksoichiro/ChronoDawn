package com.chronodawn.neoforge.platform;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * NeoForge-specific implementation of ChronoDawnPlatform.
 *
 * 1.21.9: FMLLoader.isProduction() is no longer static, use instance method.
 *
 * Do NOT call this class directly - use ChronoDawnPlatform instead.
 */
public class ChronoDawnPlatformImpl {
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
        // 1.21.9: FMLEnvironment.production field removed, use isProduction() method instead
        return !FMLEnvironment.isProduction();
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
