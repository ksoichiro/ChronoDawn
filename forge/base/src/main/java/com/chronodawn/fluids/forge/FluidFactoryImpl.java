package com.chronodawn.fluids.forge;

import com.chronodawn.fluids.DecorativeWaterFluid;
import com.chronodawn.forge.registry.ModFluidTypes;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.fluids.FluidType;

/**
 * Forge-specific implementation of FluidFactory.
 *
 * Forge requires FluidType support, so DecorativeWaterFluid instances
 * with getFluidType() implementation are provided.
 */
public class FluidFactoryImpl {
    /**
     * Create a Decorative Water Source fluid instance for Forge.
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
     * Create a Decorative Water Flowing fluid instance for Forge.
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
