package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CobbledDeepslateTemporalStoneSlab extends SlabBlock {
    public CobbledDeepslateTemporalStoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CobbledDeepslateTemporalStoneBlock.createProperties();
    }
}
