package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MossyTemporalStoneBricksStairs extends StairBlock {
    public MossyTemporalStoneBricksStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.MOSSY_TEMPORAL_STONE_BRICKS.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return MossyTemporalStoneBricksBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "mossy_temporal_stone_bricks_stairs")));
    }
}
