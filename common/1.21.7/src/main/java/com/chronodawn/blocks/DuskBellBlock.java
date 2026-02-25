package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Dusk Bell - Tall decorative flower found in the ChronoDawn dimension.
 *
 * Properties:
 * - Red-themed tall flower (2 blocks high)
 * - Can be placed on grass, dirt, farmland, and similar blocks
 * - Cannot be placed in flower pots (too tall)
 * - Paired with Dawn Bell to represent time cycle (dawn/dusk)
 *
 * Placement:
 * - Requires 2 blocks of vertical space
 * - Can be placed on dirt, grass, farmland, etc. (standard flower placement rules)
 * - Breaks instantly when mined
 *
 * Drops:
 * - Drops 1x Dusk Bell item when either block is broken
 */
public class DuskBellBlock extends DoublePlantBlock {
    public DuskBellBlock(BlockBehaviour.Properties properties) {
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
            .offsetType(BlockBehaviour.OffsetType.XZ)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dusk_bell")));
    }
}
