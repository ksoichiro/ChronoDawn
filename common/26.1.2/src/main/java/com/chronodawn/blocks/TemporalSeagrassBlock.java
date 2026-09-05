package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSeagrassBlock extends SeagrassBlock {
    public static final MapCodec<TemporalSeagrassBlock> CODEC = simpleCodec(TemporalSeagrassBlock::new);

    public TemporalSeagrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<SeagrassBlock> codec() {
        return (MapCodec<SeagrassBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockState aboveState = level.getBlockState(pos.above());
        if (aboveState.is(Blocks.WATER) && aboveState.getFluidState().getAmount() == 8) {
            DoublePlantBlock.placeAt(level, ModBlocks.TALL_TEMPORAL_SEAGRASS.get().defaultBlockState(), pos, 2);
        }
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollision()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
