package com.chronodawn.neoforge;

import com.chronodawn.blocks.ChronoDawnPortalBlock;
import net.minecraft.server.MinecraftServer;

/**
 * Version-specific server helper for NeoForge 1.21.2.
 * Contains server-side code that differs between Minecraft versions.
 */
public class VersionSpecificServerHelper {

    /**
     * Process pending portal teleports.
     * In 1.21.2, portal teleports are deferred to avoid ConcurrentModificationException
     * during entity iteration. This method must be called after all entity ticks.
     *
     * @param server The Minecraft server instance
     */
    public static void processPendingTeleports(MinecraftServer server) {
        ChronoDawnPortalBlock.processPendingTeleports(server);
    }
}
