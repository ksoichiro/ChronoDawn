package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstoneStairs extends StairBlock {
    public TemporalSandstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_SANDSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalSandstone.createProperties();
    }
}
