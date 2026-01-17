package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Clockstone Stairs - Stair variant of Clockstone Block.
 *
 * Standard stair block crafted from Clockstone Block.
 *
 * Crafting:
 * - 6x Clockstone Block → 4x Clockstone Stairs (stair crafting pattern)
 */
public class ClockstoneStairs extends StairBlock {
    public ClockstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.CLOCKSTONE_BLOCK.get().defaultBlockState(), properties);
    }

    /**
     * Create default properties for Clockstone Stairs.
     *
     * @return Block properties copied from Clockstone Block
     */
    public static BlockBehaviour.Properties createProperties() {
        return ClockstoneBlock.createProperties();
    }
}
