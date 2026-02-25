package com.chronodawn.blocks;

import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

/**
 * Time Wood Trapdoor block.
 *
 * Standard wooden trapdoor that can be opened/closed and placed horizontally or vertically.
 * Can be opened with redstone signal.
 *
 * Properties:
 * - Hardness: 3.0 (same as Oak Trapdoor)
 * - Blast Resistance: 3.0 (same as Oak Trapdoor)
 * - Tool Required: None (axe is faster)
 * - Can be opened manually or with redstone
 *
 * Crafting:
 * - 6x Time Wood Planks (trapdoor crafting pattern) → 2x Time Wood Trapdoor
 *
 * Task: T080x [P] [US1] Create Time Wood Trapdoor block
 */
public class TimeWoodTrapdoor extends TrapDoorBlock {
    public TimeWoodTrapdoor(BlockBehaviour.Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    /**
     * Create default properties for Time Wood Trapdoor.
     *
     * @return Block properties similar to vanilla wooden trapdoors
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(3.0f, 3.0f) // hardness, blast resistance (same as Oak Trapdoor)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .isValidSpawn((state, level, pos, entityType) -> false);
    }
}
