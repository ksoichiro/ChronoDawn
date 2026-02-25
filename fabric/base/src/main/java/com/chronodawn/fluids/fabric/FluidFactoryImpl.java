package com.chronodawn.fluids.fabric;

import com.chronodawn.fluids.DecorativeWaterFluid;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Fabric-specific implementation of FluidFactory.
 *
 * Fabric does not require FluidType, so standard DecorativeWaterFluid instances are used.
 */
public class FluidFactoryImpl {
    /**
     * Create a Decorative Water Source fluid instance for Fabric.
     *
     * @return Decorative Water Source fluid instance
     */
    public static FlowingFluid createDecorativeWaterSource() {
        return new DecorativeWaterFluid.Source();
    }

    /**
     * Create a Decorative Water Flowing fluid instance for Fabric.
     *
     * @return Decorative Water Flowing fluid instance
     */
    public static FlowingFluid createDecorativeWaterFlowing() {
        return new DecorativeWaterFluid.Flowing();
    }
}
