package com.chronosphere.blocks;

import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModItems;
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
 * Fruit of Time Block - A growing fruit block attached to Time Wood logs.
 *
 * Similar to Cocoa blocks in vanilla Minecraft, this block:
 * - Attaches to the side of Time Wood Log blocks
 * - Has 3 growth stages (0-2), where 2 is mature
 * - Grows over time using random ticks
 * - Can be harvested when mature for Fruit of Time items
 * - Has directional facing (NORTH, SOUTH, EAST, WEST)
 *
 * Properties:
 * - AGE: 0-2 (growth stages)
 * - FACING: Horizontal direction (which side of log it's attached to)
 * - Growth: Random tick based, similar to crops
 * - Drop: 1-3 Fruit of Time items when mature (age=2)
 *
 * Visual:
 * - Stage 0: Small fruit bud (fruit_of_time_stage_0.png)
 * - Stage 1: Medium-sized growing fruit (fruit_of_time_stage_1.png)
 * - Stage 2: Large mature fruit (fruit_of_time_stage_2.png)
 *
 * Mechanics:
 * - Must be placed on Time Wood Log blocks
 * - Can be bone-mealed to accelerate growth
 * - Breaking the supporting log will break the fruit
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
     * Facing property for directional placement (horizontal directions only).
     */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /**
     * VoxelShapes for each growth stage and direction.
     * Shape dimensions increase as the fruit grows.
     */
    private static final VoxelShape[] EAST_SHAPES = new VoxelShape[]{
            Block.box(11.0, 7.0, 6.0, 15.0, 12.0, 10.0),  // Stage 0: Small
            Block.box(9.0, 5.0, 5.0, 15.0, 12.0, 11.0),   // Stage 1: Medium
            Block.box(7.0, 3.0, 4.0, 15.0, 12.0, 12.0)    // Stage 2: Large (mature)
    };

    private static final VoxelShape[] WEST_SHAPES = new VoxelShape[]{
            Block.box(1.0, 7.0, 6.0, 5.0, 12.0, 10.0),    // Stage 0: Small
            Block.box(1.0, 5.0, 5.0, 7.0, 12.0, 11.0),    // Stage 1: Medium
            Block.box(1.0, 3.0, 4.0, 9.0, 12.0, 12.0)     // Stage 2: Large (mature)
    };

    private static final VoxelShape[] NORTH_SHAPES = new VoxelShape[]{
            Block.box(6.0, 7.0, 1.0, 10.0, 12.0, 5.0),    // Stage 0: Small
            Block.box(5.0, 5.0, 1.0, 11.0, 12.0, 7.0),    // Stage 1: Medium
            Block.box(4.0, 3.0, 1.0, 12.0, 12.0, 9.0)     // Stage 2: Large (mature)
    };

    private static final VoxelShape[] SOUTH_SHAPES = new VoxelShape[]{
            Block.box(6.0, 7.0, 11.0, 10.0, 12.0, 15.0),  // Stage 0: Small
            Block.box(5.0, 5.0, 9.0, 11.0, 12.0, 15.0),   // Stage 1: Medium
            Block.box(4.0, 3.0, 7.0, 12.0, 12.0, 15.0)    // Stage 2: Large (mature)
    };

    public FruitOfTimeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
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
        BlockPos attachedPos = pos.relative(state.getValue(FACING).getOpposite());
        BlockState attachedState = level.getBlockState(attachedPos);

        // Can only survive if attached to Time Wood Log
        return attachedState.is(ModBlocks.TIME_WOOD_LOG.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPES[age];
            case NORTH -> NORTH_SHAPES[age];
            case WEST -> WEST_SHAPES[age];
            default -> EAST_SHAPES[age];  // EAST
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        // When player clicks a face of the log, the new block is placed on that face
        // The fruit's FACING should point back toward the log (opposite of clicked face)
        // Example: Player clicks NORTH face of log → Fruit placed on north side → Fruit faces SOUTH (toward log)
        Direction fruitFacing = clickedFace.getOpposite();

        // Only allow horizontal placement
        if (fruitFacing.getAxis().isHorizontal()) {
            BlockState state = this.defaultBlockState().setValue(FACING, fruitFacing);

            // Check if the fruit can survive at this position (log must be behind it)
            if (state.canSurvive(level, pos)) {
                return state;
            }
        }

        return null;  // Cannot be placed
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Break if the supporting log is removed
        if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE);
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
