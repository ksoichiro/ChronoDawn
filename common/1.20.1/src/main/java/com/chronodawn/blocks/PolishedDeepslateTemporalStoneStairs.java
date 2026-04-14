package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolishedDeepslateTemporalStoneStairs extends StairBlock {
    public PolishedDeepslateTemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.POLISHED_DEEPSLATE_TEMPORAL_STONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return PolishedDeepslateTemporalStoneBlock.createProperties();
    }
}
