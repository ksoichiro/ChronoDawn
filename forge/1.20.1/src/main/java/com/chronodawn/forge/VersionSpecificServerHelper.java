package com.chronodawn.forge;

import net.minecraft.server.MinecraftServer;

/**
 * Version-specific server helper for Forge 1.20.1.
 * Contains server-side code that differs between Minecraft versions.
 */
public class VersionSpecificServerHelper {

    /**
     * Process pending portal teleports.
     * In 1.20.1, portal teleports are processed synchronously in the block
     * interaction (see {@code ChronoDawnPortalBlock}), not deferred. This
     * method is a no-op for 1.20.1 (mirrors NeoForge 1.21.1's equivalent).
     *
     * @param server The Minecraft server instance
     */
    public static void processPendingTeleports(MinecraftServer server) {
        // In 1.20.1, portal teleports are processed synchronously in the block interaction
        // No deferred processing is needed
    }
}
