package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Time Blossom - Decorative flower found in the ChronoDawn dimension.
 *
 * Properties:
 * - Simple decorative flower with no special effects
 * - Can be placed on grass, dirt, farmland, and similar blocks
 * - Can be placed in flower pots
 * - Purple-themed appearance matching ChronoDawn aesthetic
 *
 * Placement:
 * - Can be placed on dirt, grass, farmland, etc. (standard flower placement rules)
 * - Breaks instantly when mined
 *
 * Drops:
 * - Drops 1x Time Blossom item when broken
 */
public class TimeBlossomBlock extends BushBlock {
    private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);

    public TimeBlossomBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Factory method for creating block properties.
     * Follows the pattern used by other blocks in the mod.
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // Allow placement on standard flower-compatible blocks
        return state.is(BlockTags.DIRT) ||
               state.is(BlockTags.SAND) ||
               state.is(net.minecraft.world.level.block.Blocks.FARMLAND);
    }
}
