package com.chronosphere;

import com.chronosphere.core.dimension.ChronosphereBiomeProvider;
import com.chronosphere.core.dimension.ChronosphereDimension;
import com.chronosphere.events.ChronosphereEvents;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModDimensions;
import com.chronosphere.registry.ModEntities;
import com.chronosphere.registry.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chronosphere {
    public static final String MOD_ID = "chronosphere";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Chronosphere Mod (common) initialized");

        // Initialize registries (Phase 2 - Foundational)
        ModBlocks.register();
        ModItems.register();
        ModEntities.register();
        ModDimensions.register();

        // Register event handlers (Phase 2 - Foundational)
        ChronosphereEvents.register();

        // Initialize dimension systems (Phase 3 - User Story 1)
        ChronosphereDimension.init();
        ChronosphereBiomeProvider.init();
    }
}


