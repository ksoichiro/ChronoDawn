package com.chronosphere;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chronosphere {
    public static final String MOD_ID = "chronosphere";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Chronosphere Mod (common) initialized");

        // TODO: Initialize registries in Phase 2
        // ModBlocks.register();
        // ModItems.register();
        // ModEntities.register();

        // TODO: Register event handlers in Phase 2
        // ChronosphereEvents.register();
    }
}
