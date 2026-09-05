package com.chronodawn.forge.registry;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for custom particle types in Forge.
 */
public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(Registries.PARTICLE_TYPE, ChronoDawn.MOD_ID);

    /**
     * ChronoDawn portal particle - orange colored portal particle
     * with "sucking in" movement like Nether portal.
     */
    public static final Supplier<SimpleParticleType> CHRONO_DAWN_PORTAL =
        PARTICLE_TYPES.register("chronodawn_portal", () -> new SimpleParticleType(false));

    /**
     * Register particle types with the mod event bus.
     *
     * @param eventBus The mod event bus
     */
    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
        ChronoDawn.LOGGER.debug("Registered particle types for Forge");
    }
}
