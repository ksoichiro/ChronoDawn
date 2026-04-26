package com.chronodawn.blocks;

import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Temporal Tall Grass - Tall decorative grass found in the ChronoDawn dimension.
 *
 * Properties:
 * - Tall grass (2 blocks high) with no special effects
 * - Can be placed on grass, dirt, farmland, and similar blocks
 * - Breaks instantly when mined
 *
 * Placement:
 * - Requires 2 blocks of vertical space
 * - Can be placed on dirt, grass, farmland, etc. (standard tall grass placement rules)
 *
 * Drops:
 * - Drops 1x Temporal Tall Grass item when the lower half is broken
 */
public class TemporalTallGrassBlock extends DoublePlantBlock {
    public TemporalTallGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ);
    }
}
