package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneSlab extends SlabBlock {
    public DeepslateTemporalStoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties();
    }
}
