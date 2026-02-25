package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.fluids.FluidFactory;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

/**
 * Architectury Registry wrapper for custom fluids.
 * Uses platform-specific FluidFactory to support NeoForge's FluidType requirement.
 */
public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ChronoDawn.MOD_ID, Registries.FLUID);

    /**
     * Decorative Water (Source) - Water source block that looks like vanilla water.
     * Used in structure NBT files to distinguish from Aquifer water.
     * Platform-specific: NeoForge implementation includes FluidType support.
     */
    public static final RegistrySupplier<FlowingFluid> DECORATIVE_WATER = FLUIDS.register(
        "decorative_water",
        FluidFactory::createDecorativeWaterSource
    );

    /**
     * Decorative Water (Flowing) - Flowing water that looks like vanilla water.
     * Platform-specific: NeoForge implementation includes FluidType support.
     */
    public static final RegistrySupplier<FlowingFluid> DECORATIVE_WATER_FLOWING = FLUIDS.register(
        "decorative_water_flowing",
        FluidFactory::createDecorativeWaterFlowing
    );

    /**
     * Initialize fluid registry.
     */
    public static void register() {
        FLUIDS.register();
        ChronoDawn.LOGGER.debug("Registered ModFluids");
    }
}
