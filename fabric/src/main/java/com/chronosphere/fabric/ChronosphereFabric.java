package com.chronosphere.fabric;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.ChronosWardenEntity;
import com.chronosphere.entities.bosses.ClockworkColossusEntity;
import com.chronosphere.entities.bosses.EntropyKeeperEntity;
import com.chronosphere.entities.bosses.TemporalPhantomEntity;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.entities.bosses.TimeTyrantEntity;
import com.chronosphere.entities.mobs.ClockworkSentinelEntity;
import com.chronosphere.entities.mobs.FloqEntity;
import com.chronosphere.entities.mobs.TemporalWraithEntity;
import com.chronosphere.entities.mobs.TimeKeeperEntity;
import com.chronosphere.fabric.compat.CustomPortalFabric;
import com.chronosphere.fabric.event.BlockProtectionEventHandler;
import com.chronosphere.registry.ModEntities;
import com.chronosphere.worldgen.processors.BossRoomProtectionProcessor;
import com.chronosphere.worldgen.processors.MasterClockProtectionProcessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChronosphereFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Chronosphere.init();

        // Register entity attributes (Fabric-specific)
        registerEntityAttributes();

        // Register spawn placements (Fabric-specific)
        registerSpawnPlacements();

        // Initialize spawn eggs (Fabric-specific timing)
        com.chronosphere.registry.ModItems.initializeSpawnEggs();
        Chronosphere.LOGGER.info("Initialized spawn eggs for Fabric");

        // Initialize Custom Portal API integration
        CustomPortalFabric.init();

        // Register block protection event handler
        BlockProtectionEventHandler.register();

        // Register server tick event for pending boss room protections
        // Check every 100 ticks (5 seconds) instead of every tick to reduce load
        final int[] tickCounter = {0};
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter[0]++;
            if (tickCounter[0] >= 100) {
                tickCounter[0] = 0;
                server.getAllLevels().forEach(level -> {
                    BossRoomProtectionProcessor.registerPendingProtections(level);
                    MasterClockProtectionProcessor.registerPendingProtections(level);
                });
            }
        });

        Chronosphere.LOGGER.info("Chronosphere Mod (Fabric) initialized");
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

        Chronosphere.LOGGER.info("Registered entity attributes for Fabric");
    }

    /**
     * Register spawn placements for custom mobs.
     * This allows mobs to spawn in Chronosphere even in daylight (always daytime dimension).
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

        Chronosphere.LOGGER.info("Registered spawn placements for custom mobs");
    }
}
