package com.chronodawn.blocks;

import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Dark Time Wood Fence - Fence variant of Dark Time Wood Planks.
 *
 * Standard fence block that connects to other fences and blocks.
 *
 * Crafting:
 * - 4x Dark Time Wood Planks + 2x Stick → 3x Dark Time Wood Fence
 */
public class DarkTimeWoodFence extends FenceBlock {
    public DarkTimeWoodFence(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Dark Time Wood Fence.
     *
     * @return Block properties for wooden fence
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 3.0f)
                .sound(SoundType.WOOD);
    }
}
