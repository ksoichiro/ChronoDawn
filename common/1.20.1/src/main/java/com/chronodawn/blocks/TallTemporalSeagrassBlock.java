package com.chronodawn.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

// MapCodec/codec() is 1.21+ only — not used in 1.20.1
public class TallTemporalSeagrassBlock extends TallSeagrassBlock {
    public TallTemporalSeagrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }
}
