package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
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
        return BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_farmland")));
    }

    @Override
    protected BlockState updateShape(
        BlockState state,
        LevelReader level,
        ScheduledTickAccess tickAccess,
        BlockPos pos,
        Direction direction,
        BlockPos neighborPos,
        BlockState neighborState,
        RandomSource random
    ) {
        if (direction == Direction.UP && !state.canSurvive(level, pos)) {
            return ModBlocks.TEMPORAL_DIRT.get().defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (!level.isClientSide && fallDistance > 0.5) {
            turnToTemporalDirt(entity, state, level, pos);
        }
    }

    private static void turnToTemporalDirt(Entity entity, BlockState state, Level level, BlockPos pos) {
        BlockState blockstate = Block.pushEntitiesUp(state, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState(), level, pos);
        level.setBlockAndUpdate(pos, blockstate);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, state));
    }
}
