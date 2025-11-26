package com.chronosphere;

import com.chronosphere.core.dimension.ChronosphereBiomeProvider;
import com.chronosphere.core.dimension.ChronosphereDimension;
import com.chronosphere.events.ChronosphereEvents;
import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModBlockEntities;
import com.chronosphere.registry.ModCreativeTabs;
import com.chronosphere.registry.ModDimensions;
import com.chronosphere.registry.ModEffects;
import com.chronosphere.registry.ModEntities;
import com.chronosphere.registry.ModFluids;
import com.chronosphere.registry.ModItems;
import com.chronosphere.registry.ModStructureProcessorTypes;
import com.chronosphere.registry.ModTreeDecoratorTypes;
import com.chronosphere.worldgen.spawning.ChronosWardenSpawner;
import com.chronosphere.worldgen.spawning.ClockworkColossusSpawner;
import com.chronosphere.worldgen.spawning.PhantomCatacombsBossRoomPlacer;
import com.chronosphere.worldgen.spawning.TemporalPhantomSpawner;
import com.chronosphere.worldgen.spawning.TimeGuardianSpawner;
import com.chronosphere.worldgen.spawning.TimeTyrantSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Chronosphere {
    public static final String MOD_ID = "chronosphere";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Chronosphere Mod (common) initialized");

        // Initialize registries (Phase 2 - Foundational)
        ModFluids.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModEffects.register();
        ModEntities.register();
        ModDimensions.register();
        ModCreativeTabs.register();
        ModTreeDecoratorTypes.register();
        ModStructureProcessorTypes.register();

        // Initialize spawn eggs after entities are registered
        ModItems.initializeSpawnEggs();

        // Register event handlers (Phase 2 - Foundational)
        ChronosphereEvents.register();

        // Initialize dimension systems (Phase 3 - User Story 1)
        ChronosphereDimension.init();
        ChronosphereBiomeProvider.init();

        // Initialize spawn systems (Phase 4 - User Story 2)
        TimeGuardianSpawner.register();

        // Initialize spawn systems (Phase 5 - User Story 3)
        TimeTyrantSpawner.register();
        ChronosWardenSpawner.register();
        ClockworkColossusSpawner.register();
        PhantomCatacombsBossRoomPlacer.register();
        TemporalPhantomSpawner.register();
    }
}


