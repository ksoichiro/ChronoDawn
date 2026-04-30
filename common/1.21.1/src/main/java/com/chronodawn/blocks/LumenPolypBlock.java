package com.chronodawn.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LumenPolypBlock extends SeaPickleBlock {
    public static final MapCodec<LumenPolypBlock> CODEC = simpleCodec(LumenPolypBlock::new);

    public LumenPolypBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<SeaPickleBlock> codec() {
        return (MapCodec<SeaPickleBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .lightLevel(state -> {
                int pickles = state.getValue(SeaPickleBlock.PICKLES);
                return state.getValue(SeaPickleBlock.WATERLOGGED) ? 3 + 3 * pickles : 0;
            })
            .sound(SoundType.SLIME_BLOCK)
            .noOcclusion()
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }
}
