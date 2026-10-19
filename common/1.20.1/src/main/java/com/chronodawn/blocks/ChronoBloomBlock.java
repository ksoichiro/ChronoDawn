package com.chronodawn.blocks;

import com.chronodawn.compat.CompatLevelTime;
import com.chronodawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Chrono Bloom - Flower that opens and closes with the time of day.
 *
 * STAGE follows {@link ChronoBloomStage#stageFor(long)}: closed at night, opening at dawn,
 * open during the day, and closing at dusk. The stage is set on placement and refreshed
 * on random ticks, so a flower catches up with the time within a short while.
 *
 * Bone meal drops one extra Chrono Bloom, like vanilla small flowers spread copies.
 */
public class ChronoBloomBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);

    public ChronoBloomBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, ChronoBloomStage.CLOSED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    /**
     * Same properties as the Time Blossom, plus random ticks for the stage update and no light.
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .replaceable()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .randomTicks()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(STAGE, ChronoBloomStage.stageFor(CompatLevelTime.dayTime(context.getLevel())));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateStage(state, level, pos);
    }

    /**
     * Sets STAGE to the stage for the current time when it differs.
     * Flag 2 sends the change to clients without neighbor updates, since only the look changes.
     * Shared with {@link PottedChronoBloomBlock}.
     */
    static void updateStage(BlockState state, Level level, BlockPos pos) {
        int stage = ChronoBloomStage.stageFor(CompatLevelTime.dayTime(level));
        if (state.getValue(STAGE) != stage) {
            level.setBlock(pos, state.setValue(STAGE, stage), Block.UPDATE_CLIENTS);
        }
    }

    // ==================== Bonemealable Interface ====================

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClientSide) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        popResource(level, pos, new ItemStack(ModItems.CHRONO_BLOOM.get()));
    }
}
