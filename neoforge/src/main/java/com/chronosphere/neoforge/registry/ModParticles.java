package com.chronosphere.neoforge.registry;

import com.chronosphere.Chronosphere;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for custom particle types in NeoForge.
 */
public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Chronosphere.MOD_ID);

    /**
     * Chronosphere portal particle - orange colored portal particle
     * with "sucking in" movement like Nether portal.
     */
    public static final Supplier<SimpleParticleType> CHRONOSPHERE_PORTAL =
        PARTICLE_TYPES.register("chronosphere_portal", () -> new SimpleParticleType(false));

    /**
     * Register particle types with the mod event bus.
     *
     * @param eventBus The mod event bus
     */
    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
        Chronosphere.LOGGER.info("Registered particle types for NeoForge");
    }
}
