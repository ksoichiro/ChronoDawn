package com.chronodawn.compat.v1_20_1.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.worldgen.processors.BossRoomProtectionProcessor;
import com.chronodawn.worldgen.processors.CopyFluidLevelProcessor;
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
        DeferredRegister.create(ChronoDawn.MOD_ID, Registries.STRUCTURE_PROCESSOR);

    /**
     * Copy Fluid Level Processor Type - Converts chronodawn:decorative_water
     * to minecraft:water while preserving the fluid level property.
     */
    public static final RegistrySupplier<StructureProcessorType<CopyFluidLevelProcessor>> COPY_FLUID_LEVEL =
        STRUCTURE_PROCESSOR_TYPES.register(
            "copy_fluid_level",
            () -> () -> CopyFluidLevelProcessor.CODEC.codec()
        );

    /**
     * Boss Room Protection Processor Type - Detects Boss Room Boundary Marker blocks,
     * calculates bounding box, registers protection, and replaces markers with specified blocks.
     */
    public static final RegistrySupplier<StructureProcessorType<BossRoomProtectionProcessor>> BOSS_ROOM_PROTECTION =
        STRUCTURE_PROCESSOR_TYPES.register(
            "boss_room_protection",
            () -> () -> BossRoomProtectionProcessor.CODEC.codec()
        );

    /**
     * Initialize structure processor type registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        STRUCTURE_PROCESSOR_TYPES.register();
        ChronoDawn.LOGGER.info("Registered ModStructureProcessorTypes");
    }
}
