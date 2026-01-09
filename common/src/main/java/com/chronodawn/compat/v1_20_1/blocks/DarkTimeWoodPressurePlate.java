package com.chronodawn.blocks;

import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Dark Time Wood Pressure Plate block.
 *
 * Wooden pressure plate that emits redstone signal.
 * Activated by players, mobs, items, and other entities.
 *
 * Properties:
 * - Hardness: 0.5 (same as Oak Pressure Plate)
 * - Blast Resistance: 0.5 (same as Oak Pressure Plate)
 * - Tool Required: None
 * - Detects: All entities (players, mobs, items, etc.)
 *
 * Crafting:
 * - 2x Dark Time Wood Planks (horizontal row) → 1x Dark Time Wood Pressure Plate
 */
public class DarkTimeWoodPressurePlate extends PressurePlateBlock {
    public DarkTimeWoodPressurePlate(BlockBehaviour.Properties properties) {
        // 1.20.1: PressurePlateBlock constructor takes (sensitivity, properties, blockSetType)
        super(PressurePlateBlock.Sensitivity.EVERYTHING, properties, BlockSetType.OAK);
    }

    /**
     * Create default properties for Dark Time Wood Pressure Plate.
     *
     * @return Block properties similar to vanilla wooden pressure plates
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .noCollission()
                .strength(0.5f) // hardness and blast resistance (same as Oak Pressure Plate)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.DESTROY);
    }
}
