package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneBricksWall extends WallBlock {
    public TemporalStoneBricksWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBricksBlock.createProperties();
    }
}
