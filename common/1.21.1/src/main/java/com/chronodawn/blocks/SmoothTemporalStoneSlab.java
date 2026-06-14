package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SmoothTemporalStoneSlab extends SlabBlock {
    public SmoothTemporalStoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return SmoothTemporalStoneBlock.createProperties();
    }
}
