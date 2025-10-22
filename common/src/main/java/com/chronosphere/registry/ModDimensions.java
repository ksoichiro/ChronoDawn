package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Architectury Registry wrapper for custom dimensions.
 *
 * This class provides dimension resource keys for the Chronosphere dimension.
 * Dimension registration is handled via JSON datapacks in resources/data/chronosphere/dimension/.
 *
 * Reference: data-model.md (Dimension: Chronosphere)
 */
public class ModDimensions {
    /**
     * Resource key for the Chronosphere dimension level.
     * This is used for dimension teleportation and world access.
     */
    public static final ResourceKey<Level> CHRONOSPHERE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "chronosphere")
    );

    /**
     * Resource key for the Chronosphere dimension type.
     * This defines the dimension's environmental properties (fixed time, no ceiling, etc.).
     */
    public static final ResourceKey<DimensionType> CHRONOSPHERE_DIMENSION_TYPE = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, "chronosphere")
    );

    /**
     * Initialize dimension registry.
     * This method must be called during mod initialization.
     *
     * Note: Dimensions are registered via JSON datapacks, not programmatically.
     * This method primarily exists for logging and future extension points.
     */
    public static void register() {
        Chronosphere.LOGGER.info("Registered ModDimensions (keys prepared, JSON datapacks will define the dimension)");
    }
}
