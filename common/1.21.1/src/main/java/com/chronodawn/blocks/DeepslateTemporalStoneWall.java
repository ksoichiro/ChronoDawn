package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneWall extends WallBlock {
    public DeepslateTemporalStoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties();
    }
}
