package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalCobblestoneStairs extends StairBlock {
    public TemporalCobblestoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_COBBLESTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalCobblestoneBlock.createProperties();
    }
}
