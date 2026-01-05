package com.chronodawn.fabric.registry;

import com.chronodawn.ChronoDawn;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

/**
 * Registry for custom particle types in Fabric.
 */
public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.PARTICLE_TYPE);

    /**
     * ChronoDawn portal particle - orange colored portal particle
     * with "sucking in" movement like Nether portal.
     */
    public static final RegistrySupplier<SimpleParticleType> CHRONO_DAWN_PORTAL =
        PARTICLE_TYPES.register("chronodawn_portal", () -> FabricParticleTypes.simple());

    /**
     * Register particle types.
     */
    public static void register() {
        PARTICLE_TYPES.register();
        ChronoDawn.LOGGER.info("Registered particle types for Fabric");
    }
}
