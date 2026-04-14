package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolishedDeepslateTemporalStoneSlab extends SlabBlock {
    public PolishedDeepslateTemporalStoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return PolishedDeepslateTemporalStoneBlock.createProperties();
    }
}
