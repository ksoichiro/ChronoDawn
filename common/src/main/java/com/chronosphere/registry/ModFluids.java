package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.fluids.DecorativeWaterFluid;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

/**
 * Architectury Registry wrapper for custom fluids.
 */
public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Chronosphere.MOD_ID, Registries.FLUID);

    /**
     * Decorative Water (Source) - Water source block that looks like vanilla water.
     * Used in structure NBT files to distinguish from Aquifer water.
     */
    public static final RegistrySupplier<FlowingFluid> DECORATIVE_WATER = FLUIDS.register(
        "decorative_water",
        () -> new DecorativeWaterFluid.Source()
    );

    /**
     * Decorative Water (Flowing) - Flowing water that looks like vanilla water.
     */
    public static final RegistrySupplier<FlowingFluid> DECORATIVE_WATER_FLOWING = FLUIDS.register(
        "decorative_water_flowing",
        () -> new DecorativeWaterFluid.Flowing()
    );

    /**
     * Initialize fluid registry.
     */
    public static void register() {
        FLUIDS.register();
        Chronosphere.LOGGER.info("Registered ModFluids");
    }
}
