package com.chronodawn.worldgen.features;

import com.chronodawn.registry.ModBlocks;
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
 * Ancient Time Wood Tree Feature - Defines tree generation for Ancient Time Wood trees.
 *
 * This tree generates in the ChronoDawn dimension as a wide-canopy variant.
 *
 * Tree Characteristics:
 * - Trunk: 5-7 blocks tall
 * - Trunk Material: Ancient Time Wood Log
 * - Foliage: Blob-shaped canopy (3 block radius, wider than standard)
 * - Foliage Material: Ancient Time Wood Leaves
 *
 * Generation:
 * - Biome: chronodawn_plains (rare variant)
 * - Placement: Defined in placed_feature/ancient_time_wood_tree.json
 */
public class AncientTimeWoodTreeFeature {

    /**
     * Resource key for the configured feature.
     */
    public static final ResourceKey<ConfiguredFeature<?, ?>> ANCIENT_TIME_WOOD_TREE =
        ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath("chronodawn", "ancient_time_wood_tree")
        );

    /**
     * Bootstrap method for data generation.
     * This is called during data generation to register the configured feature.
     *
     * @param context Bootstrap context for feature registration
     */
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Register the Ancient Time Wood tree configured feature
        FeatureUtils.register(
            context,
            ANCIENT_TIME_WOOD_TREE,
            Feature.TREE,
            createTreeConfiguration()
        );
    }

    /**
     * Creates the tree configuration for Ancient Time Wood trees.
     *
     * Configuration:
     * - Base height: 5 blocks
     * - Height variation: +0-2 blocks (total 5-7 blocks)
     * - Trunk: Straight trunk placer
     * - Foliage: Blob-shaped, radius 3, offset 0, height 4 (wider canopy)
     * - Foliage height: 2 layers (base + 1)
     *
     * @return Tree configuration for Ancient Time Wood trees
     */
    private static TreeConfiguration createTreeConfiguration() {
        return new TreeConfiguration.TreeConfigurationBuilder(
            // Trunk block provider: Ancient Time Wood Log
            BlockStateProvider.simple(ModBlocks.ANCIENT_TIME_WOOD_LOG.get()),

            // Trunk placer: straight trunk, 5 blocks base height, 0-2 random height
            new StraightTrunkPlacer(
                5,  // base height
                2,  // height rand A (first random addition)
                0   // height rand B (second random addition)
            ),

            // Foliage block provider: Ancient Time Wood Leaves
            BlockStateProvider.simple(ModBlocks.ANCIENT_TIME_WOOD_LEAVES.get()),

            // Foliage placer: blob-shaped canopy (wider radius for ancient trees)
            new BlobFoliagePlacer(
                ConstantInt.of(3),  // radius (wider than standard Time Wood)
                ConstantInt.of(0),  // offset (vertical offset from top of trunk)
                4                    // height (foliage vertical height, taller for wide canopy)
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
    private AncientTimeWoodTreeFeature() {
        throw new UnsupportedOperationException("Utility class");
    }
}
