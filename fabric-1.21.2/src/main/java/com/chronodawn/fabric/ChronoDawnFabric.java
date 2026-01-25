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
package com.chronodawn.fabric;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.ChronosWardenEntity;
import com.chronodawn.entities.bosses.ClockworkColossusEntity;
import com.chronodawn.entities.bosses.EntropyKeeperEntity;
import com.chronodawn.entities.bosses.TemporalPhantomEntity;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.entities.bosses.TimeTyrantEntity;
import com.chronodawn.entities.mobs.ChronalLeechEntity;
import com.chronodawn.entities.mobs.ClockworkSentinelEntity;
import com.chronodawn.entities.mobs.EpochHuskEntity;
import com.chronodawn.entities.mobs.FloqEntity;
import com.chronodawn.entities.mobs.ForgottenMinuteEntity;
import com.chronodawn.entities.mobs.GlideFishEntity;
import com.chronodawn.entities.mobs.MomentCreeperEntity;
import com.chronodawn.entities.mobs.ParadoxCrawlerEntity;
import com.chronodawn.entities.mobs.SecondhandArcherEntity;
import com.chronodawn.entities.mobs.ChronoTurtleEntity;
import com.chronodawn.entities.mobs.TimeboundRabbitEntity;
import com.chronodawn.entities.mobs.TemporalWraithEntity;
import com.chronodawn.entities.mobs.TimelineStriderEntity;
import com.chronodawn.entities.mobs.TimeKeeperEntity;
import com.chronodawn.fabric.event.BlockProtectionEventHandler;
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
        ChronoDawn.LOGGER.debug("Initialized spawn eggs for Fabric");

        // Register fuel items
        ChronoDawnFuelRegistry.register();

        // Register block protection event handler
        BlockProtectionEventHandler.register();

        // Register server tick event for pending boss room protections and portal teleports
        // Check boss room protections every 100 ticks (5 seconds) instead of every tick to reduce load
        // Process portal teleports every tick to ensure responsiveness
        final int[] tickCounter = {0};
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Process pending portal teleports (every tick)
            // CRITICAL: This must run after ALL entity ticks to avoid ConcurrentModificationException
            com.chronodawn.blocks.ChronoDawnPortalBlock.processPendingTeleports(server);

            // Process boss room protections (every 100 ticks)
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

        FabricDefaultAttributeRegistry.register(
            ModEntities.EPOCH_HUSK.get(),
            EpochHuskEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.FORGOTTEN_MINUTE.get(),
            ForgottenMinuteEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.CHRONAL_LEECH.get(),
            ChronalLeechEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.MOMENT_CREEPER.get(),
            MomentCreeperEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.GLIDE_FISH.get(),
            GlideFishEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.TIMELINE_STRIDER.get(),
            TimelineStriderEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.SECONDHAND_ARCHER.get(),
            SecondhandArcherEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.PARADOX_CRAWLER.get(),
            ParadoxCrawlerEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.CHRONO_TURTLE.get(),
            ChronoTurtleEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.TIMEBOUND_RABBIT.get(),
            TimeboundRabbitEntity.createAttributes()
        );

        ChronoDawn.LOGGER.debug("Registered entity attributes for Fabric");
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

        // Epoch Husk - spawns on ground in daylight
        SpawnPlacements.register(
            ModEntities.EPOCH_HUSK.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            EpochHuskEntity::checkEpochHuskSpawnRules
        );

        // Forgotten Minute - flying mob with no spawn restrictions
        SpawnPlacements.register(
            ModEntities.FORGOTTEN_MINUTE.get(),
            SpawnPlacementTypes.NO_RESTRICTIONS,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ForgottenMinuteEntity::checkForgottenMinuteSpawnRules
        );

        // Chronal Leech - spawns on ground in daylight
        SpawnPlacements.register(
            ModEntities.CHRONAL_LEECH.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ChronalLeechEntity::checkChronalLeechSpawnRules
        );

        // Moment Creeper - spawns on ground in daylight
        SpawnPlacements.register(
            ModEntities.MOMENT_CREEPER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            MomentCreeperEntity::checkMomentCreeperSpawnRules
        );

        // GlideFish - spawns in water
        SpawnPlacements.register(
            ModEntities.GLIDE_FISH.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.OCEAN_FLOOR,
            net.minecraft.world.entity.animal.WaterAnimal::checkSurfaceWaterAnimalSpawnRules
        );

        // Timeline Strider - spawns on ground in daylight (Monster with any light)
        SpawnPlacements.register(
            ModEntities.TIMELINE_STRIDER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TimelineStriderEntity::checkTimelineStriderSpawnRules
        );

        // Secondhand Archer - spawns on ground in daylight
        SpawnPlacements.register(
            ModEntities.SECONDHAND_ARCHER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            SecondhandArcherEntity::checkSecondhandArcherSpawnRules
        );

        // Paradox Crawler - spawns on ground in daylight
        SpawnPlacements.register(
            ModEntities.PARADOX_CRAWLER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ParadoxCrawlerEntity::checkParadoxCrawlerSpawnRules
        );

        // Chrono Turtle - spawns in water
        SpawnPlacements.register(
            ModEntities.CHRONO_TURTLE.get(),
            SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.OCEAN_FLOOR,
            net.minecraft.world.entity.animal.WaterAnimal::checkSurfaceWaterAnimalSpawnRules
        );

        // Timebound Rabbit - spawns on ground like animals
        SpawnPlacements.register(
            ModEntities.TIMEBOUND_RABBIT.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TimeboundRabbitEntity::checkTimeboundRabbitSpawnRules
        );

        ChronoDawn.LOGGER.debug("Registered spawn placements for custom mobs");
    }
}
