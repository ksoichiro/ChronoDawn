package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalStoneBricksStairs extends StairBlock {
    public TemporalStoneBricksStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_STONE_BRICKS.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalStoneBricksBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_stone_bricks_stairs")));
    }
}
