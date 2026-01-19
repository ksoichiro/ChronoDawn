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
package com.chronodawn.neoforge;

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
import com.chronodawn.entities.mobs.EpochHuskEntity;
import com.chronodawn.entities.mobs.ForgottenMinuteEntity;
import com.chronodawn.entities.mobs.ChronalLeechEntity;
import com.chronodawn.entities.mobs.MomentCreeperEntity;
import com.chronodawn.neoforge.registry.ModFluidTypes;
import com.chronodawn.neoforge.registry.ModLootModifiers;
import com.chronodawn.neoforge.registry.ModParticles;
import com.chronodawn.registry.ModEntities;
import com.chronodawn.registry.ModItems;
import com.chronodawn.worldgen.processors.BossRoomProtectionProcessor;
import com.chronodawn.worldgen.protection.BlockProtectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(ChronoDawn.MOD_ID)
public class ChronoDawnNeoForge {
    private int tickCounter = 0;

    public ChronoDawnNeoForge(IEventBus modEventBus) {
        // Register FluidTypes (NeoForge-specific, must be registered before ChronoDawn.init())
        ModFluidTypes.register(modEventBus);

        ChronoDawn.init();

        // Register particle types (NeoForge-specific)
        ModParticles.register(modEventBus);

        // Register loot modifiers (NeoForge-specific)
        ModLootModifiers.register(modEventBus);

        // Register entity attributes (NeoForge-specific)
        modEventBus.addListener(this::registerEntityAttributes);

        // Register spawn placements (NeoForge-specific)
        modEventBus.addListener(this::registerSpawnPlacements);

        // Common setup - initialize spawn eggs after entities are registered
        modEventBus.addListener(this::commonSetup);

        // Register server tick event for pending boss room protections
        NeoForge.EVENT_BUS.addListener(this::onServerTick);

        // Register block protection events (breaking and placement)
        NeoForge.EVENT_BUS.addListener(this::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(this::onBlockPlace);

        ChronoDawn.LOGGER.info("ChronoDawn Mod (NeoForge) initialized");
    }

    /**
     * Server tick event handler for NeoForge.
     * Registers pending boss room protections.
     * Check every 100 ticks (5 seconds) instead of every tick to reduce load.
     */
    private void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter >= 100) {
            tickCounter = 0;
            event.getServer().getAllLevels().forEach(level -> {
                BossRoomProtectionProcessor.registerPendingProtections(level);
            });
        }
    }

    /**
     * Block break event handler for NeoForge.
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
     * Block place event handler for NeoForge.
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
     * Register entity attributes for NeoForge.
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

        ChronoDawn.LOGGER.info("Registered entity attributes for NeoForge");
    }

    /**
     * Common setup for NeoForge.
     * Initializes spawn eggs after all entities are registered.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize spawn eggs - must be done after entities are registered
            ModItems.initializeSpawnEggs();
            ChronoDawn.LOGGER.info("Initialized spawn eggs for NeoForge");
        });
    }

    /**
     * Register spawn placements for custom mobs.
     * This allows mobs to spawn in ChronoDawn even in daylight (always daytime dimension).
     *
     * NeoForge uses RegisterSpawnPlacementsEvent instead of direct SpawnPlacements.register() calls.
     */
    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // Temporal Wraith - spawns on ground in daylight (Monster with any light)
        event.register(
            ModEntities.TEMPORAL_WRAITH.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TemporalWraithEntity::checkTemporalWraithSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        // Clockwork Sentinel - spawns on ground in daylight (Monster with any light)
        event.register(
            ModEntities.CLOCKWORK_SENTINEL.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ClockworkSentinelEntity::checkClockworkSentinelSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        // Time Keeper - spawns on ground in bright areas (Creature/Animal spawn rules)
        event.register(
            ModEntities.TIME_KEEPER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            TimeKeeperEntity::checkTimeKeeperSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        // Floq - spawns on ground like animals
        event.register(
            ModEntities.FLOQ.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            FloqEntity::checkFloqSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        // Epoch Husk - spawns on ground in daylight
        event.register(
            ModEntities.EPOCH_HUSK.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            EpochHuskEntity::checkEpochHuskSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        // Forgotten Minute - flying mob with no spawn restrictions
        event.register(
            ModEntities.FORGOTTEN_MINUTE.get(),
            SpawnPlacementTypes.NO_RESTRICTIONS,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ForgottenMinuteEntity::checkForgottenMinuteSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        // Chronal Leech - spawns on ground in daylight
        event.register(
            ModEntities.CHRONAL_LEECH.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            ChronalLeechEntity::checkChronalLeechSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        // Moment Creeper - spawns on ground in daylight
        event.register(
            ModEntities.MOMENT_CREEPER.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            MomentCreeperEntity::checkMomentCreeperSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );

        ChronoDawn.LOGGER.info("Registered spawn placements for custom mobs");
    }
}
