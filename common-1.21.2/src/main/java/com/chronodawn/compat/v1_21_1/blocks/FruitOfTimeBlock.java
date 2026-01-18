package com.chronodawn.compat.v1_21_1.blocks;

import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Fruit of Time Block - A growing fruit block hanging from Time Wood leaves.
 *
 * Similar to Apple-like fruits, this block:
 * - Hangs from the bottom of Time Wood Leaves blocks
 * - Has 3 growth stages (0-2), where 2 is mature
 * - Grows over time using random ticks
 * - Can be harvested when mature for Fruit of Time items
 * - Always faces downward (no directional property needed)
 *
 * Properties:
 * - AGE: 0-2 (growth stages)
 * - Growth: Random tick based, similar to crops
 * - Drop: 2-3 Fruit of Time items when mature (age=2), 1 when immature
 *
 * Visual:
 * - Stage 0: Small fruit bud (fruit_of_time_stage_0.png)
 * - Stage 1: Medium-sized growing fruit (fruit_of_time_stage_1.png)
 * - Stage 2: Large mature fruit (fruit_of_time_stage_2.png)
 *
 * Mechanics:
 * - Must hang from Time Wood Leaves blocks
 * - Can be bone-mealed to accelerate growth
 * - Breaking the supporting leaves will break the fruit
 *
 * Reference: T080k [US1] Create Fruit of Time block
 * Related: FruitDecorator places these during tree generation
 */
public class FruitOfTimeBlock extends Block implements BonemealableBlock {
    /**
     * Maximum age for the fruit (0-2, where 2 is mature).
     */
    public static final int MAX_AGE = 2;

    /**
     * Age property tracking growth stage.
     */
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;

    /**
     * VoxelShapes for each growth stage (hanging downward).
     * Shape dimensions increase as the fruit grows.
     * Fruits hang from the top (y=16) and grow downward.
     */
    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(6.0, 12.0, 6.0, 10.0, 16.0, 10.0),  // Stage 0: Small (4x4x4)
            Block.box(4.0, 9.0, 4.0, 11.0, 16.0, 11.0),   // Stage 1: Medium (7x7x7)
            Block.box(4.0, 6.0, 4.0, 12.0, 16.0, 12.0)    // Stage 2: Large (8x10x8)
    };

    public FruitOfTimeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0));
    }

    /**
     * Create default properties for Fruit of Time Block.
     *
     * @return Block properties with fruit-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .randomTicks()                          // Enable random tick for growth
                .strength(0.2f, 3.0f)                   // Weak hardness, moderate blast resistance
                .sound(SoundType.WOOD)
                .noOcclusion()                          // Not a full block
                .pushReaction(PushReaction.DESTROY);    // Destroyed by pistons
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Growth logic: 20% chance per random tick (similar to crops)
        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = state.getValue(AGE);
            if (age < MAX_AGE && random.nextInt(5) == 0) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        // Can only survive if hanging from Time Wood Leaves
        return aboveState.is(ModBlocks.TIME_WOOD_LEAVES.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return SHAPES[age];
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = this.defaultBlockState();

        // Check if the fruit can survive at this position (leaves must be above)
        if (state.canSurvive(level, pos)) {
            return state;
        }

        return null;  // Cannot be placed
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Break if the supporting leaves above are removed
        if (direction == Direction.UP && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // ==================== Bonemealable Interface ====================

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;  // Always succeed when bone meal is used
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // Advance growth by 1 stage when bone mealed
        level.setBlock(pos, state.setValue(AGE, Math.min(MAX_AGE, state.getValue(AGE) + 1)), 2);
    }
}
