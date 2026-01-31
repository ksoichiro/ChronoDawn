package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Clockstone Wall - Wall variant of Clockstone Block.
 *
 * Standard wall block crafted from Clockstone Block.
 *
 * Crafting:
 * - 6x Clockstone Block → 6x Clockstone Wall (2 rows of 3)
 */
public class ClockstoneWall extends WallBlock {
    public ClockstoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Wall.
     *
     * @return Block properties copied from Clockstone Block
     */
    public static BlockBehaviour.Properties createProperties() {
        return ClockstoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_wall")));
    }
}
