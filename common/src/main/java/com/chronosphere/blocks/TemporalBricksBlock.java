package com.chronosphere.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Temporal Bricks - Building block crafted from Clockstone.
 *
 * This block serves as a refined building material with a brick-like appearance
 * and time-themed aesthetic. Suitable for constructing structures and decorative builds.
 *
 * Properties:
 * - Hardness: 2.5 (similar to stone bricks)
 * - Blast Resistance: 6.0 (same as stone bricks)
 * - Requires pickaxe to mine
 * - Stone sound
 *
 * Crafting:
 * - 4x Clockstone → 1x Temporal Bricks (2x2 crafting pattern)
 *
 * Usage:
 * - Building material for structures
 * - Can be crafted into stairs, slabs, walls, and other variants
 * - Time-themed alternative to stone bricks
 *
 * Visual:
 * - Brick-like texture with time/clock theme
 * - Purple/blue color scheme to match Chronosphere aesthetic
 *
 * Task: T242 [P] [US1] Create Temporal Bricks block
 */
public class TemporalBricksBlock extends Block {
    public TemporalBricksBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Temporal Bricks.
     *
     * @return Block properties with appropriate settings for brick building material
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)   // Purple color for time theme
                .strength(2.5f, 6.0f)              // Medium hardness, good blast resistance
                .requiresCorrectToolForDrops()      // Requires pickaxe
                .sound(SoundType.STONE);            // Stone sound for brick material
    }
}
