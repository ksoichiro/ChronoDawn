package com.chronodawn.events;

import com.chronodawn.ChronoDawn;

/**
 * Base event handler structure for ChronoDawn mod.
 *
 * This class coordinates the registration of all event handlers in the mod.
 * Each specific event handler (Entity, Block, Player) is registered through this central point.
 *
 * Using Architectury Event API ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ChronoDawnEvents {
    /**
     * Register all event handlers.
     * This method must be called during mod initialization.
     */
    public static void register() {
        EntityEventHandler.register();
        BlockEventHandler.register();
        PlayerEventHandler.register();
        TimeDistortionEventHandler.register();

        ChronoDawn.LOGGER.info("Registered ChronoDawnEvents");
    }
}
