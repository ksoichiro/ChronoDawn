package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Dark Time Wood Slab - Slab variant of Dark Time Wood Planks.
 *
 * Standard slab block crafted from Dark Time Wood Planks.
 *
 * Crafting:
 * - 3x Dark Time Wood Planks → 6x Dark Time Wood Slab (horizontal row)
 */
public class DarkTimeWoodSlab extends SlabBlock {
    public DarkTimeWoodSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Dark Time Wood Slab.
     *
     * @return Block properties copied from Dark Time Wood Planks
     */
    public static BlockBehaviour.Properties createProperties() {
        return DarkTimeWoodPlanks.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_slab")));
    }
}
