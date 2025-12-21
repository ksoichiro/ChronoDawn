package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Temporal Bricks Wall - Wall variant of Temporal Bricks.
 *
 * Standard wall block crafted from Temporal Bricks.
 *
 * Crafting:
 * - 6x Temporal Bricks → 6x Temporal Bricks Wall (2 rows of 3)
 *
 * Task: T243 [P] [US1] Create stairs/slabs/walls/fences variants for Temporal Bricks
 */
public class TemporalBricksWall extends WallBlock {
    public TemporalBricksWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Temporal Bricks Wall.
     *
     * @return Block properties copied from Temporal Bricks
     */
    public static BlockBehaviour.Properties createProperties() {
        return TemporalBricksBlock.createProperties();
    }
}
