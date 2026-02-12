package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Clockstone Slab - Slab variant of Clockstone Block.
 *
 * Standard slab block crafted from Clockstone Block.
 *
 * Crafting:
 * - 3x Clockstone Block → 6x Clockstone Slab (horizontal row)
 */
public class ClockstoneSlab extends SlabBlock {
    public ClockstoneSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Slab.
     *
     * @return Block properties copied from Clockstone Block
     */
    public static BlockBehaviour.Properties createProperties() {
        return ClockstoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_slab")));
    }
}
