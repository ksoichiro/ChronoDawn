package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Ancient Time Wood Stairs - Stair variant of Ancient Time Wood Planks.
 *
 * Standard stair block crafted from Ancient Time Wood Planks.
 *
 * Crafting:
 * - 6x Ancient Time Wood Planks → 4x Ancient Time Wood Stairs (stair crafting pattern)
 */
public class AncientTimeWoodStairs extends StairBlock {
    public AncientTimeWoodStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.ANCIENT_TIME_WOOD_PLANKS.get().defaultBlockState(), properties);
    }

    /**
     * Create default properties for Ancient Time Wood Stairs.
     *
     * @return Block properties copied from Ancient Time Wood Planks
     */
    public static BlockBehaviour.Properties createProperties() {
        return AncientTimeWoodPlanks.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "ancient_time_wood_stairs")));
    }
}
