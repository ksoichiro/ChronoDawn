package com.chronodawn.fabric;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.ChronosWardenEntity;
import com.chronodawn.entities.bosses.ClockworkColossusEntity;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import com.chronodawn.entities.bosses.TemporalPhantomEntity;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.entities.bosses.TimeTyrantEntity;
import com.chronodawn.entities.mobs.ClockworkSentinelEntity;
import com.chronodawn.entities.mobs.FloqEntity;
import com.chronodawn.entities.mobs.TemporalWraithEntity;
import com.chronodawn.entities.mobs.TimeKeeperEntity;
import com.chronodawn.fabric.compat.CustomPortalFabric;
import com.chronodawn.fabric.event.BlockProtectionEventHandler;
import com.chronodawn.fabric.event.LavenderBookEventHandler;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.worldgen.processors.BossRoomProtectionProcessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChronoDawnFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ChronoDawn.init();

        // Register entity attributes (Fabric-specific)
        registerEntityAttributes();

        // Register spawn placements (Fabric-specific)
        registerSpawnPlacements();

        // Initialize spawn eggs (Fabric-specific timing)
        com.chronodawn.registry.ModItems.initializeSpawnEggs();
        ChronoDawn.LOGGER.info("Initialized spawn eggs for Fabric");

        // Initialize Custom Portal API integration
        CustomPortalFabric.init();

        // Register fuel items
        ChronoDawnFuelRegistry.register();

        // Register block protection event handler
        BlockProtectionEventHandler.register();

        // Register Lavender guidebook event handler (Fabric only)
        LavenderBookEventHandler.register();

        // Register server tick event for pending boss room protections
        // Check every 100 ticks (5 seconds) instead of every tick to reduce load
        final int[] tickCounter = {0};
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter[0]++;
            if (tickCounter[0] >= 100) {
                tickCounter[0] = 0;
                server.getAllLevels().forEach(level -> {
                    BossRoomProtectionProcessor.registerPendingProtections(level);
                });
            }
        });

        ChronoDawn.LOGGER.info("ChronoDawn Mod (Fabric) initialized");
    }

    /**
     * Register entity attributes for Fabric.
     * This is required for all custom living entities to have proper attributes.
     */
    private void registerEntityAttributes() {
        // Boss entities
        FabricDefaultAttributeRegistry.register(
            ModEntities.TIME_GUARDIAN.get(),
            TimeGuardianEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.CHRONOS_WARDEN.get(),
            ChronosWardenEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.CLOCKWORK_COLOSSUS.get(),
            ClockworkColossusEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.TIME_TYRANT.get(),
            TimeTyrantEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.TEMPORAL_PHANTOM.get(),
            TemporalPhantomEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.ENTROPY_KEEPER.get(),
            EntropyKeeperEntity.createAttributes()
        );

        // Custom mobs
        FabricDefaultAttributeRegistry.register(
            ModEntities.TEMPORAL_WRAITH.get(),
            TemporalWraithEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.CLOCKWORK_SENTINEL.get(),
            ClockworkSentinelEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.TIME_KEEPER.get(),
            TimeKeeperEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.FLOQ.get(),
            FloqEntity.createAttributes()
        );

        ChronoDawn.LOGGER.info("Registered entity attributes for Fabric");
    }

    /**
     * Register spawn placements for custom mobs.
     * This allows mobs to spawn in ChronoDawn even in daylight (always daytime dimension).
     */
    private void registerSpawnPlacements() {
        // Temporal Wraith - spawns on ground in daylight (Monster with any light)
        SpawnPlacements.register(
            ModEntities.TEMPORAL_WRAITH.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TemporalWraithEntity::checkTemporalWraithSpawnRules
        );

        // Clockwork Sentinel - spawns on ground in daylight (Monster with any light)
        SpawnPlacements.register(
            ModEntities.CLOCKWORK_SENTINEL.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ClockworkSentinelEntity::checkClockworkSentinelSpawnRules
        );

        // Time Keeper - spawns on ground in bright areas (Creature/Animal spawn rules)
        SpawnPlacements.register(
            ModEntities.TIME_KEEPER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TimeKeeperEntity::checkTimeKeeperSpawnRules
        );

        // Floq - spawns on ground like animals
        SpawnPlacements.register(
            ModEntities.FLOQ.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            FloqEntity::checkFloqSpawnRules
        );

        ChronoDawn.LOGGER.info("Registered spawn placements for custom mobs");
    }
}
