package com.chronodawn.blocks;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Dark Time Wood Log block.
 *
 * A variant of Time Wood Log with a darker appearance.
 * Found in tall, ancient trees in the ChronoDawn dimension.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Log)
 * - Blast Resistance: 2.0
 * - Flammable: Yes
 * - Directional: Can be placed in X, Y, Z axis
 * - Darker color than regular Time Wood Log
 *
 * Reference: T088t [US1] Design Time Wood color variants
 */
public class DarkTimeWoodLog extends RotatedPillarBlock {
    public DarkTimeWoodLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Dark Time Wood Log.
     *
     * @return Block properties with wood-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PODZOL) // Darker brown color
                .strength(2.0f, 2.0f)
                .sound(SoundType.WOOD);
    }
}
