package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Timeless Mushroom - Edible mushroom that grows in darkness.
 *
 * Growth Properties:
 * - No growth stages (instant placement like vanilla mushrooms)
 * - Grows in low light (light level 12 or less)
 * - Can spread to nearby blocks in darkness
 * - Slow natural spread rate
 *
 * Placement:
 * - Can be placed on dirt, grass, mycelium, podzol, and temporal moss
 * - Requires low light level to be placed and to spread (an existing mushroom
 *   is not broken when the light later rises)
 *
 * Drops:
 * - Breaks instantly and drops 1x Timeless Mushroom item
 * - Cannot grow into huge mushroom (no bonemeal growth)
 *
 * Visual Theme:
 * - Silver/white color with faint glow
 * - Distinct from Unstable Fungus (purple/blue)
 * - "Timeless" theme = pale, ethereal, ghost-like
 *
 * Reference: WORK_NOTES.md (Crop 3: Timeless Mushroom)
 * Task: T212 [US1] Create Timeless Mushroom block
 */
public class TimelessMushroomBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<TimelessMushroomBlock> CODEC = simpleCodec(TimelessMushroomBlock::new);
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

    public TimelessMushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<BushBlock> codec() {
        // In 1.21.5, codec() returns MapCodec<BushBlock> not MapCodec<? extends BushBlock>
        return (MapCodec<BushBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Can be placed on natural ground, including temporal moss in underground grottos.
        return state.is(BlockTags.DIRT) ||
               state.is(Blocks.MYCELIUM) ||
               state.is(Blocks.PODZOL) ||
               state.is(Blocks.MOSS_BLOCK) ||
               state.is(ModBlocks.TEMPORAL_MOSS.get());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        // Check if block below is valid
        if (!this.mayPlaceOn(belowState, level, belowPos)) {
            return false;
        }

        // Check if placement position is not a log block
        if (level.getBlockState(pos).is(BlockTags.LOGS)) {
            return false;
        }

        // Check if block below is not a log block (prevents tree root replacement)
        if (belowState.is(BlockTags.LOGS)) {
            return false;
        }

        // Check if block above is not a log block (prevents placement under tree trunks)
        BlockPos abovePos = pos.above();
        if (level.getBlockState(abovePos).is(BlockTags.LOGS)) {
            return false;
        }

        // Light is deliberately not checked here. Worldgen places mushrooms before
        // light is calculated, so some generate in bright spots; a light check here
        // would break them on the next neighbor update (e.g. bone meal growing grass).
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!isDarkEnough(context.getLevel(), context.getClickedPos())) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    /**
     * Light requirement for placing and spreading (must be 12 or less).
     */
    private static boolean isDarkEnough(LevelReader level, BlockPos pos) {
        return level.getRawBrightness(pos, 0) <= 12;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Attempt to spread to nearby blocks (slow rate)
        if (random.nextInt(25) == 0) {
            int nearbyMushrooms = 0;
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            // Count nearby mushrooms (5 block radius)
            for (int x = -4; x <= 4; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -4; z <= 4; z++) {
                        mutablePos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                        if (level.getBlockState(mutablePos).is(this)) {
                            nearbyMushrooms++;
                            if (nearbyMushrooms > 4) {
                                return; // Too crowded, stop spreading
                            }
                        }
                    }
                }
            }

            // Try to spread to random nearby position
            BlockPos targetPos = pos.offset(
                random.nextInt(3) - 1,
                random.nextInt(2) - random.nextInt(2),
                random.nextInt(3) - 1
            );

            // Check if target position is valid for mushroom placement
            if (level.getBlockState(targetPos).isAir() &&
                this.canSurvive(this.defaultBlockState(), level, targetPos) &&
                isDarkEnough(level, targetPos)) {
                level.setBlock(targetPos, this.defaultBlockState(), 2);
            }
        }
    }

    // ==================== Bonemealable Interface ====================
    // Note: We intentionally do NOT allow huge mushroom growth to keep it distinct from vanilla

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return false; // Cannot be bonemealed
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // No-op: bonemeal does nothing
    }
}
