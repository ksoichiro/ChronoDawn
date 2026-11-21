package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DawnstoneStairs extends StairBlock {
    public DawnstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.DAWNSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DawnstoneBlock.createProperties();
    }
}
