package com.chronodawn.compat.v1_21_2.blocks;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

/**
 * Ancient Time Wood Fence Gate block.
 *
 * Standard fence gate that connects to fences.
 * Can be opened manually or with redstone signal.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Fence Gate)
 * - Blast Resistance: 3.0 (same as Oak Fence Gate)
 * - Tool Required: None (axe is faster)
 * - Can be opened manually or with redstone
 *
 * Crafting:
 * - 4x Stick + 2x Ancient Time Wood Planks → 1x Ancient Time Wood Fence Gate
 */
public class AncientTimeWoodFenceGate extends FenceGateBlock {
    public AncientTimeWoodFenceGate(BlockBehaviour.Properties properties) {
        super(WoodType.OAK, properties);
    }

    /**
     * Create default properties for Ancient Time Wood Fence Gate.
     *
     * @return Block properties similar to vanilla wooden fence gates
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 3.0f) // hardness, blast resistance (same as Oak Fence Gate)
                .sound(SoundType.WOOD);
    }
}
