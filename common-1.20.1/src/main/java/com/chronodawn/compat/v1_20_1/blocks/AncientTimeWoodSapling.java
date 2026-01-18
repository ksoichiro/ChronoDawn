package com.chronodawn.blocks;

import com.chronodawn.worldgen.features.AncientTimeWoodTreeFeature;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import org.jetbrains.annotations.Nullable;

/**
 * Ancient Time Wood Sapling block (Minecraft 1.20.1 version).
 *
 * When planted and grown (with bone meal or naturally), grows into an Ancient Time Wood tree.
 * Obtained from Ancient Time Wood Leaves with a low drop chance.
 *
 * Properties:
 * - Can be planted on grass, dirt, farmland
 * - Grows into Ancient Time Wood tree (wide-canopy variant)
 * - Can be accelerated with bone meal
 * - Destroyed by water flow and pistons
 *
 * Drop Source:
 * - Ancient Time Wood Leaves (5% base chance, affected by Fortune)
 *
 * Visual:
 * - Texture: ancient_time_wood_sapling.png (small weathered time-themed plant)
 */
public class AncientTimeWoodSapling extends SaplingBlock {
    private static final AncientTimeWoodTreeGrower TREE_GROWER = new AncientTimeWoodTreeGrower();

    public AncientTimeWoodSapling(BlockBehaviour.Properties properties) {
        super(TREE_GROWER, properties);
    }

    /**
     * Create default properties for Ancient Time Wood Sapling.
     *
     * @return Block properties with sapling-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .noOcclusion()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)
                .pushReaction(PushReaction.DESTROY);
    }

    /**
     * Custom TreeGrower for Ancient Time Wood tree (1.20.1 version using AbstractTreeGrower).
     */
    private static class AncientTimeWoodTreeGrower extends AbstractTreeGrower {
        @Nullable
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(
            RandomSource random,
            boolean hasFlowers
        ) {
            return AncientTimeWoodTreeFeature.ANCIENT_TIME_WOOD_TREE;
        }
    }
}
