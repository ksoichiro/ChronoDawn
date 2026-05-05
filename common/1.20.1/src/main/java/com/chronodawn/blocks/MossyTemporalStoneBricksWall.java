package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MossyTemporalStoneBricksWall extends WallBlock {
    public MossyTemporalStoneBricksWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return MossyTemporalStoneBricksBlock.createProperties();
    }
}
