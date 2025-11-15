package com.chronosphere.blocks;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

/**
 * Time Wood Button block.
 *
 * Standard wooden button that emits redstone signal when pressed.
 * Stays active for 1.5 seconds (30 game ticks).
 * Can be placed on any solid surface.
 *
 * Properties:
 * - Hardness: 0.5 (same as Oak Button)
 * - Blast Resistance: 0.5 (same as Oak Button)
 * - Tool Required: None
 * - Signal duration: 30 ticks (1.5 seconds)
 *
 * Crafting:
 * - 1x Time Wood Planks → 1x Time Wood Button
 *
 * Task: T080z [P] [US1] Create Time Wood Button block
 */
public class TimeWoodButton extends ButtonBlock {
    public TimeWoodButton(BlockBehaviour.Properties properties) {
        super(BlockSetType.OAK, 30, properties);
    }

    /**
     * Create default properties for Time Wood Button.
     *
     * @return Block properties similar to vanilla wooden buttons
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .noCollission()
                .strength(0.5f) // hardness and blast resistance (same as Oak Button)
                .sound(SoundType.WOOD);
    }
}
