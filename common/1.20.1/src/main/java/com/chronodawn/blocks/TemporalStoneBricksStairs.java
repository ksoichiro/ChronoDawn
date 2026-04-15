package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneBricksStairs extends StairBlock {
    public TemporalStoneBricksStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_STONE_BRICKS.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBricksBlock.createProperties();
    }
}
