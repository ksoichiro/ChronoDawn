package com.chronosphere.events;

import com.chronosphere.Chronosphere;

/**
 * Base event handler structure for Chronosphere mod.
 *
 * This class coordinates the registration of all event handlers in the mod.
 * Each specific event handler (Entity, Block, Player) is registered through this central point.
 *
 * Using Architectury Event API ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ChronosphereEvents {
    /**
     * Register all event handlers.
     * This method must be called during mod initialization.
     */
    public static void register() {
        EntityEventHandler.register();
        BlockEventHandler.register();
        PlayerEventHandler.register();

        Chronosphere.LOGGER.info("Registered ChronosphereEvents");
    }
}
