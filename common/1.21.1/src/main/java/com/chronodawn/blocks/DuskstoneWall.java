package com.chronodawn.blocks;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DuskstoneWall extends WallBlock {
    public DuskstoneWall(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return DuskstoneBlock.createProperties();
    }
}
