package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DeepslateTemporalStoneStairs extends StairBlock {
    public DeepslateTemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.DEEPSLATE_TEMPORAL_STONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DeepslateTemporalStoneBlock.createProperties();
    }
}
