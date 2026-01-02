package com.chronodawn.worldgen.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Ancient Time Wood Tree Feature - Defines tree generation for Ancient Time Wood trees.
 *
 * This tree generates in the ChronoDawn dimension as a variant of Time Wood trees.
 *
 * Tree Characteristics:
 * - Trunk: Taller than regular Time Wood (5-8 blocks)
 * - Trunk Material: Ancient Time Wood Log (custom block)
 * - Foliage: Wide canopy (blob-shaped, larger radius)
 * - Foliage Material: Ancient Time Wood Leaves (custom block)
 * - Appearance: Weathered, ancient-looking variant
 *
 * Generation:
 * - Biome: chronodawn_ancient_forest
 * - Placement: Defined in resources/data/chronodawn/worldgen/configured_feature/ancient_time_wood_tree.json
 * - Tree configuration is data-driven (JSON), not code-based
 *
 * Note: This class only holds the ResourceKey for referencing the configured feature.
 * The actual tree configuration (trunk height, foliage shape, decorators) is defined in JSON.
 *
 * Reference: spec.md (User Story 1, FR-007)
 * Tasks: T080r [US1] Create Ancient Time Wood tree variant
 */
public class AncientTimeWoodTreeFeature {

    /**
     * Resource key for the configured feature.
     * Used by TreeGrower/AbstractTreeGrower to reference the tree configuration defined in JSON.
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> ANCIENT_TIME_WOOD_TREE =
        ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath("chronodawn", "ancient_time_wood_tree")
        );

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static constants.
     */
    private AncientTimeWoodTreeFeature() {
        throw new UnsupportedOperationException("Utility class");
    }
}
