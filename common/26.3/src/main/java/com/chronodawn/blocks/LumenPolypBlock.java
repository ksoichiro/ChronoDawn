package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LumenPolypBlock extends SeaPickleBlock {

    public LumenPolypBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // Vanilla SeaPickleBlock only spreads on top of coral blocks and does nothing elsewhere
    // while still consuming the bone meal, so grow the count in place on any supporting block.
    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level,
            net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.block.BonemealSource source) {
        return state.getValue(WATERLOGGED) && state.getValue(PICKLES) < 4;
    }

    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level,
            net.minecraft.util.RandomSource random, net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.block.BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(net.minecraft.server.level.ServerLevel level,
            net.minecraft.util.RandomSource random, net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.block.BonemealSource source) {
        level.setBlock(pos, state.setValue(PICKLES, state.getValue(PICKLES) + 1), 2);
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .lightLevel(state -> {
                int pickles = state.getValue(SeaPickleBlock.PICKLES);
                return state.getValue(SeaPickleBlock.WATERLOGGED) ? 3 + 3 * pickles : 0;
            })
            .sound(SoundType.SLIME_BLOCK)
            .noOcclusion()
            .pushReaction(net.minecraft.world.level.material.PushReaction.POPPED)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
