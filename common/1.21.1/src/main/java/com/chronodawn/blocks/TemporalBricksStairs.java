package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Temporal Bricks Stairs - Stair variant of Temporal Bricks.
 *
 * Standard stair block crafted from Temporal Bricks.
 *
 * Crafting:
 * - 6x Temporal Bricks → 4x Temporal Bricks Stairs (stair crafting pattern)
 *
 * Task: T243 [P] [US1] Create stairs/slabs/walls/fences variants for Temporal Bricks
 */
public class TemporalBricksStairs extends StairBlock {
    public TemporalBricksStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_BRICKS.get().defaultBlockState(), properties);
    }

    /**
     * Create default properties for Temporal Bricks Stairs.
     *
     * @return Block properties copied from Temporal Bricks
     */
    public static BlockBehaviour.Properties createProperties() {
        return TemporalBricksBlock.createProperties();
    }
}
