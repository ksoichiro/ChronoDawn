package com.chronodawn.blocks;

import com.chronodawn.worldgen.features.FruitOfTimeTreeFeature;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.Optional;

/**
 * Time Wood Sapling block.
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
    private static final TreeGrower TIME_WOOD_TREE_GROWER = new TreeGrower(
        "time_wood",
        Optional.empty(), // mega tree feature
        Optional.of(FruitOfTimeTreeFeature.FRUIT_OF_TIME_TREE),
        Optional.empty()  // secondary mega tree feature
    );

    public TimeWoodSapling(BlockBehaviour.Properties properties) {
        super(TIME_WOOD_TREE_GROWER, properties);
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
}

