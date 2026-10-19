package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Potted Chrono Bloom - Flower pot holding a Chrono Bloom, with the same time-driven STAGE.
 *
 * The FlowerPotBlock constructor registers the Chrono Bloom as content of the empty vanilla
 * pot, so using a Chrono Bloom on a flower pot creates this block.
 */
public class PottedChronoBloomBlock extends FlowerPotBlock {
    public static final IntegerProperty STAGE = ChronoBloomBlock.STAGE;

    public PottedChronoBloomBlock(BlockBehaviour.Properties properties) {
        super(ModBlocks.CHRONO_BLOOM.get(), properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, ChronoBloomStage.CLOSED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    /**
     * Vanilla FlowerPotBlock places the default state (CLOSED) when a flower is inserted,
     * so correct the stage right away instead of waiting for a random tick.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            ChronoBloomBlock.updateStage(state, level, pos);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ChronoBloomBlock.updateStage(state, level, pos);
    }
}
