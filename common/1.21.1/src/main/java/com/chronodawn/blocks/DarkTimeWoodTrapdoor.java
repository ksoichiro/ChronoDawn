package com.chronodawn.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

/**
 * Dark Time Wood Trapdoor block.
 *
 * Standard wooden trapdoor that can be opened/closed by players.
 * Can be placed horizontally or vertically.
 *
 * Properties:
 * - Hardness: 3.0 (same as Oak Trapdoor)
 * - Blast Resistance: 3.0 (same as Oak Trapdoor)
 * - Tool Required: None (axe is faster)
 * - Can be opened manually or with redstone
 *
 * Crafting:
 * - 6x Dark Time Wood Planks (2 rows of 3) → 2x Dark Time Wood Trapdoor
 */
public class DarkTimeWoodTrapdoor extends TrapDoorBlock {
    public DarkTimeWoodTrapdoor(BlockBehaviour.Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    /**
     * Create default properties for Dark Time Wood Trapdoor.
     *
     * @return Block properties similar to vanilla wooden trapdoors
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(3.0f, 3.0f) // hardness, blast resistance (same as Oak Trapdoor)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }
}
