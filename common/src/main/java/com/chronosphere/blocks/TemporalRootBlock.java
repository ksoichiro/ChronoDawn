package com.chronosphere.blocks;

import com.chronosphere.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Temporal Root - Root vegetable crop that grows in the Chronosphere dimension.
 *
 * Growth Stages:
 * - 8 stages (0-7) like vanilla carrots/potatoes
 * - Stage 7 is fully mature and ready for harvest
 *
 * Properties:
 * - Can only be planted on farmland
 * - Requires light level 9+ to grow
 * - Random tick growth like vanilla crops
 * - Drops Temporal Root items (and itself when mature)
 *
 * Drops:
 * - Stage 0-6: 1x Temporal Root (seed/replantable)
 * - Stage 7 (mature): 2-4x Temporal Roots + 0-2x Temporal Root (seeds)
 * - Fortune enchantment increases drop count
 *
 * Visual Theme:
 * - Root vegetable with time-themed appearance
 * - Growth stages show progressively larger leafy tops
 *
 * Reference: WORK_NOTES.md (Crop 1: Temporal Root)
 * Task: T212 [US1] Create Temporal Root crop block
 */
public class TemporalRootBlock extends CropBlock {
    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

    // Collision shapes for each growth stage (same as vanilla carrots)
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)
    };

    public TemporalRootBlock(Properties properties) {
        super(properties);
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
        // Temporal Root item is both food and seed (like carrots/potatoes)
        return ModItems.TEMPORAL_ROOT.get();
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
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Growth logic (same as vanilla crops)
        if (level.getRawBrightness(pos, 0) >= 9) {
            int currentAge = this.getAge(state);
            if (currentAge < this.getMaxAge()) {
                float growthSpeed = CropBlock.getGrowthSpeed(state.getBlock(), level, pos);
                if (random.nextInt((int) (25.0F / growthSpeed) + 1) == 0) {
                    level.setBlock(pos, this.getStateForAge(currentAge + 1), 2);
                }
            }
        }
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Allow placement on farmland (for player planting) or grass/dirt (for worldgen)
        return state.is(net.minecraft.world.level.block.Blocks.FARMLAND) ||
               state.is(net.minecraft.tags.BlockTags.DIRT);
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        // Check if block below is valid (farmland or dirt)
        if (!this.mayPlaceOn(belowState, level, belowPos)) {
            return false;
        }

        // Check if block below is not a log block (prevents tree root replacement)
        if (belowState.is(net.minecraft.tags.BlockTags.LOGS)) {
            return false;
        }

        // Check if block above is not a log block (prevents placement under tree trunks)
        BlockPos abovePos = pos.above();
        if (level.getBlockState(abovePos).is(net.minecraft.tags.BlockTags.LOGS)) {
            return false;
        }

        return true;
    }
}
