package com.chronodawn.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalLanternBlock extends LanternBlock {
    public static final MapCodec<TemporalLanternBlock> CODEC = simpleCodec(TemporalLanternBlock::new);

    public TemporalLanternBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<LanternBlock> codec() {
        return (MapCodec<LanternBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .instabreak()
            .sound(SoundType.LANTERN)
            .lightLevel(state -> 13)
            .noOcclusion();
    }
}
