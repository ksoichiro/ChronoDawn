package com.chronodawn.blocks;

import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

// MapCodec/codec() is 1.21+ only — not used in 1.20.1
public class LumenPolypBlock extends SeaPickleBlock {
    public LumenPolypBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Vanilla SeaPickleBlock only spreads on top of coral blocks and does nothing elsewhere
    // while still consuming the bone meal, so grow the count in place on any supporting block.
    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level,
            net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, boolean isClient) {
        return state.getValue(WATERLOGGED) && state.getValue(PICKLES) < 4;
    }

    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level,
            net.minecraft.util.RandomSource random, net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(net.minecraft.server.level.ServerLevel level,
            net.minecraft.util.RandomSource random, net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state) {
        level.setBlock(pos, state.setValue(PICKLES, state.getValue(PICKLES) + 1), 2);
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
