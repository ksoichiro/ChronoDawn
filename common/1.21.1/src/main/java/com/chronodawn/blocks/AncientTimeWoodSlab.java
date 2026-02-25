package com.chronodawn.blocks;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Ancient Time Wood Slab - Slab variant of Ancient Time Wood Planks.
 *
 * Standard slab block crafted from Ancient Time Wood Planks.
 *
 * Crafting:
 * - 3x Ancient Time Wood Planks → 6x Ancient Time Wood Slab (horizontal row)
 */
public class AncientTimeWoodSlab extends SlabBlock {
    public AncientTimeWoodSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Ancient Time Wood Slab.
     *
     * @return Block properties copied from Ancient Time Wood Planks
     */
    public static BlockBehaviour.Properties createProperties() {
        return AncientTimeWoodPlanks.createProperties();
    }
}
