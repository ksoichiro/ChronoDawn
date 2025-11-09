package com.chronosphere.blocks;

import com.chronosphere.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Time Wood Stairs - Stair variant of Time Wood Planks.
 *
 * Standard stair block crafted from Time Wood Planks.
 *
 * Crafting:
 * - 6x Time Wood Planks → 4x Time Wood Stairs (stair crafting pattern)
 *
 * Task: T243 [P] [US1] Create stairs/slabs/walls/fences variants for Time Wood Planks
 */
public class TimeWoodStairs extends StairBlock {
    public TimeWoodStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TIME_WOOD_PLANKS.get().defaultBlockState(), properties);
    }

    /**
     * Create default properties for Time Wood Stairs.
     *
     * @return Block properties copied from Time Wood Planks
     */
    public static BlockBehaviour.Properties createProperties() {
        return TimeWoodPlanks.createProperties();
    }
}
