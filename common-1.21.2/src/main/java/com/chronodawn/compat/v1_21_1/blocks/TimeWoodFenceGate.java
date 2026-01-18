package com.chronodawn.compat.v1_21_1.blocks;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

/**
 * Time Wood Fence Gate block.
 *
 * Standard fence gate that can be opened/closed.
 * Can be opened with redstone signal.
 * Connects to fences and walls.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Fence Gate)
 * - Blast Resistance: 3.0 (same as Oak Fence Gate)
 * - Tool Required: None (axe is faster)
 * - Can be opened manually or with redstone
 *
 * Crafting:
 * - 2x Stick + 4x Time Wood Planks (fence gate pattern) → 1x Time Wood Fence Gate
 *
 * Task: T080y [P] [US1] Create Time Wood Fence Gate block
 */
public class TimeWoodFenceGate extends FenceGateBlock {
    public TimeWoodFenceGate(BlockBehaviour.Properties properties) {
        super(WoodType.OAK, properties);
    }

    /**
     * Create default properties for Time Wood Fence Gate.
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
