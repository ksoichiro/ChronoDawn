package com.chronosphere.worldgen.features;

import com.chronosphere.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

/**
 * Dark Time Wood Tree Feature - Defines tree generation for Dark Time Wood trees.
 *
 * This tree generates in the Chronosphere dimension as a tall variant.
 *
 * Tree Characteristics:
 * - Trunk: 6-8 blocks tall (taller than standard Time Wood)
 * - Trunk Material: Dark Time Wood Log
 * - Foliage: Blob-shaped canopy (2 block radius)
 * - Foliage Material: Dark Time Wood Leaves
 *
 * Generation:
 * - Biome: chronosphere_plains (rare variant)
 * - Placement: Defined in placed_feature/dark_time_wood_tree.json
 */
public class DarkTimeWoodTreeFeature {

    /**
     * Resource key for the configured feature.
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> DARK_TIME_WOOD_TREE =
        ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath("chronosphere", "dark_time_wood_tree")
        );

    /**
     * Bootstrap method for data generation.
     * This is called during data generation to register the configured feature.
     *
     * @param context Bootstrap context for feature registration
     */
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Register the Dark Time Wood tree configured feature
        FeatureUtils.register(
            context,
            DARK_TIME_WOOD_TREE,
            Feature.TREE,
            createTreeConfiguration()
        );
    }

    /**
     * Creates the tree configuration for Dark Time Wood trees.
     *
     * Configuration:
     * - Base height: 6 blocks
     * - Height variation: +0-2 blocks (total 6-8 blocks)
     * - Trunk: Straight trunk placer
     * - Foliage: Blob-shaped, radius 2, offset 0, height 3
     * - Foliage height: 2 layers (base + 1)
     *
     * @return Tree configuration for Dark Time Wood trees
     */
    private static TreeConfiguration createTreeConfiguration() {
        return new TreeConfiguration.TreeConfigurationBuilder(
            // Trunk block provider: Dark Time Wood Log
            BlockStateProvider.simple(ModBlocks.DARK_TIME_WOOD_LOG.get()),

            // Trunk placer: straight trunk, 6 blocks base height, 0-2 random height
            new StraightTrunkPlacer(
                6,  // base height (taller than normal Time Wood)
                2,  // height rand A (first random addition)
                0   // height rand B (second random addition)
            ),

            // Foliage block provider: Dark Time Wood Leaves
            BlockStateProvider.simple(ModBlocks.DARK_TIME_WOOD_LEAVES.get()),

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
    private DarkTimeWoodTreeFeature() {
        throw new UnsupportedOperationException("Utility class");
    }
}
