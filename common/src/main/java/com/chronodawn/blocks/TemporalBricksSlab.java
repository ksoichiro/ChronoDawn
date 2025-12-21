package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Temporal Bricks Slab - Slab variant of Temporal Bricks.
 *
 * Standard slab block crafted from Temporal Bricks.
 *
 * Crafting:
 * - 3x Temporal Bricks → 6x Temporal Bricks Slab (horizontal row)
 *
 * Task: T243 [P] [US1] Create stairs/slabs/walls/fences variants for Temporal Bricks
 */
public class TemporalBricksSlab extends SlabBlock {
    public TemporalBricksSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Temporal Bricks Slab.
     *
     * @return Block properties copied from Temporal Bricks
     */
    public static BlockBehaviour.Properties createProperties() {
        return TemporalBricksBlock.createProperties();
    }
}
