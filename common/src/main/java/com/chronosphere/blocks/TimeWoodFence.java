package com.chronosphere.blocks;

import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Time Wood Fence - Fence variant of Time Wood Planks.
 *
 * Standard fence block crafted from Time Wood Planks.
 *
 * Crafting:
 * - 4x Time Wood Planks + 2x Stick → 3x Time Wood Fence
 *
 * Task: T243 [P] [US1] Create stairs/slabs/walls/fences variants for Time Wood Planks
 */
public class TimeWoodFence extends FenceBlock {
    public TimeWoodFence(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Wood Fence.
     *
     * @return Block properties copied from Time Wood Planks
     */
    public static BlockBehaviour.Properties createProperties() {
        return TimeWoodPlanks.createProperties();
    }
}
