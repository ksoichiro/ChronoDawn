package com.chronodawn.compat.v1_20_1.blocks;

import com.chronodawn.worldgen.features.FruitOfTimeTreeFeature;
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
 * Time Wood Sapling block (Minecraft 1.20.1 version).
 *
 * When planted and grown (with bone meal or naturally), grows into a Fruit of Time tree.
 * Obtained from Time Wood Leaves with a low drop chance.
 *
 * Properties:
 * - Can be planted on grass, dirt, farmland
 * - Grows into Fruit of Time tree
 * - Can be accelerated with bone meal
 * - Destroyed by water flow and pistons
 *
 * Drop Source:
 * - Time Wood Leaves (5% base chance, affected by Fortune)
 *
 * Visual:
 * - Texture: time_wood_sapling.png (small time-themed plant)
 */
public class TimeWoodSapling extends SaplingBlock {
    private static final TimeWoodTreeGrower TREE_GROWER = new TimeWoodTreeGrower();

    public TimeWoodSapling(BlockBehaviour.Properties properties) {
        super(TREE_GROWER, properties);
    }

    /**
     * Create default properties for Time Wood Sapling.
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
     * Custom TreeGrower for Time Wood tree (1.20.1 version using AbstractTreeGrower).
     */
    private static class TimeWoodTreeGrower extends AbstractTreeGrower {
        @Nullable
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(
            RandomSource random,
            boolean hasFlowers
        ) {
            return FruitOfTimeTreeFeature.FRUIT_OF_TIME_TREE;
        }
    }
}
