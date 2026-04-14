package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MossyTemporalCobblestoneSlab extends SlabBlock {
    public MossyTemporalCobblestoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return MossyTemporalCobblestoneBlock.createProperties();
    }
}
