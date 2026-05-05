package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MossyTemporalStoneBricksStairs extends StairBlock {
    public MossyTemporalStoneBricksStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.MOSSY_TEMPORAL_STONE_BRICKS.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return MossyTemporalStoneBricksBlock.createProperties();
    }
}
