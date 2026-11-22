package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolishedDuskstoneStairs extends StairBlock {
    public PolishedDuskstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.POLISHED_DUSKSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return PolishedDuskstoneBlock.createProperties();
    }
}
