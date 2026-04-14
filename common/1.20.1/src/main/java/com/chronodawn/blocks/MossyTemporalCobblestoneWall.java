package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MossyTemporalCobblestoneWall extends WallBlock {
    public MossyTemporalCobblestoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return MossyTemporalCobblestoneBlock.createProperties();
    }
}
