package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Architectury Registry wrapper for custom dimensions.
 *
 * This class provides dimension resource keys for the ChronoDawn dimension.
 * Dimension registration is handled via JSON datapacks in resources/data/chronodawn/dimension/.
 *
 * Reference: data-model.md (Dimension: ChronoDawn)
 */
public class ModDimensions {
    /**
     * Resource key for the ChronoDawn dimension level.
     * This is used for dimension teleportation and world access.
     */
    public static final ResourceKey<Level> CHRONOSPHERE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronodawn")
    );

    /**
     * Resource key for the ChronoDawn dimension type.
     * This defines the dimension's environmental properties (fixed time, no ceiling, etc.).
     */
    public static final ResourceKey<DimensionType> CHRONOSPHERE_DIMENSION_TYPE = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronodawn")
    );

    /**
     * Initialize dimension registry.
     * This method must be called during mod initialization.
     *
     * Note: Dimensions are registered via JSON datapacks, not programmatically.
     * This method primarily exists for logging and future extension points.
     */
    public static void register() {
        ChronoDawn.LOGGER.info("Registered ModDimensions (keys prepared, JSON datapacks will define the dimension)");
    }
}
