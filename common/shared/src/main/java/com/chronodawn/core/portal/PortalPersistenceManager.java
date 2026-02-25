package com.chronodawn.core.portal;

import com.chronodawn.ChronoDawn;
import com.chronodawn.data.PortalRegistryData;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.server.level.ServerLevel;

/**
 * Portal Persistence Manager - Manages loading and saving portal data.
 *
 * This class coordinates between PortalRegistry (in-memory) and PortalRegistryData (persistent storage).
 *
 * Lifecycle:
 * 1. Server Level Load: Load portal data from disk → PortalRegistry
 * 2. Portal Changes: PortalRegistry marks PortalRegistryData as dirty
 * 3. World Save: Minecraft automatically saves dirty PortalRegistryData
 *
 * Reference: Portal persistence implementation
 */
public class PortalPersistenceManager {
    /**
     * Initialize portal persistence system.
     * Registers event listeners for server lifecycle events.
     */
    public static void initialize() {
        // Load portal data when overworld loads
        LifecycleEvent.SERVER_LEVEL_LOAD.register(PortalPersistenceManager::onServerLevelLoad);

        ChronoDawn.LOGGER.debug("Portal Persistence Manager initialized");
    }

    /**
     * Load portal data when a server level loads.
     *
     * @param level Server level being loaded
     */
    private static void onServerLevelLoad(ServerLevel level) {
        // Only load on overworld (to avoid duplicate loading)
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return;
        }

        ChronoDawn.LOGGER.debug("Loading portal registry from world save data...");

        // Get or create portal registry data
        PortalRegistryData data = PortalRegistryData.get(level);

        // Link registry to saved data for automatic persistence
        PortalRegistry.getInstance().setSavedData(data);

        ChronoDawn.LOGGER.debug("Portal registry loaded successfully");
    }
}
