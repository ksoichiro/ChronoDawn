package com.chronodawn.blocks;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Ancient Time Wood Log block.
 *
 * A variant of Time Wood Log with a weathered, ancient appearance.
 * Found in wide-canopy trees in the ChronoDawn dimension.
 *
 * Properties:
 * - Hardness: 2.0 (same as Oak Log)
 * - Blast Resistance: 2.0
 * - Flammable: Yes
 * - Directional: Can be placed in X, Y, Z axis
 * - Weathered, grayish-brown color
 *
 * Reference: T088t [US1] Design Time Wood color variants
 */
public class AncientTimeWoodLog extends RotatedPillarBlock {
    public AncientTimeWoodLog(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Ancient Time Wood Log.
     *
     * @return Block properties with wood-like settings
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN) // Weathered brown color
                .strength(2.0f, 2.0f)
                .sound(SoundType.WOOD);
    }
}
