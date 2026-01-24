package com.chronodawn.worldgen.features;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.compat.CompatResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * Fruit of Time Tree Feature - Defines tree generation for Fruit of Time trees.
 *
 * This tree generates in the ChronoDawn dimension and produces Fruit of Time items.
 *
 * Tree Characteristics:
 * - Trunk: 4-6 blocks tall (birch-like)
 * - Trunk Material: Time Wood Log (custom block)
 * - Foliage: Blob-shaped canopy (2-3 block radius)
 * - Foliage Material: Time Wood Leaves (custom block)
 * - Special Feature: Will later be decorated with Fruit of Time blocks/items
 *
 * Generation:
 * - Biome: chronodawn_plains
 * - Placement: Defined in resources/data/chronodawn/worldgen/configured_feature/fruit_of_time_tree.json
 * - Tree configuration is data-driven (JSON), not code-based
 *
 * Note: This class only holds the ResourceKey for referencing the configured feature.
 * The actual tree configuration (trunk height, foliage shape, decorators) is defined in JSON.
 *
 * Reference: spec.md (User Story 1, FR-007)
 * Tasks: T079 [US1] Create Fruit of Time block feature, T080r [US1] Update to use Time Wood blocks
 */
public class FruitOfTimeTreeFeature {

    /**
     * Resource key for the configured feature.
     * Used by TreeGrower/AbstractTreeGrower to reference the tree configuration defined in JSON.
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> FRUIT_OF_TIME_TREE =
        ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            CompatResourceLocation.create("chronodawn", "fruit_of_time_tree")
        );

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private FruitOfTimeTreeFeature() {
        throw new UnsupportedOperationException("Utility class");
    }
}
