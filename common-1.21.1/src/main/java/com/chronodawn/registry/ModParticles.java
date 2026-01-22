package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

/**
 * Particle type registration for ChronoDawn mod.
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.PARTICLE_TYPE);

    /**
     * Chrono Dawn Portal particle - golden/orange particles that float gently like Nether Portal
     */
    public static final RegistrySupplier<SimpleParticleType> CHRONO_DAWN_PORTAL =
        PARTICLE_TYPES.register("chrono_dawn_portal", ModParticles::createSimpleParticleType);

    /**
     * Create a simple particle type (helper method to avoid constructor access issues).
     */
    private static SimpleParticleType createSimpleParticleType() {
        return new SimpleParticleType(false) {};
    }

    public static void register() {
        PARTICLE_TYPES.register();
    }
}

