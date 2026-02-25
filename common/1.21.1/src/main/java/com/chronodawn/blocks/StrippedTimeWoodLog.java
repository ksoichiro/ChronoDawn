package com.chronodawn.blocks;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Stripped Time Wood Log block.
 *
 * Stripped variant of Time Wood Log, obtained by right-clicking with an axe.
 * Has bark removed, showing the inner wood texture.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Log)
 * - Blast Resistance: 2.0
 * - Flammable: Yes
 * - Directional: Can be placed in X, Y, Z axis
 *
 * Visual:
 * - Side texture: stripped_time_wood_log.png (inner wood without bark)
 * - Top texture: stripped_time_wood_log_top.png (time-themed rings, same as unstripped)
 *
 * Reference: T719 [Add axe stripping functionality for Time Wood logs]
 */
public class StrippedTimeWoodLog extends RotatedPillarBlock {
    public StrippedTimeWoodLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Stripped Time Wood Log.
     *
     * @return Block properties with wood-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 2.0f) // hardness, blast resistance (same as Oak Log)
                .sound(SoundType.WOOD);
    }
}
