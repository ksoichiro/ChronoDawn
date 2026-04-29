package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

// MapCodec/codec() is 1.21+ only — not used in 1.20.1
public class TemporalKelpBlock extends KelpBlock {
    public TemporalKelpBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected Block getBodyBlock() {
        return ModBlocks.TEMPORAL_KELP_PLANT.get();
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }
}
