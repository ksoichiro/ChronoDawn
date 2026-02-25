package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Dark Time Wood Stairs - Stair variant of Dark Time Wood Planks.
 *
 * Standard stair block crafted from Dark Time Wood Planks.
 *
 * Crafting:
 * - 6x Dark Time Wood Planks → 4x Dark Time Wood Stairs (stair crafting pattern)
 */
public class DarkTimeWoodStairs extends StairBlock {
    public DarkTimeWoodStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.DARK_TIME_WOOD_PLANKS.get().defaultBlockState(), properties);
    }

    /**
     * Create default properties for Dark Time Wood Stairs.
     *
     * @return Block properties copied from Dark Time Wood Planks
     */
    public static BlockBehaviour.Properties createProperties() {
        return DarkTimeWoodPlanks.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "dark_time_wood_stairs")));
    }
}
