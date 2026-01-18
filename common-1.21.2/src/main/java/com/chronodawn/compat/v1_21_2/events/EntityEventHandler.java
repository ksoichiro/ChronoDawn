package com.chronodawn.compat.v1_21_2.events;

import com.chronodawn.ChronoDawn;
import com.chronodawn.core.teleport.TeleporterChargingHandler;
import com.chronodawn.core.time.ReversedResonance;
import com.chronodawn.core.time.TimeDistortionEffect;
import com.chronodawn.entities.bosses.TimeGuardianEntity;
import com.chronodawn.registry.ModDimensions;
import com.chronodawn.worldgen.spawning.TimeGuardianSpawner;
import com.chronodawn.worldgen.spawning.TimeTyrantSpawner;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entity event handler using Architectury Event API.
 *
 * This handler manages entity-related events such as:
 * - Entity tick events (for time distortion effects)
 * - Entity collision events (for Unstable Fungus)
 * - Entity damage events (for boss mechanics)
 * - Entity death events (for boss defeat triggers)
 *
 * Implemented features:
 * - Time distortion effect application (Slowness IV in ChronoDawn dimension)
 * - Unstable Fungus collision handling (implemented in UnstableFungus.entityInside)
 * - Time Guardian spawning in Desert Clock Tower structures
 * - Reversed resonance trigger on Time Guardian defeat
 *
 * Implementation Strategy:
 * - Uses SERVER_LEVEL_POST tick event to process entities in ChronoDawn dimension
 * - Only processes entities in the ChronoDawn dimension to minimize performance impact
 * - Applies time distortion effect to hostile mobs every 5 ticks (T178 performance optimization)
 * - Checks for Desert Clock Tower structures and spawns Time Guardians periodically
 * - Hooks entity death event to trigger reversed resonance on boss defeats
 * - Unstable Fungus collision effects are handled directly in the block class (UnstableFungus.java)
 *   using the entityInside method, which is the standard Minecraft approach for block collisions
 *
 * TODO: Implement in future phases:
 * - Time Tyrant defeat triggers (reversed resonance, dimension stabilization)
 *
 * Reference: data-model.md (Time Distortion Effects, Entities, Reversed Resonance)
 * Task: T074 [US1] Add entity tick event handler for Slowness IV application
 * Task: T086 [US1] Unstable Fungus collision handler (implemented in UnstableFungus.java)
 * Task: T114 [US2] Time Guardian spawn logic integration
 * Task: T115 [US2] Implement reversed resonance trigger on Time Guardian defeat
 * Task: T178 [Performance] Optimize entity tick rate to 5-tick intervals
 */
