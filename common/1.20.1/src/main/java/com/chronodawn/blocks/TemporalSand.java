package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSand extends FallingBlock {
    public TemporalSand(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.SAND);
    }
}
