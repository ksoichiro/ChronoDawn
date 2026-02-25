package com.chronodawn.neoforge;

import net.minecraft.server.MinecraftServer;

/**
 * Version-specific server helper for NeoForge 1.21.1.
 * Contains server-side code that differs between Minecraft versions.
 */
public class VersionSpecificServerHelper {

    /**
     * Process pending portal teleports.
     * In 1.21.1, portal teleports are processed synchronously, not deferred.
     * This method is a no-op for 1.21.1.
     *
     * @param server The Minecraft server instance
     */
    public static void processPendingTeleports(MinecraftServer server) {
        // In 1.21.1, portal teleports are processed synchronously in the block interaction
        // No deferred processing is needed
    }
}
