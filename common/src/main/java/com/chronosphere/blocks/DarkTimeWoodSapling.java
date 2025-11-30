package com.chronosphere.blocks;

import com.chronosphere.worldgen.features.DarkTimeWoodTreeFeature;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Optional;

/**
 * Dark Time Wood Sapling block.
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
    private static final TreeGrower DARK_TIME_WOOD_TREE_GROWER = new TreeGrower(
        "dark_time_wood",
        Optional.empty(), // mega tree feature
        Optional.of(DarkTimeWoodTreeFeature.DARK_TIME_WOOD_TREE),
        Optional.empty()  // secondary mega tree feature
    );

    public DarkTimeWoodSapling(BlockBehaviour.Properties properties) {
        super(DARK_TIME_WOOD_TREE_GROWER, properties);
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
}
