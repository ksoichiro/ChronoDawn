package com.chronosphere.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Clockstone Ore block.
 *
 * A custom ore block found in the Overworld (Ancient Ruins) and Chronosphere dimension.
 * Drops Clockstone item when mined.
 *
 * Properties:
 * - Hardness: 3.0 (similar to Iron Ore)
 * - Blast Resistance: 3.0
 * - Requires correct tool (pickaxe) for drops
 * - Fortune enchantment compatible
 *
 * Reference: data-model.md (Blocks → Clockstone Ore)
 */
public class ClockstoneOre extends Block {
    public ClockstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Ore.
     *
     * @return Block properties with appropriate settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f) // hardness, blast resistance
                .requiresCorrectToolForDrops() // Requires pickaxe
                .sound(SoundType.STONE);
    }
}
