package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CobbledDeepslateTemporalStoneStairs extends StairBlock {
    public CobbledDeepslateTemporalStoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.COBBLED_DEEPSLATE_TEMPORAL_STONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CobbledDeepslateTemporalStoneBlock.createProperties();
    }
}
