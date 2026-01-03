package com.chronodawn.worldgen.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Dark Time Wood Tree Feature - Defines tree generation for Dark Time Wood trees.
 *
 * This tree generates in the ChronoDawn dimension as a darker variant of Time Wood trees.
 *
 * Tree Characteristics:
 * - Trunk: Very tall (6-10 blocks)
 * - Trunk Material: Dark Time Wood Log (custom block)
 * - Foliage: Tall, narrow canopy
 * - Foliage Material: Dark Time Wood Leaves (custom block)
 * - Appearance: Dark, ominous variant
 *
 * Generation:
 * - Biome: chronodawn_dark_forest
 * - Placement: Defined in resources/data/chronodawn/worldgen/configured_feature/dark_time_wood_tree.json
 * - Tree configuration is data-driven (JSON), not code-based
 *
 * Note: This class only holds the ResourceKey for referencing the configured feature.
 * The actual tree configuration (trunk height, foliage shape, decorators) is defined in JSON.
 *
 * Reference: spec.md (User Story 1, FR-007)
 * Tasks: T080r [US1] Create Dark Time Wood tree variant
 */
public class DarkTimeWoodTreeFeature {

    /**
     * Resource key for the configured feature.
     * Used by TreeGrower/AbstractTreeGrower to reference the tree configuration defined in JSON.
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_TIME_WOOD_TREE =
        ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            CompatResourceLocation.create("chronodawn", "dark_time_wood_tree")
        );

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static constants.
     */
    private DarkTimeWoodTreeFeature() {
        throw new UnsupportedOperationException("Utility class");
    }
}
