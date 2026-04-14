package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolishedDeepslateTemporalStoneStairs extends StairBlock {
    public PolishedDeepslateTemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.POLISHED_DEEPSLATE_TEMPORAL_STONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return PolishedDeepslateTemporalStoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "polished_deepslate_temporal_stone_stairs")));
    }
}
