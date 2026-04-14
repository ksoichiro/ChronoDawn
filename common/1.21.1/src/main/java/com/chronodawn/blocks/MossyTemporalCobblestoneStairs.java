package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MossyTemporalCobblestoneStairs extends StairBlock {
    public MossyTemporalCobblestoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.MOSSY_TEMPORAL_COBBLESTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return MossyTemporalCobblestoneBlock.createProperties();
    }
}
