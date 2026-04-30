package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TemporalDeadBushBlock extends BushBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public TemporalDeadBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(net.minecraft.world.level.block.Blocks.SAND)
            || state.is(net.minecraft.world.level.block.Blocks.RED_SAND)
            || state.is(com.chronodawn.registry.ModBlocks.TEMPORAL_SAND.get())
            || state.is(com.chronodawn.registry.ModBlocks.TEMPORAL_DIRT.get())
            || state.is(com.chronodawn.registry.ModBlocks.COARSE_TEMPORAL_DIRT.get())
            || state.is(com.chronodawn.registry.ModBlocks.PARCHED_TEMPORAL_DIRT.get())
            || state.is(com.chronodawn.registry.ModBlocks.TEMPORAL_GRAVEL.get());
    }
}
