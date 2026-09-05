package com.chronodawn.forge.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Forge-specific Fluid Type registry.
 * FluidType is required in Forge for all custom fluids.
 */
public class ModFluidTypes {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
        ForgeRegistries.Keys.FLUID_TYPES,
        ChronoDawn.MOD_ID
    );

    /**
     * Decorative Water FluidType - behaves exactly like vanilla water.
     * Used for decorative water features in structures.
     *
     * Note: Unlike NeoForge 1.21.2+ (which removed initializeClient() from
     * FluidType and moved client extension registration to
     * RegisterClientExtensionsEvent), Forge 1.20.1 still has
     * FluidType.initializeClient(), so client extensions are registered here
     * directly via an anonymous subclass override.
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
            .pathType(BlockPathTypes.WATER)
            .adjacentPathType(null)
        ) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return CompatResourceLocation.create("minecraft", "block/water_still");
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return CompatResourceLocation.create("minecraft", "block/water_flow");
                    }

                    @Override
                    public ResourceLocation getOverlayTexture() {
                        return CompatResourceLocation.create("minecraft", "block/water_overlay");
                    }

                    @Override
                    public int getTintColor() {
                        // Use vanilla water color
                        return 0xFF3F76E4;
                    }
                });
            }
        }
    );

    /**
     * Register FluidTypes to the Forge registry.
     */
    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        ChronoDawn.LOGGER.debug("Registered ModFluidTypes for Forge");
    }
}
