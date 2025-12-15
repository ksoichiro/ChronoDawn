package com.chronosphere.fluids;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Platform-specific fluid factory using Architectury's @ExpectPlatform pattern.
 *
 * NeoForge implementation provides FluidType support.
 * Fabric implementation uses standard FlowingFluid.
 */
public class FluidFactory {
    /**
     * Create a Decorative Water Source fluid instance.
     *
     * @return Platform-specific Decorative Water Source fluid instance
     */
    @ExpectPlatform
    public static FlowingFluid createDecorativeWaterSource() {
        throw new AssertionError("@ExpectPlatform method not replaced");
    }

    /**
     * Create a Decorative Water Flowing fluid instance.
     *
     * @return Platform-specific Decorative Water Flowing fluid instance
     */
    @ExpectPlatform
    public static FlowingFluid createDecorativeWaterFlowing() {
        throw new AssertionError("@ExpectPlatform method not replaced");
    }
}
