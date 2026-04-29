package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TemporalPointedDripstoneBlock extends Block {
    public static final EnumProperty<DripstoneDirection> DIRECTION = EnumProperty.create("direction", DripstoneDirection.class);
    public static final EnumProperty<Thickness> THICKNESS = EnumProperty.create("thickness", Thickness.class);

    private static final VoxelShape TIP_UP = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape TIP_DOWN = Block.box(5.0, 5.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape FRUSTUM = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);

    public TemporalPointedDripstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(DIRECTION, DripstoneDirection.DOWN)
            .setValue(THICKNESS, Thickness.TIP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION, THICKNESS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShapeForState(state);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (state.getValue(DIRECTION) == DripstoneDirection.UP && state.getValue(THICKNESS) == Thickness.TIP) {
            entity.causeFallDamage(fallDistance + 2.0f, 2.0f, level.damageSources().stalagmite());
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        super.onRemove(state, level, pos, newState, moved);
        if (!level.isClientSide && state.getValue(DIRECTION) == DripstoneDirection.DOWN) {
            cascadeBreak(level, pos);
        }
    }

    private static void cascadeBreak(Level level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.getBlock() instanceof TemporalPointedDripstoneBlock
            && belowState.getValue(DIRECTION) == DripstoneDirection.DOWN) {
            Block.dropResources(belowState, level, below);
            level.removeBlock(below, false);
            cascadeBreak(level, below);
        }
    }

    private static VoxelShape getShapeForState(BlockState state) {
        if (state.getValue(THICKNESS) == Thickness.TIP) {
            return state.getValue(DIRECTION) == DripstoneDirection.UP ? TIP_UP : TIP_DOWN;
        }
        return FRUSTUM;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        DripstoneDirection direction = context.getClickedFace() == Direction.DOWN
            ? DripstoneDirection.DOWN : DripstoneDirection.UP;

        return defaultBlockState().setValue(DIRECTION, direction).setValue(THICKNESS, Thickness.TIP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        DripstoneDirection direction = state.getValue(DIRECTION);
        BlockPos growthPos = direction == DripstoneDirection.UP ? pos.above() : pos.below();
        if (!facingPos.equals(growthPos)) {
            return state;
        }

        Thickness newThickness = calculateThickness(level, pos, direction);
        if (state.getValue(THICKNESS) == newThickness) {
            return state;
        }
        return state.setValue(THICKNESS, newThickness);
    }

    private static Thickness calculateThickness(LevelAccessor level, BlockPos pos, DripstoneDirection direction) {
        BlockPos growthPos = direction == DripstoneDirection.UP ? pos.above() : pos.below();
        BlockState growthState = level.getBlockState(growthPos);
        if (isSameDripstoneWithDirection(growthState, direction)) {
            return Thickness.FRUSTUM;
        }
        return Thickness.TIP;
    }

    private static boolean isSameDripstoneWithDirection(BlockState state, DripstoneDirection direction) {
        return state.getBlock() instanceof TemporalPointedDripstoneBlock
            && state.getValue(DIRECTION) == direction;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.POINTED_DRIPSTONE)
                .noOcclusion();
    }

    public enum DripstoneDirection implements StringRepresentable {
        UP("up"),
        DOWN("down");

        private final String serializedName;

        DripstoneDirection(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }

    public enum Thickness implements StringRepresentable {
        TIP("tip"),
        FRUSTUM("frustum"),
        MIDDLE("middle"),
        BASE("base");

        private final String serializedName;

        Thickness(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }
    }
}
