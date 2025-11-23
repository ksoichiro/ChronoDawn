package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import com.chronosphere.worldgen.processors.CopyFluidLevelProcessor;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

/**
 * Architectury Registry wrapper for custom structure processor types.
 *
 * Structure processors are used to transform blocks during structure generation,
 * such as replacing blocks, adding randomness, or applying special logic.
 *
 * Task: T239 [US3] Guardian Vault structure generation
 */
public class ModStructureProcessorTypes {
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES =
        DeferredRegister.create(Chronosphere.MOD_ID, Registries.STRUCTURE_PROCESSOR);

    /**
     * Copy Fluid Level Processor Type - Converts chronosphere:decorative_water
     * to minecraft:water while preserving the fluid level property.
     */
    public static final RegistrySupplier<StructureProcessorType<CopyFluidLevelProcessor>> COPY_FLUID_LEVEL =
        STRUCTURE_PROCESSOR_TYPES.register(
            "copy_fluid_level",
            () -> new StructureProcessorType<CopyFluidLevelProcessor>() {
                @Override
                public MapCodec<CopyFluidLevelProcessor> codec() {
                    return CopyFluidLevelProcessor.CODEC;
                }
            }
        );

    /**
     * Initialize structure processor type registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        STRUCTURE_PROCESSOR_TYPES.register();
        Chronosphere.LOGGER.info("Registered ModStructureProcessorTypes");
    }
}
