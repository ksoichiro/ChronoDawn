package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneBricksSlab extends SlabBlock {
    public TemporalStoneBricksSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBricksBlock.createProperties();
    }
}
