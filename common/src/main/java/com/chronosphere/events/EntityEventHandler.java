package com.chronosphere.events;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.time.ReversedResonance;
import com.chronosphere.core.time.TimeDistortionEffect;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.registry.ModDimensions;
import com.chronosphere.worldgen.spawning.TimeGuardianSpawner;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

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
 * - Time distortion effect application (Slowness IV in Chronosphere dimension)
 * - Unstable Fungus collision handling (implemented in UnstableFungus.entityInside)
 * - Time Guardian spawning in Desert Clock Tower structures
 * - Reversed resonance trigger on Time Guardian defeat
 *
 * Implementation Strategy:
 * - Uses SERVER_LEVEL_POST tick event to process entities in Chronosphere dimension
 * - Only processes entities in the Chronosphere dimension to minimize performance impact
 * - Applies time distortion effect to hostile mobs every tick
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
 */
public class EntityEventHandler {
    /**
     * Register entity event listeners.
     */
    public static void register() {
        // Register Server Level Tick event for time distortion effect and spawning
        // We use SERVER_LEVEL_POST instead of LIVING_TICK (which doesn't exist in Architectury)
        TickEvent.SERVER_LEVEL_POST.register(level -> {
            // Only process entities in Chronosphere dimension
            // Use location() to compare ResourceLocation instead of ResourceKey
            if (level.dimension().location().equals(ModDimensions.CHRONOSPHERE_DIMENSION.location())) {
                processChronosphereEntities(level);

                // Check for Desert Clock Tower structures and spawn Time Guardians
                TimeGuardianSpawner.checkAndSpawnGuardians(level);
            }
        });

        // Register entity death event for boss defeat triggers
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            // Check if the entity is a Time Guardian
            if (entity instanceof TimeGuardianEntity && entity.level() instanceof ServerLevel serverLevel) {
                handleTimeGuardianDefeat(serverLevel, (TimeGuardianEntity) entity);
            }

            // Return PASS to allow normal death processing
            return EventResult.pass();
        });

        Chronosphere.LOGGER.info("Registered EntityEventHandler with time distortion effect, Time Guardian spawning, and boss defeat triggers");
    }

    /**
     * Process all living entities in the Chronosphere dimension.
     * Applies time distortion effect to hostile mobs.
     *
     * @param level The ServerLevel to process
     */
    private static void processChronosphereEntities(ServerLevel level) {
        // Iterate through all entities and apply time distortion to living entities
        for (var entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity livingEntity) {
                TimeDistortionEffect.applyTimeDistortion(livingEntity);
            }
        }
    }

    /**
     * Handle Time Guardian defeat and trigger reversed resonance.
     *
     * @param level The ServerLevel where the defeat occurred
     * @param guardian The defeated Time Guardian entity
     */
    private static void handleTimeGuardianDefeat(ServerLevel level, TimeGuardianEntity guardian) {
        Chronosphere.LOGGER.info(
            "Time Guardian defeated at [{}, {}, {}]",
            guardian.getX(), guardian.getY(), guardian.getZ()
        );

        // Trigger reversed resonance (30 seconds)
        ReversedResonance.triggerOnTimeGuardianDefeat(
            level,
            guardian.position()
        );
    }
}

