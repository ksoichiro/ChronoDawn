package com.chronodawn.compat.v1_20_1.blocks;

import com.chronodawn.worldgen.features.DarkTimeWoodTreeFeature;
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
 * Dark Time Wood Sapling block (Minecraft 1.20.1 version).
 *
 * When planted and grown (with bone meal or naturally), grows into a Dark Time Wood tree.
 * Obtained from Dark Time Wood Leaves with a low drop chance.
 *
 * Properties:
 * - Can be planted on grass, dirt, farmland
 * - Grows into Dark Time Wood tree (tall variant)
 * - Can be accelerated with bone meal
 * - Destroyed by water flow and pistons
 *
 * Drop Source:
 * - Dark Time Wood Leaves (5% base chance, affected by Fortune)
 *
 * Visual:
 * - Texture: dark_time_wood_sapling.png (small dark time-themed plant)
 */
public class DarkTimeWoodSapling extends SaplingBlock {
    private static final DarkTimeWoodTreeGrower TREE_GROWER = new DarkTimeWoodTreeGrower();

    public DarkTimeWoodSapling(BlockBehaviour.Properties properties) {
        super(TREE_GROWER, properties);
    }

    /**
     * Create default properties for Dark Time Wood Sapling.
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
     * Custom TreeGrower for Dark Time Wood tree (1.20.1 version using AbstractTreeGrower).
     */
    private static class DarkTimeWoodTreeGrower extends AbstractTreeGrower {
        @Nullable
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(
            RandomSource random,
            boolean hasFlowers
        ) {
            return DarkTimeWoodTreeFeature.DARK_TIME_WOOD_TREE;
        }
    }
}
