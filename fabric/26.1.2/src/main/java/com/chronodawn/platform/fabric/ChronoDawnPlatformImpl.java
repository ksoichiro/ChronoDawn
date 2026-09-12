package com.chronodawn.platform.fabric;

import java.nio.file.Path;

/**
 * Architectury @ExpectPlatform implementation at its default lookup location.
 */
public final class ChronoDawnPlatformImpl {
    private ChronoDawnPlatformImpl() {
    }

    public static Path getConfigDirectory() {
        return com.chronodawn.fabric.platform.ChronoDawnPlatformImpl.getConfigDirectory();
    }

    public static boolean isDevelopmentEnvironment() {
        return com.chronodawn.fabric.platform.ChronoDawnPlatformImpl.isDevelopmentEnvironment();
    }

    public static String getPlatformName() {
        return com.chronodawn.fabric.platform.ChronoDawnPlatformImpl.getPlatformName();
    }
}
