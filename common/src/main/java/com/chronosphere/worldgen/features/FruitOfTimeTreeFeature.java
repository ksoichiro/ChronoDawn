package com.chronosphere.worldgen.features;

import com.chronosphere.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

/**
 * Fruit of Time Tree Feature - Defines tree generation for Fruit of Time trees.
 *
 * This tree generates in the Chronosphere dimension and produces Fruit of Time items.
 *
 * Tree Characteristics:
 * - Trunk: 4-6 blocks tall (birch-like)
 * - Trunk Material: Time Wood Log (custom block)
 * - Foliage: Blob-shaped canopy (2-3 block radius)
 * - Foliage Material: Time Wood Leaves (custom block)
 * - Special Feature: Will later be decorated with Fruit of Time blocks/items
 *
 * Generation:
 * - Biome: chronosphere_plains
 * - Placement: Defined in placed_feature/fruit_of_time_tree.json
 *
 * Future Enhancements:
 * - Tree decorator to place Fruit of Time blocks on leaves
 * - Custom particle effects for time-themed atmosphere
 *
 * Reference: spec.md (User Story 1, FR-007)
 * Tasks: T079 [US1] Create Fruit of Time block feature, T080r [US1] Update to use Time Wood blocks
 */
public class FruitOfTimeTreeFeature {

    /**
     * Resource key for the configured feature.
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> FRUIT_OF_TIME_TREE =
        ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath("chronosphere", "fruit_of_time_tree")
        );

    /**
     * Bootstrap method for data generation.
     * This is called during data generation to register the configured feature.
     *
     * @param context Bootstrap context for feature registration
     */
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Register the Fruit of Time tree configured feature
        FeatureUtils.register(
            context,
            FRUIT_OF_TIME_TREE,
            Feature.TREE,
            createTreeConfiguration()
        );
    }

    /**
     * Creates the tree configuration for Fruit of Time trees.
     *
     * Configuration:
     * - Base height: 4 blocks
     * - Height variation: +0-2 blocks (total 4-6 blocks)
     * - Trunk: Straight trunk placer
     * - Foliage: Blob-shaped, radius 2, offset 0, height 3
     * - Foliage height: 2 layers (base + 1)
     *
     * @return Tree configuration for Fruit of Time trees
     */
    private static TreeConfiguration createTreeConfiguration() {
        return new TreeConfiguration.TreeConfigurationBuilder(
            // Trunk block provider: Time Wood Log (custom block)
            BlockStateProvider.simple(ModBlocks.TIME_WOOD_LOG.get()),

            // Trunk placer: straight trunk, 4 blocks base height, 0-2 random height
            new StraightTrunkPlacer(
                4,  // base height
                2,  // height rand A (first random addition)
                0   // height rand B (second random addition)
            ),

            // Foliage block provider: Time Wood Leaves (custom block)
            BlockStateProvider.simple(ModBlocks.TIME_WOOD_LEAVES.get()),

            // Foliage placer: blob-shaped canopy
            new BlobFoliagePlacer(
                ConstantInt.of(2),  // radius
                ConstantInt.of(0),  // offset (vertical offset from top of trunk)
                3                    // height (foliage vertical height)
            ),

            // Feature size: 2 layers (controls dirt placement under tree)
            new TwoLayersFeatureSize(
                1,  // limit (minimum height for upper layer)
                0,  // lower size (blocks in lower layer)
                1   // upper limit (minimum clearance needed above)
            )
        )
        .ignoreVines()  // Don't generate vines
        .build();
    }

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private FruitOfTimeTreeFeature() {
        throw new UnsupportedOperationException("Utility class");
    }
}