public class EntityEventHandler {
    /**
     * Tick counter for time distortion effect processing.
     * Processes every 5 ticks to reduce performance impact.
     * T430: Now uses per-dimension AtomicInteger for thread-safety and dimension isolation.
     */
    private static final Map<ResourceLocation, AtomicInteger> timeDistortionTickCounters = new ConcurrentHashMap<>();
    /**
     * Register entity event listeners.
     */
    public static void register() {
        // Register Server Level Tick event for time distortion effect and spawning
        // We use SERVER_LEVEL_POST instead of LIVING_TICK (which doesn't exist in Architectury)
        TickEvent.SERVER_LEVEL_POST.register(level -> {
            // Only process entities in ChronoDawn dimension
            // Use location() to compare ResourceLocation instead of ResourceKey
            if (level.dimension().location().equals(ModDimensions.CHRONO_DAWN_DIMENSION.location())) {
                // T178: Optimize time distortion processing to 5-tick intervals
                // T430: Use per-dimension tick counter for thread-safety and dimension isolation
                ResourceLocation dimensionId = level.dimension().location();
                timeDistortionTickCounters.putIfAbsent(dimensionId, new AtomicInteger(0));
                AtomicInteger tickCounter = timeDistortionTickCounters.get(dimensionId);

                if (tickCounter.incrementAndGet() >= 5) {
                    tickCounter.set(0);
                    processChronoDawnEntities(level);
                }

                // Check for Desert Clock Tower structures and spawn Time Guardians
                TimeGuardianSpawner.checkAndSpawnGuardians(level);

                // Note: Time Tyrant spawns when Boss Room Door is opened, not on tick
                // See BossRoomDoorBlock.use() for spawn logic
            }
        });

        // Register entity death event for boss defeat triggers
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            // Check if the entity is a Time Guardian
            if (entity instanceof TimeGuardianEntity && entity.level() instanceof ServerLevel serverLevel) {
                handleTimeGuardianDefeat(serverLevel, (TimeGuardianEntity) entity);
            }

            // Check if the entity is a Time Tyrant (T138-T140)
            if (entity instanceof com.chronodawn.entities.bosses.TimeTyrantEntity && entity.level() instanceof ServerLevel serverLevel) {
                handleTimeTyrantDefeat(serverLevel, (com.chronodawn.entities.bosses.TimeTyrantEntity) entity);
            }

            // Return PASS to allow normal death processing
            return EventResult.pass();
        });

        // Register player tick event for teleporter charging and Chrono Aegis Clarity
        TickEvent.PLAYER_POST.register(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                TeleporterChargingHandler.tick(serverPlayer);
                handleChronoAegisClarity(serverPlayer);
            }
        });

        ChronoDawn.LOGGER.info("Registered EntityEventHandler with time distortion effect, Time Guardian spawning, Time Tyrant spawning, boss defeat triggers, and teleporter charging");
    }

    /**
     * Process all living entities in the ChronoDawn dimension.
     * Applies time distortion effect to hostile mobs.
     *
     * @param level The ServerLevel to process
     */
    private static void processChronoDawnEntities(ServerLevel level) {
        // Iterate through all entities and apply time distortion to living entities
        for (var entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity livingEntity) {
                TimeDistortionEffect.applyTimeDistortion(livingEntity);
            }
        }
    }

    /**
     * Handle Time Guardian defeat and trigger reversed resonance.
     * Also spawns a down teleporter on the boss floor.
     *
     * @param level The ServerLevel where the defeat occurred
     * @param guardian The defeated Time Guardian entity
     */
    private static void handleTimeGuardianDefeat(ServerLevel level, TimeGuardianEntity guardian) {
        ChronoDawn.LOGGER.info(
            "Time Guardian defeated at [{}, {}, {}]",
            guardian.getX(), guardian.getY(), guardian.getZ()
        );

        // Find UP teleporter on 4th floor (should be about 8 blocks below guardian)
        net.minecraft.core.BlockPos searchPos = guardian.blockPosition().below(8);
        net.minecraft.core.BlockPos upTeleporterPos = findNearbyUpTeleporter(level, searchPos);

        if (upTeleporterPos == null) {
            ChronoDawn.LOGGER.error("Could not find UP teleporter on 4th floor! Cannot place DOWN teleporter.");
            return;
        }

        // Calculate DOWN teleporter position:
        // - Same X/Z as UP teleporter (centered)
        // - 6 blocks above UP teleporter (places at floor+2 on 5th floor)
        net.minecraft.core.BlockPos teleporterPos = upTeleporterPos.above(6);

        // Replace any existing block at this position
        level.setBlock(
            teleporterPos,
            com.chronodawn.registry.ModBlocks.CLOCK_TOWER_TELEPORTER.get()
                .defaultBlockState()
                .setValue(com.chronodawn.blocks.ClockTowerTeleporterBlock.DIRECTION,
                         com.chronodawn.blocks.ClockTowerTeleporterBlock.TeleportDirection.DOWN),
            3 // UPDATE_CLIENTS | UPDATE_NEIGHBORS
        );

        // Set target position in BlockEntity (points to UP teleporter position)
        if (level.getBlockEntity(teleporterPos) instanceof com.chronodawn.blocks.ClockTowerTeleporterBlockEntity be) {
            be.setTargetPos(upTeleporterPos);
        }

        ChronoDawn.LOGGER.info("Spawned DOWN teleporter at [{}, {}, {}] targeting UP teleporter at [{}, {}, {}]",
            teleporterPos.getX(), teleporterPos.getY(), teleporterPos.getZ(),
            upTeleporterPos.getX(), upTeleporterPos.getY(), upTeleporterPos.getZ()
        );
    }

    /**
     * Find nearby UP teleporter within a radius.
     * Searches horizontally and vertically around the given position.
     *
     * @param level The level to search in
     * @param center The center position to search around
     * @return The position of the UP teleporter, or null if not found
     */
    private static net.minecraft.core.BlockPos findNearbyUpTeleporter(ServerLevel level, net.minecraft.core.BlockPos center) {
        int searchRadius = 20;
        int verticalSearchRange = 10;

        // Search in horizontal plane around the center position
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                for (int y = -verticalSearchRange; y <= verticalSearchRange; y++) {
                    net.minecraft.core.BlockPos checkPos = center.offset(x, y, z);
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(checkPos);

                    if (state.getBlock() instanceof com.chronodawn.blocks.ClockTowerTeleporterBlock) {
                        com.chronodawn.blocks.ClockTowerTeleporterBlock.TeleportDirection direction =
                            state.getValue(com.chronodawn.blocks.ClockTowerTeleporterBlock.DIRECTION);

                        if (direction == com.chronodawn.blocks.ClockTowerTeleporterBlock.TeleportDirection.UP) {
                            return checkPos;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Handle Time Tyrant defeat and trigger final boss mechanics.
     * Executes T138-T140 tasks:
     * - T138: Stasis Core destruction
     * - T139: Reversed Resonance (60 seconds)
     * - T140: Dimension stabilization
     *
     * @param level The ServerLevel where the defeat occurred
     * @param tyrant The defeated Time Tyrant entity
     */
    private static void handleTimeTyrantDefeat(ServerLevel level, com.chronodawn.entities.bosses.TimeTyrantEntity tyrant) {
        ChronoDawn.LOGGER.info(
            "Time Tyrant defeated at [{}, {}, {}]",
            tyrant.getX(), tyrant.getY(), tyrant.getZ()
        );

        // T138: Stasis Core destruction
        // Broadcast message about Stasis Core destruction
        net.minecraft.network.chat.Component coreMessage = net.minecraft.network.chat.Component.translatable(
            "message.chronodawn.stasis_core_destroyed"
        ).withStyle(net.minecraft.ChatFormatting.DARK_PURPLE, net.minecraft.ChatFormatting.BOLD);

        for (net.minecraft.server.level.ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.displayClientMessage(coreMessage, false);
        }

        // T139: Reversed Resonance (60 seconds duration)
        com.chronodawn.core.time.ReversedResonance.triggerOnTimeTyrantDefeat(
            level,
            tyrant.position()
        );

        // T140: Dimension stabilization
        com.chronodawn.core.dimension.DimensionStabilizer.stabilizeDimension(level);
    }

    /**
     * Handle Chrono Aegis Clarity effect.
     * Auto-cleanses negative effects (Slowness, Weakness, Mining Fatigue) every 2 seconds.
     *
     * This implementation uses PLAYER_POST tick event instead of MobEffect.applyEffectTick()
     * to avoid ConcurrentModificationException when the game saves entity NBT data.
     *
     * @param player The player to process
     * Task: T240 [US3] Fix Clarity auto-cleanse feature
     */
    private static void handleChronoAegisClarity(ServerPlayer player) {
        // Check if player has Chrono Aegis buff
        // 1.21.1: hasEffect() takes Holder<MobEffect>
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> aegisEffect =
            net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(
                com.chronodawn.registry.ModEffects.CHRONO_AEGIS_BUFF.get()
            );

        if (!player.hasEffect(aegisEffect)) {
            return;
        }

        // Apply Clarity every 40 ticks (2 seconds)
        if (player.tickCount % 40 != 0) {
            return;
        }

        // Remove negative effects that Time Tyrant and bosses apply
        // MobEffects constants already return Holder<MobEffect> in Minecraft 1.21.1
        player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
        player.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
    }
}

