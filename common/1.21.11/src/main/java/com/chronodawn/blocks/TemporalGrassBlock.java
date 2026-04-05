package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

/**
 * Temporal Grass Block - Custom grass block for the ChronoDawn biome.
 *
 * Extends SpreadingSnowyDirtBlock (instead of GrassBlock) to avoid the sealed
 * codec issue in GrassBlock. Spreads between TemporalDirt blocks and reverts
 * to TemporalDirt (not vanilla dirt) when light is blocked.
 */
public class TemporalGrassBlock extends SpreadingSnowyDirtBlock {
    public static final MapCodec<TemporalGrassBlock> CODEC = simpleCodec(TemporalGrassBlock::new);

    public TemporalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends SpreadingSnowyDirtBlock> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_grass_block")));
    }

    /**
     * Returns true if the grass block can remain as grass (light is not blocked above).
     * Mirrors vanilla SpreadingSnowyDirtBlock.canBeGrass logic.
     */
    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (aboveState.getFluidState().getAmount() == 8) {
            return false;
        } else {
            int lightBlock = LightEngine.getLightBlockInto(state, aboveState, Direction.UP, aboveState.getLightBlock());
            return lightBlock < 15;
        }
    }

    /**
     * Returns true if grass can propagate to the given position.
     * Same as canBeGrass but also checks that there is no water above.
     */
    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return canBeGrass(state, level, pos) && !level.getFluidState(above).is(FluidTags.WATER);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState());
        } else {
            if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
                BlockState grassState = this.defaultBlockState();
                for (int i = 0; i < 4; i++) {
                    BlockPos target = pos.offset(
                            random.nextInt(3) - 1,
                            random.nextInt(5) - 3,
                            random.nextInt(3) - 1);
                    // Only spread to temporal dirt blocks (not vanilla dirt)
                    if (level.getBlockState(target).is(ModBlocks.TEMPORAL_DIRT.get())
                            && canPropagate(grassState, level, target)) {
                        level.setBlockAndUpdate(target, grassState.setValue(SNOWY,
                                isSnowySetting(level.getBlockState(target.above()))));
                    }
                }
            }
        }
    }
}
