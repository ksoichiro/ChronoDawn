package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Dusk Bell - Tall decorative flower found in the ChronoDawn dimension.
 *
 * Properties:
 * - Red-themed tall flower (2 blocks high)
 * - Can be placed on grass, dirt, farmland, and similar blocks
 * - Cannot be placed in flower pots (too tall)
 * - Paired with Dawn Bell to represent time cycle (dawn/dusk)
 *
 * Placement:
 * - Requires 2 blocks of vertical space
 * - Can be placed on dirt, grass, farmland, etc. (standard flower placement rules)
 * - Breaks instantly when mined
 *
 * Drops:
 * - Drops 1x Dusk Bell item when either block is broken
 */
public class DuskBellBlock extends TallFlowerBlock {
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public DuskBellBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // OffsetType.XZ shifts the model randomly, so the outline must follow the
    // same offset or it stays centered while the flower is drawn off-center.
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.move(state.getOffset(pos));
    }

    /**
     * Factory method for creating block properties.
     * Follows the pattern used by other blocks in the mod.
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .replaceable()
            .noCollision()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
            .lightLevel(state -> 4)
            .offsetType(BlockBehaviour.OffsetType.XZ)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dusk_bell")));
    }
}
