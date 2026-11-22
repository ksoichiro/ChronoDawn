package com.chronodawn.blocks;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DuskstoneStairs extends StairBlock {
    public DuskstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.DUSKSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DuskstoneBlock.createProperties();
    }
}
