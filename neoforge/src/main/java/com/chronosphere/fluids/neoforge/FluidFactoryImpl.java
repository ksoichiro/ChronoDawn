package com.chronosphere.fluids.neoforge;

import com.chronosphere.fluids.DecorativeWaterFluid;
import com.chronosphere.neoforge.registry.ModFluidTypes;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * NeoForge-specific implementation of FluidFactory.
 *
 * NeoForge requires FluidType support, so DecorativeWaterFluid instances
 * with getFluidType() implementation are provided.
 */
public class FluidFactoryImpl {
    /**
     * Create a Decorative Water Source fluid instance for NeoForge.
     *
     * @return Decorative Water Source fluid instance with FluidType support
     */
    public static FlowingFluid createDecorativeWaterSource() {
        return new DecorativeWaterFluid.Source() {
            @Override
            public FluidType getFluidType() {
                return ModFluidTypes.DECORATIVE_WATER_TYPE.get();
            }
        };
    }

    /**
     * Create a Decorative Water Flowing fluid instance for NeoForge.
     *
     * @return Decorative Water Flowing fluid instance with FluidType support
     */
    public static FlowingFluid createDecorativeWaterFlowing() {
        return new DecorativeWaterFluid.Flowing() {
            @Override
            public FluidType getFluidType() {
                return ModFluidTypes.DECORATIVE_WATER_TYPE.get();
            }
        };
    }
}
