package com.chronodawn.compat.v1_20_1.blocks;

import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

/**
 * Time Wood Pressure Plate block.
 *
 * Standard wooden pressure plate that emits redstone signal when stepped on.
 * Activated by players, mobs, items, and other entities.
 *
 * Properties:
 * - Hardness: 0.5 (same as Oak Pressure Plate)
 * - Blast Resistance: 0.5 (same as Oak Pressure Plate)
 * - Tool Required: None (axe is faster)
 * - Activated by any entity
 *
 * Crafting:
 * - 2x Time Wood Planks (pressure plate pattern) → 1x Time Wood Pressure Plate
 *
 * Task: T080aa [P] [US1] Create Time Wood Pressure Plate block
 */
public class TimeWoodPressurePlate extends PressurePlateBlock {
    public TimeWoodPressurePlate(BlockBehaviour.Properties properties) {
        // 1.20.1: PressurePlateBlock constructor takes (sensitivity, properties, blockSetType)
        super(PressurePlateBlock.Sensitivity.EVERYTHING, properties, BlockSetType.OAK);
    }

    /**
     * Create default properties for Time Wood Pressure Plate.
     *
     * @return Block properties similar to vanilla wooden pressure plates
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .noCollission()
                .strength(0.5f) // hardness and blast resistance (same as Oak Pressure Plate)
                .sound(SoundType.WOOD);
    }
}
