package com.chronodawn.platform.neoforge;

import java.nio.file.Path;

/**
 * Architectury @ExpectPlatform implementation at its default lookup location.
 */
public final class ChronoDawnPlatformImpl {
    private ChronoDawnPlatformImpl() {
    }

    public static Path getConfigDirectory() {
        return com.chronodawn.neoforge.platform.ChronoDawnPlatformImpl.getConfigDirectory();
    }

    public static boolean isDevelopmentEnvironment() {
        return com.chronodawn.neoforge.platform.ChronoDawnPlatformImpl.isDevelopmentEnvironment();
    }

    public static String getPlatformName() {
        return com.chronodawn.neoforge.platform.ChronoDawnPlatformImpl.getPlatformName();
    }
}
