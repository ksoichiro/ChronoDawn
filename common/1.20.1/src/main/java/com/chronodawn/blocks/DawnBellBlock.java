package com.chronodawn.blocks;

import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Dawn Bell - Tall decorative flower found in the ChronoDawn dimension.
 *
 * Properties:
 * - Blue-themed tall flower (2 blocks high)
 * - Can be placed on grass, dirt, farmland, and similar blocks
 * - Cannot be placed in flower pots (too tall)
 * - Paired with Dusk Bell to represent time cycle (dawn/dusk)
 *
 * Placement:
 * - Requires 2 blocks of vertical space
 * - Can be placed on dirt, grass, farmland, etc. (standard flower placement rules)
 * - Breaks instantly when mined
 *
 * Drops:
 * - Drops 1x Dawn Bell item when either block is broken
 */
public class DawnBellBlock extends DoublePlantBlock {
    public DawnBellBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Factory method for creating block properties.
     * Follows the pattern used by other blocks in the mod.
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ);
    }
}
