package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Clockstone Block - Portal frame building material.
 *
 * This block is crafted from Clockstone items and used to construct portal frames
 * for traveling to the ChronoDawn dimension.
 *
 * Properties:
 * - Hardness: 5.0 (harder than stone, similar to iron block)
 * - Blast Resistance: 6.0 (more resistant than stone)
 * - Requires pickaxe to mine
 * - Used as portal frame material
 *
 * Crafting:
 * - 9x Clockstone items → 1x Clockstone Block (3x3 crafting)
 * - Reversible: 1x Clockstone Block → 9x Clockstone items
 *
 * Usage:
 * 1. Mine Clockstone Ore to obtain Clockstone items
 * 2. Craft Clockstone items into Clockstone Blocks
 * 3. Build portal frame (4x5 to 23x23 rectangle)
 * 4. Ignite with Time Hourglass to activate portal
 *
 * Reference: data-model.md (Portal System → Portal Frame)
 * Task: T045 [US1] Portal Frame Validator (requires portal frame block)
 */
public class ClockstoneBlock extends Block {
    public ClockstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Block.
     *
     * @return Block properties with appropriate settings for portal frame material
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)  // Purple color for time-themed block
                .strength(5.0f, 6.0f)              // Harder than stone
                .requiresCorrectToolForDrops()      // Requires pickaxe
                .sound(SoundType.METAL);            // Metallic sound for mystical material
    }
}
