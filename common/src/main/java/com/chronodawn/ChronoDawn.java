/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn;

import com.chronodawn.core.dimension.ChronoDawnBiomeProvider;
import com.chronodawn.core.dimension.ChronoDawnDimension;
import com.chronodawn.core.portal.PortalPersistenceManager;
import com.chronodawn.events.ChronoDawnEvents;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModBlockEntities;
import com.chronodawn.registry.ModCreativeTabs;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.registry.ModEffects;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModFluids;
import com.chronodawn.registry.ModItems;
import com.chronodawn.registry.ModParticles;
import com.chronodawn.registry.ModSounds;
import com.chronodawn.registry.ModStructureProcessorTypes;
import com.chronodawn.registry.ModTreeDecoratorTypes;
import com.chronodawn.worldgen.spawning.ChronosWardenSpawner;
import com.chronodawn.worldgen.spawning.ClockworkColossusSpawner;
import com.chronodawn.worldgen.spawning.EntropyKeeperSpawner;
import com.chronodawn.worldgen.spawning.MasterClockBossRoomPlacer;
import com.chronodawn.worldgen.spawning.PhantomCatacombsBossRoomPlacer;
import com.chronodawn.worldgen.spawning.TemporalPhantomSpawner;
import com.chronodawn.worldgen.spawning.TimeGuardianSpawner;
import com.chronodawn.worldgen.spawning.TimeTyrantSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChronoDawn {
    public static final String MOD_ID = "chronodawn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("ChronoDawn Mod (common) initialized");

        // Initialize registries (Phase 2 - Foundational)
        ModFluids.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModSounds.register();
        ModEffects.register();
        ModEntities.register();
        ModParticles.register();
        ModDimensions.register();
        ModCreativeTabs.register();
        ModTreeDecoratorTypes.register();
        ModStructureProcessorTypes.register();

        // Note: Spawn egg initialization is platform-specific
        // - Fabric: Called immediately in ChronoDawnFabric.onInitialize()
        // - NeoForge: Called in FMLCommonSetupEvent (ChronoDawnNeoForge.commonSetup())

        // Register event handlers (Phase 2 - Foundational)
        ChronoDawnEvents.register();

        // Initialize dimension systems (Phase 3 - User Story 1)
        ChronoDawnDimension.init();
        ChronoDawnBiomeProvider.init();

        // Initialize portal persistence
        PortalPersistenceManager.initialize();

        // Initialize spawn systems (Phase 4 - User Story 2)
        TimeGuardianSpawner.register();

        // Initialize spawn systems (Phase 5 - User Story 3)
        TimeTyrantSpawner.register();
        ChronosWardenSpawner.register();
        ClockworkColossusSpawner.register();
        MasterClockBossRoomPlacer.register();
        PhantomCatacombsBossRoomPlacer.register();
        TemporalPhantomSpawner.register();
        // EntropyKeeperSpawner disabled - Entropy Keeper now spawns via EntropyCryptTrapdoorBlock
        // EntropyKeeperSpawner.register();
    }
}


