package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Temporal Farmland - Farmland variant that matches Temporal Dirt color scheme.
 *
 * Created when Temporal Dirt or Temporal Grass is tilled with a hoe.
 * Reverts to Temporal Dirt (not vanilla dirt) when trampled or when a solid
 * block is placed on top.
 */
public class TemporalFarmlandBlock extends FarmBlock {
    @SuppressWarnings("unchecked")
    public static final MapCodec<FarmBlock> CODEC = (MapCodec<FarmBlock>)(MapCodec<?>) simpleCodec(TemporalFarmlandBlock::new);

    public TemporalFarmlandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FarmBlock> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.FARMLAND);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP && !state.canSurvive(level, pos)) {
            return ModBlocks.TEMPORAL_DIRT.get().defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            turnToTemporalDirt(null, state, level, pos);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide) {
            turnToTemporalDirt(entity, state, level, pos);
        }
        entity.causeFallDamage(fallDistance, 1.0F, level.damageSources().fall());
    }

    private static void turnToTemporalDirt(Entity entity, BlockState state, Level level, BlockPos pos) {
        BlockState blockstate = Block.pushEntitiesUp(state, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, blockstate);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, state));
    }
}
