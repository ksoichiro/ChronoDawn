package com.chronosphere.blocks;

import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Chrono Melon Stem - Stem block that grows Chrono Melons.
 *
 * Growth Stages:
 * - 8 stages (0-7) like vanilla melon/pumpkin stems
 * - Stage 7 is fully mature and can produce melons
 *
 * Properties:
 * - Must be planted on farmland
 * - Requires light level 9+ to grow
 * - Random tick growth like vanilla stems
 * - When mature, attempts to place melon on adjacent blocks
 *
 * Drops:
 * - Does not drop anything when broken (seeds come from melon slices)
 *
 * Visual Theme:
 * - Time-themed stem with golden/amber tint
 * - Growth stages show progressively thicker stem
 *
 * Note:
 * - Simplified implementation using CropBlock as base
 * - Melon placement logic in randomTick
 *
 * Reference: WORK_NOTES.md (Crop 2: Chrono Melon)
 * Task: T212 [US1] Create Chrono Melon Stem block
 */
public class ChronoMelonStemBlock extends CropBlock {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

    // Collision shapes for each growth stage (same as vanilla stems)
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D),
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 4.0D, 9.0D),
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D),
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 8.0D, 9.0D),
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D),
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 12.0D, 9.0D),
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D),
            Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)
    };

    public ChronoMelonStemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), 0));
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.CHRONO_MELON_SEEDS.get();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[this.getAge(state)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int currentAge = this.getAge(state);

        if (currentAge < this.getMaxAge()) {
            // Stem growth (with light check)
            if (level.getRawBrightness(pos, 0) >= 9) {
                float growthSpeed = getGrowthSpeed(this, level, pos);
                if (random.nextInt((int) (25.0F / growthSpeed) + 1) == 0) {
                    level.setBlock(pos, this.getStateForAge(currentAge + 1), 2);
                }
            }
        } else if (currentAge >= this.getMaxAge()) {
            // Check if melon already exists in any adjacent direction
            boolean hasAdjacentMelon = false;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos melonPos = pos.relative(direction);
                if (level.getBlockState(melonPos).is(ModBlocks.CHRONO_MELON.get())) {
                    hasAdjacentMelon = true;
                    break;
                }
            }

            // Only try to place melon if none exists adjacent
            if (!hasAdjacentMelon) {
                // Try to place melon on the first available adjacent space
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos melonPos = pos.relative(direction);
                    BlockState blockAtPos = level.getBlockState(melonPos);

                    // Only place melon if block is air or replaceable
                    if (blockAtPos.isAir() || blockAtPos.canBeReplaced()) {
                        level.setBlockAndUpdate(melonPos, ModBlocks.CHRONO_MELON.get().defaultBlockState());
                        break; // Only place one melon
                    }
                }
            }
        }
    }
}
