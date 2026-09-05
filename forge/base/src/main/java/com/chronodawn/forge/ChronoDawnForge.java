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
package com.chronodawn.forge;

import com.chronodawn.ChronoDawn;
import com.chronodawn.entities.bosses.*;
import com.chronodawn.entities.mobs.*;
import com.chronodawn.forge.event.OverlayPackFinder;
import com.chronodawn.forge.registry.ModFluidTypes;
import com.chronodawn.forge.registry.ModLootModifiers;
import com.chronodawn.forge.registry.ModParticles;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import com.chronodawn.worldgen.processors.BossRoomProtectionProcessor;
import com.chronodawn.worldgen.protection.BlockProtectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ChronoDawn.MOD_ID)
public class ChronoDawnForge {
    private int tickCounter = 0;

    public ChronoDawnForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register FluidTypes (Forge-specific, must be registered before ChronoDawn.init())
        ModFluidTypes.register(modEventBus);

        ChronoDawn.init();
        com.chronodawn.network.ModNetworking.register();

        // Register particle types (Forge-specific)
        ModParticles.register(modEventBus);

        // Register loot modifiers (Forge-specific)
        ModLootModifiers.register(modEventBus);

        // Register entity attributes (Forge-specific)
        modEventBus.addListener(this::registerEntityAttributes);

        // Register spawn placements (Forge-specific)
        modEventBus.addListener(this::registerSpawnPlacements);

        // Common setup - initialize spawn eggs after entities are registered
        modEventBus.addListener(this::commonSetup);

        // Register the runtime config overlay datapack with the server pack repository.
        // OverlayPackBootstrap.writeOverlay() ran during ChronoDawn.init() above; this
        // event fires later (when PackRepository is constructed for a world load).
        modEventBus.addListener((AddPackFindersEvent event) -> OverlayPackFinder.onAddPackFinders(event));

