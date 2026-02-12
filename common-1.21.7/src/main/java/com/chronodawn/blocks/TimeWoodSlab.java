package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Time Wood Slab - Slab variant of Time Wood Planks.
 *
 * Standard slab block crafted from Time Wood Planks.
 *
 * Crafting:
 * - 3x Time Wood Planks → 6x Time Wood Slab (horizontal row)
 *
 * Task: T243 [P] [US1] Create stairs/slabs/walls/fences variants for Time Wood Planks
 */
public class TimeWoodSlab extends SlabBlock {
    public TimeWoodSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Wood Slab.
     *
     * @return Block properties copied from Time Wood Planks
     */
    public static BlockBehaviour.Properties createProperties() {
        return TimeWoodPlanks.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_wood_slab")));
    }
}
