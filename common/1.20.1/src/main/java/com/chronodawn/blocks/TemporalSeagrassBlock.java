package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

// MapCodec/codec() is 1.21+ only — not used in 1.20.1
public class TemporalSeagrassBlock extends SeagrassBlock {
    public TemporalSeagrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockState aboveState = level.getBlockState(pos.above());
        if (aboveState.is(Blocks.WATER) && aboveState.getFluidState().getAmount() == 8) {
            DoublePlantBlock.placeAt(level, ModBlocks.TALL_TEMPORAL_SEAGRASS.get().defaultBlockState(), pos, 2);
        }
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }
}
