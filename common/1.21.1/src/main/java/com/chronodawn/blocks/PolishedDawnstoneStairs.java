package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolishedDawnstoneStairs extends StairBlock {
    public PolishedDawnstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.POLISHED_DAWNSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return PolishedDawnstoneBlock.createProperties();
    }
}
