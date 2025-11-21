package com.chronosphere.fabric;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.entities.bosses.TimeTyrantEntity;
import com.chronosphere.entities.mobs.ClockworkSentinelEntity;
import com.chronosphere.entities.mobs.TemporalWraithEntity;
import com.chronosphere.entities.mobs.TimeKeeperEntity;
import com.chronosphere.fabric.compat.CustomPortalFabric;
import com.chronosphere.registry.ModEntities;
import com.chronosphere.worldgen.biomes.ChronosphereOverworldRegion;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import terrablender.api.Regions;

public class ChronosphereFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Chronosphere.init();

        // Register entity attributes (Fabric-specific)
        registerEntityAttributes();

        // Register spawn placements (Fabric-specific)
        registerSpawnPlacements();

        // Initialize Custom Portal API integration
        CustomPortalFabric.init();

        // Register TerraBlender regions for Strange Forest biome
        registerTerraBlenderRegions();

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
            ModEntities.TIME_TYRANT.get(),
            TimeTyrantEntity.createAttributes()
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

        Chronosphere.LOGGER.info("Registered spawn placements for custom mobs");
    }

    /**
     * Register TerraBlender regions for biome injection.
     * This adds Strange Forest biome to the Overworld.
     */
    private void registerTerraBlenderRegions() {
        // Register Strange Forest biome region with weight 2 (rare but discoverable)
        Regions.register(new ChronosphereOverworldRegion(
            ResourceLocation.fromNamespaceAndPath("chronosphere", "overworld"),
            2
        ));

        Chronosphere.LOGGER.info("Registered TerraBlender regions for Strange Forest biome");
    }
}
