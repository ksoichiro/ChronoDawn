package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolishedDeepslateTemporalStoneWall extends WallBlock {
    public PolishedDeepslateTemporalStoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return PolishedDeepslateTemporalStoneBlock.createProperties();
    }
}
