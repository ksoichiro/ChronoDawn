package com.chronosphere.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Clockwork Block - Decorative block with animated rotating gears theme.
 *
 * This block serves as a decorative building material with a clockwork mechanism theme.
 * Perfect for creating steampunk or time-themed builds in the Chronosphere dimension.
 *
 * Properties:
 * - Hardness: 3.5 (harder than stone, softer than Clockstone Block)
 * - Blast Resistance: 5.0 (resistant to explosions)
 * - Requires pickaxe to mine
 * - Metallic sound
 * - Decorative block with gear texture
 *
 * Crafting:
 * - 4x Clockstone + 4x Iron Ingot + 1x Redstone → 1x Clockwork Block (to be defined in recipe)
 *
 * Usage:
 * - Decorative building material for structures
 * - Adds mechanical/steampunk aesthetic to builds
 * - Can be used in combination with other time-themed blocks
 *
 * Note: Animated texture with rotating gears should be implemented as a client-side texture animation
 * using .mcmeta file alongside the texture.
 *
 * Task: T240 [P] [US1] Create Clockwork Block
 */
public class ClockworkBlock extends Block {
    public ClockworkBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockwork Block.
     *
     * @return Block properties with appropriate settings for decorative mechanical block
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)         // Metallic color for clockwork theme
                .strength(3.5f, 5.0f)             // Medium hardness, good blast resistance
                .requiresCorrectToolForDrops()     // Requires pickaxe
                .sound(SoundType.METAL);           // Metallic sound for mechanical block
    }
}
