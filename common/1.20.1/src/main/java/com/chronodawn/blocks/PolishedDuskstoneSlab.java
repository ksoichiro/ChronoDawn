package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolishedDuskstoneSlab extends SlabBlock {
    public PolishedDuskstoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return PolishedDuskstoneBlock.createProperties();
    }
}
