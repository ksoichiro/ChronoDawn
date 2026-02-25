package com.chronodawn.neoforge.registry;

import com.chronodawn.ChronoDawn;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * NeoForge-specific Fluid Type registry.
 * FluidType is required in NeoForge for all custom fluids.
 */
public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
        NeoForgeRegistries.FLUID_TYPES,
        ChronoDawn.MOD_ID
    );

    /**
     * Decorative Water FluidType - behaves exactly like vanilla water.
     * Used for decorative water features in structures.
     *
     * Note: Client extensions are registered via RegisterClientExtensionsEvent in ChronoDawnClientNeoForge
     * because NeoForge 1.21.2 removed the initializeClient() method from FluidType.
     */
    public static final Supplier<FluidType> DECORATIVE_WATER_TYPE = FLUID_TYPES.register(
        "decorative_water",
        () -> new FluidType(FluidType.Properties.create()
            .descriptionId("block.chronodawn.decorative_water")
            .fallDistanceModifier(0F)
            .canExtinguish(true)
            .canConvertToSource(true)
            .supportsBoating(true)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .lightLevel(0)
            .density(1000)
            .temperature(300)
            .viscosity(1000)
            .pathType(PathType.WATER)
            .adjacentPathType(null)
        )
    );

    /**
     * Register FluidTypes to the NeoForge registry.
     */
    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        ChronoDawn.LOGGER.debug("Registered ModFluidTypes for NeoForge");
    }
}
