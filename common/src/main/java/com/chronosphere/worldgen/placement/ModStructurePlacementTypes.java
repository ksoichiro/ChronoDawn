package com.chronosphere.worldgen.placement;

import com.chronosphere.Chronosphere;
import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

/**
 * Architectury Registry wrapper for custom structure placement types.
 *
 * Structure placement types define how structures are distributed in the world,
 * controlling spacing, frequency, and placement conditions.
 *
 * Task: T279 [US3] Create ModStructurePlacementTypes.java registry class
 */
public class ModStructurePlacementTypes {
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPES =
        DeferredRegister.create(Chronosphere.MOD_ID, Registries.STRUCTURE_PLACEMENT);

    /**
     * Guaranteed Radius Placement Type - Ensures structures appear within a specified
     * radius from world origin while maintaining minimum spacing between structures.
     *
     * JSON Usage:
     * ```json
     * {
     *   "type": "chronosphere:guaranteed_radius",
     *   "salt": 12345,
     *   "radius_chunks": 50,
     *   "min_distance": 15
     * }
     * ```
     */
    public static final RegistrySupplier<StructurePlacementType<GuaranteedRadiusStructurePlacement>> GUARANTEED_RADIUS =
        STRUCTURE_PLACEMENT_TYPES.register(
            "guaranteed_radius",
            () -> new StructurePlacementType<GuaranteedRadiusStructurePlacement>() {
                @Override
                public MapCodec<GuaranteedRadiusStructurePlacement> codec() {
                    return GuaranteedRadiusStructurePlacement.CODEC;
                }
            }
        );

    /**
     * Initialize structure placement type registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        STRUCTURE_PLACEMENT_TYPES.register();
        Chronosphere.LOGGER.info("Registered ModStructurePlacementTypes");
    }
}
