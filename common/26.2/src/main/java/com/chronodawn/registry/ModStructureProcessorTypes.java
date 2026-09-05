package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.worldgen.processors.BossRoomProtectionProcessor;
import com.chronodawn.worldgen.processors.CopyFluidLevelProcessor;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

/**
 * Architectury Registry wrapper for custom structure processor types.
 *
 * Structure processors are used to transform blocks during structure generation,
 * such as replacing blocks, adding randomness, or applying special logic.
 *
 * Task: T239 [US3] Guardian Vault structure generation
 *
 * 26.2: StructureProcessorType<P> wrapper class removed; Registries.STRUCTURE_PROCESSOR
 * is now a Registry<MapCodec<? extends StructureProcessor>> directly, so each processor's
 * own MapCodec is registered as the entry instead of a wrapper object.
 */
public class ModStructureProcessorTypes {
    public static final DeferredRegister<MapCodec<? extends StructureProcessor>> STRUCTURE_PROCESSOR_TYPES =
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.STRUCTURE_PROCESSOR);

    /**
     * Copy Fluid Level Processor Type - Converts chronodawn:decorative_water
     * to minecraft:water while preserving the fluid level property.
     */
    public static final RegistrySupplier<MapCodec<? extends StructureProcessor>> COPY_FLUID_LEVEL =
        STRUCTURE_PROCESSOR_TYPES.register(
            "copy_fluid_level",
            () -> CopyFluidLevelProcessor.CODEC
        );

    /**
     * Boss Room Protection Processor Type - Detects Boss Room Boundary Marker blocks,
     * calculates bounding box, registers protection, and replaces markers with specified blocks.
     */
    public static final RegistrySupplier<MapCodec<? extends StructureProcessor>> BOSS_ROOM_PROTECTION =
        STRUCTURE_PROCESSOR_TYPES.register(
            "boss_room_protection",
            () -> BossRoomProtectionProcessor.CODEC
        );

    /**
     * Initialize structure processor type registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        STRUCTURE_PROCESSOR_TYPES.register();
        ChronoDawn.LOGGER.debug("Registered ModStructureProcessorTypes");
    }
}
