package com.chronodawn.blocks;

import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

/**
 * Ancient Time Wood Door block.
 *
 * Standard wooden door that can be opened/closed by players.
 * Can be opened with redstone signal.
 *
 * Properties:
 * - Hardness: 3.0 (same as Oak Door)
 * - Blast Resistance: 3.0 (same as Oak Door)
 * - Tool Required: None (axe is faster)
 * - Can be opened manually or with redstone
 *
 * Crafting:
 * - 6x Ancient Time Wood Planks (door crafting pattern) → 3x Ancient Time Wood Door
 */
public class AncientTimeWoodDoor extends DoorBlock {
    public AncientTimeWoodDoor(BlockBehaviour.Properties properties) {
        super(properties, BlockSetType.OAK);
    }

    /**
     * Create default properties for Ancient Time Wood Door.
     *
     * @return Block properties similar to vanilla wooden doors
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(3.0f, 3.0f) // hardness, blast resistance (same as Oak Door)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }
}
