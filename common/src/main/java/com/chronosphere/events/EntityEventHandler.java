package com.chronosphere.events;

import com.chronosphere.Chronosphere;
import com.chronosphere.core.time.TimeDistortionEffect;
import com.chronosphere.registry.ModDimensions;
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
 *
 * Implemented features:
 * - Time distortion effect application (Slowness IV in Chronosphere dimension)
 * - Unstable Fungus collision handling (implemented in UnstableFungus.entityInside)
 *
 * Implementation Strategy:
 * - Uses SERVER_LEVEL_POST tick event to process entities in Chronosphere dimension
 * - Only processes entities in the Chronosphere dimension to minimize performance impact
 * - Applies time distortion effect to hostile mobs every tick
 * - Unstable Fungus collision effects are handled directly in the block class (UnstableFungus.java)
 *   using the entityInside method, which is the standard Minecraft approach for block collisions
 *
 * TODO: Implement in future phases:
 * - Boss defeat triggers (reversed resonance, dimension stabilization)
 *
 * Reference: data-model.md (Time Distortion Effects, Entities)
 * Task: T074 [US1] Add entity tick event handler for Slowness IV application
 * Task: T086 [US1] Unstable Fungus collision handler (implemented in UnstableFungus.java)
 */
public class EntityEventHandler {
    /**
     * Register entity event listeners.
     */
    public static void register() {
        // Register Server Level Tick event for time distortion effect
        // We use SERVER_LEVEL_POST instead of LIVING_TICK (which doesn't exist in Architectury)
        TickEvent.SERVER_LEVEL_POST.register(level -> {
            // Only process entities in Chronosphere dimension
            // Use location() to compare ResourceLocation instead of ResourceKey
            if (level.dimension().location().equals(ModDimensions.CHRONOSPHERE_DIMENSION.location())) {
                processChronosphereEntities(level);
            }
        });

        Chronosphere.LOGGER.info("Registered EntityEventHandler with time distortion effect");
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
}