        // Register server tick event for pending boss room protections
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);

        // Register block protection events (breaking and placement)
        MinecraftForge.EVENT_BUS.addListener(this::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(this::onBlockPlace);

        ChronoDawn.LOGGER.info("ChronoDawn Mod (Forge) initialized");
    }

    /**
     * Server tick event handler for Forge.
     * Processes pending portal teleports and registers pending boss room protections.
     * Check boss room protections every 100 ticks (5 seconds) instead of every tick to reduce load.
     * Process portal teleports every tick to ensure responsiveness.
     */
    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Process pending portal teleports (every tick, version-specific)
        // CRITICAL: This must run after ALL entity ticks to avoid ConcurrentModificationException
        VersionSpecificServerHelper.processPendingTeleports(event.getServer());

        // Process boss room protections (every 100 ticks)
        tickCounter++;
        if (tickCounter >= 100) {
            tickCounter = 0;
            event.getServer().getAllLevels().forEach(level -> {
                BossRoomProtectionProcessor.registerPendingProtections(level);
            });
        }
    }

    /**
     * Block break event handler for Forge.
     * Prevents players from breaking blocks in protected boss rooms.
     */
    private void onBlockBreak(BlockEvent.BreakEvent event) {
        var player = event.getPlayer();

        // Allow creative mode players to break anything
        if (player.isCreative()) {
            return;
        }

        // Check if this block is protected (need to cast LevelAccessor to Level)
        if (event.getLevel() instanceof net.minecraft.world.level.Level level) {
            if (BlockProtectionHandler.isProtected(level, event.getPos())) {
                // Display warning message
                player.displayClientMessage(
                    Component.translatable("message.chronodawn.boss_room_protected"),
                    true // action bar
                );

                // Cancel block break event
                event.setCanceled(true);
            }
        }
    }

    /**
     * Block place event handler for Forge.
     * Prevents players from placing blocks in protected boss rooms.
     */
    private void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        // Only handle player placement
        if (!(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)) {
            return;
        }

        // Allow creative mode players to place anything
        if (player.isCreative()) {
            return;
        }

        // Check if the placement position is protected (need to cast LevelAccessor to Level)
        if (event.getLevel() instanceof net.minecraft.world.level.Level level) {
            if (BlockProtectionHandler.isProtected(level, event.getPos())) {
                // Display warning message
                player.displayClientMessage(
                    Component.translatable("message.chronodawn.boss_room_no_placement"),
                    true // action bar
                );

                // Cancel block placement event
                event.setCanceled(true);
            }
        }
    }

    /**
     * Register entity attributes for Forge.
     * This is required for all custom living entities to have proper attributes.
     */
    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        // Boss entities
        event.put(
            ModEntities.TIME_GUARDIAN.get(),
            TimeGuardianEntity.createAttributes().build()
        );

        event.put(
            ModEntities.CHRONOS_WARDEN.get(),
            ChronosWardenEntity.createAttributes().build()
        );

        event.put(
            ModEntities.CLOCKWORK_COLOSSUS.get(),
            ClockworkColossusEntity.createAttributes().build()
        );

        event.put(
            ModEntities.TIME_TYRANT.get(),
            TimeTyrantEntity.createAttributes().build()
        );

        event.put(
            ModEntities.TEMPORAL_PHANTOM.get(),
            TemporalPhantomEntity.createAttributes().build()
        );

        event.put(
            ModEntities.ENTROPY_KEEPER.get(),
            EntropyKeeperEntity.createAttributes().build()
        );

        // Custom mobs
        event.put(
            ModEntities.TEMPORAL_WRAITH.get(),
            TemporalWraithEntity.createAttributes().build()
        );

        event.put(
            ModEntities.CLOCKWORK_SENTINEL.get(),
            ClockworkSentinelEntity.createAttributes().build()
        );

        event.put(
            ModEntities.TIME_KEEPER.get(),
            TimeKeeperEntity.createAttributes().build()
        );

        event.put(
            ModEntities.FLOQ.get(),
            FloqEntity.createAttributes().build()
        );

        event.put(
            ModEntities.EPOCH_HUSK.get(),
            EpochHuskEntity.createAttributes().build()
        );

        event.put(
            ModEntities.FORGOTTEN_MINUTE.get(),
            ForgottenMinuteEntity.createAttributes().build()
        );

        event.put(
            ModEntities.CHRONAL_LEECH.get(),
            ChronalLeechEntity.createAttributes().build()
        );

        event.put(
            ModEntities.MOMENT_CREEPER.get(),
            MomentCreeperEntity.createAttributes().build()
        );

        event.put(
            ModEntities.GLIDE_FISH.get(),
            GlideFishEntity.createAttributes().build()
        );

        event.put(
            ModEntities.TIMELINE_STRIDER.get(),
            TimelineStriderEntity.createAttributes().build()
        );

        event.put(
            ModEntities.HOURGLASS_GOLEM.get(),
            HourglassGolemEntity.createAttributes().build()
        );

        event.put(
            ModEntities.SECONDHAND_ARCHER.get(),
            SecondhandArcherEntity.createAttributes().build()
        );

        event.put(
            ModEntities.PARADOX_CRAWLER.get(),
            ParadoxCrawlerEntity.createAttributes().build()
        );

        event.put(
            ModEntities.CHRONO_TURTLE.get(),
            ChronoTurtleEntity.createAttributes().build()
        );

        event.put(
            ModEntities.TIMEBOUND_RABBIT.get(),
            TimeboundRabbitEntity.createAttributes().build()
        );

        event.put(
            ModEntities.PULSE_HOG.get(),
            PulseHogEntity.createAttributes().build()
        );

        event.put(
            ModEntities.SECONDWING_FOWL.get(),
            SecondwingFowlEntity.createAttributes().build()
        );

        event.put(
            ModEntities.TICKING_SHEEP.get(),
            TickingSheepEntity.createAttributes().build()
        );

        event.put(
            ModEntities.CHRONO_BOVINE.get(),
            ChronoBovineEntity.createAttributes().build()
        );

        event.put(
            ModEntities.TEMPORAL_CAPRID.get(),
            TemporalCapridEntity.createAttributes().build()
        );

        event.put(
            ModEntities.CHRONO_URSID.get(),
            ChronoUrsidEntity.createAttributes().build()
        );

        ChronoDawn.LOGGER.debug("Registered entity attributes for Forge");
    }

    /**
     * Common setup for Forge.
     * Initializes spawn eggs after all entities are registered.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize spawn eggs - must be done after entities are registered
            ModItems.initializeSpawnEggs();
            ChronoDawn.LOGGER.debug("Initialized spawn eggs for Forge");
        });
    }

    /**
     * Register spawn placements for custom mobs.
     * This allows mobs to spawn in ChronoDawn even in daylight (always daytime dimension).
     *
     * Forge uses SpawnPlacementRegisterEvent instead of direct SpawnPlacements.register() calls.
     */
    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        // Temporal Wraith - spawns on ground in daylight (Monster with any light)
        event.register(
            ModEntities.TEMPORAL_WRAITH.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TemporalWraithEntity::checkTemporalWraithSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Clockwork Sentinel - spawns on ground in daylight (Monster with any light)
        event.register(
            ModEntities.CLOCKWORK_SENTINEL.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ClockworkSentinelEntity::checkClockworkSentinelSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Time Keeper - spawns on ground in bright areas (Creature/Animal spawn rules)
        event.register(
            ModEntities.TIME_KEEPER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TimeKeeperEntity::checkTimeKeeperSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Floq - spawns on ground like animals
        event.register(
            ModEntities.FLOQ.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            FloqEntity::checkFloqSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Epoch Husk - spawns on ground in daylight
        event.register(
            ModEntities.EPOCH_HUSK.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            EpochHuskEntity::checkEpochHuskSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Forgotten Minute - flying mob with no spawn restrictions
        event.register(
            ModEntities.FORGOTTEN_MINUTE.get(),
            SpawnPlacements.Type.NO_RESTRICTIONS,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ForgottenMinuteEntity::checkForgottenMinuteSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Chronal Leech - spawns on ground in daylight
        event.register(
            ModEntities.CHRONAL_LEECH.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ChronalLeechEntity::checkChronalLeechSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Moment Creeper - spawns on ground in daylight
        event.register(
            ModEntities.MOMENT_CREEPER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            MomentCreeperEntity::checkMomentCreeperSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // GlideFish - spawns in water
        event.register(
            ModEntities.GLIDE_FISH.get(),
            SpawnPlacements.Type.IN_WATER,
            Heightmap.Types.OCEAN_FLOOR,
            net.minecraft.world.entity.animal.WaterAnimal::checkSurfaceWaterAnimalSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Timeline Strider - spawns on ground in daylight
        event.register(
            ModEntities.TIMELINE_STRIDER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TimelineStriderEntity::checkTimelineStriderSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Hourglass Golem - spawns on ground in daylight
        event.register(
            ModEntities.HOURGLASS_GOLEM.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            HourglassGolemEntity::checkHourglassGolemSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Secondhand Archer - spawns on ground in daylight
        event.register(
            ModEntities.SECONDHAND_ARCHER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            SecondhandArcherEntity::checkSecondhandArcherSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Paradox Crawler - spawns on ground in daylight
        event.register(
            ModEntities.PARADOX_CRAWLER.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ParadoxCrawlerEntity::checkParadoxCrawlerSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Chrono Turtle - spawns in water
        event.register(
            ModEntities.CHRONO_TURTLE.get(),
            SpawnPlacements.Type.IN_WATER,
            Heightmap.Types.OCEAN_FLOOR,
            net.minecraft.world.entity.animal.WaterAnimal::checkSurfaceWaterAnimalSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Timebound Rabbit - spawns on ground like animals
        event.register(
            ModEntities.TIMEBOUND_RABBIT.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TimeboundRabbitEntity::checkTimeboundRabbitSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Pulse Hog - spawns on ground like animals
        event.register(
            ModEntities.PULSE_HOG.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            PulseHogEntity::checkPulseHogSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Secondwing Fowl - spawns on ground like animals
        event.register(
            ModEntities.SECONDWING_FOWL.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            SecondwingFowlEntity::checkSecondwingFowlSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Ticking Sheep - spawns on ground like animals
        event.register(
            ModEntities.TICKING_SHEEP.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TickingSheepEntity::checkTickingSheepSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Chrono Bovine - spawns on ground like animals
        event.register(
            ModEntities.CHRONO_BOVINE.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ChronoBovineEntity::checkChronoBovineSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Temporal Caprid - spawns on ground like goats
        event.register(
            ModEntities.TEMPORAL_CAPRID.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TemporalCapridEntity::checkTemporalCapridSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        // Chrono Ursid - spawns on ground like animals (snowy biomes)
        event.register(
            ModEntities.CHRONO_URSID.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ChronoUrsidEntity::checkChronoUrsidSpawnRules,
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );

        ChronoDawn.LOGGER.debug("Registered spawn placements for custom mobs");
    }
}
