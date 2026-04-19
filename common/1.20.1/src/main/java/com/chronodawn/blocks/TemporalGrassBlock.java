package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Temporal Grass Block - Custom grass block for the ChronoDawn biome.
 *
 * Extends SpreadingSnowyDirtBlock to inherit snowy dirt spreading behaviour.
 * Spreads between TemporalDirt blocks and reverts to TemporalDirt (not vanilla
 * dirt) when light is blocked.
 */
public class TemporalGrassBlock extends SpreadingSnowyDirtBlock {
    public TemporalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.GRASS_BLOCK);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof HoeItem) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlockAndUpdate(pos, ModBlocks.TEMPORAL_FARMLAND.get().defaultBlockState());
                player.getItemInHand(hand).hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    /**
     * Returns true if the grass block can remain as grass (light is not blocked above).
     */
    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (aboveState.getFluidState().getAmount() == 8) {
            return false;
        } else {
            // Use sky light level check as a proxy for light blocking
            return level.getRawBrightness(above, 0) >= 1;
        }
    }

    /**
     * Returns true if grass can propagate to the given position.
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
                                level.getBlockState(target.above()).is(Blocks.SNOW)));
                    }
                }
            }
        }
    }
}
