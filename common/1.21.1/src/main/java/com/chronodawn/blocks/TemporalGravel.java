package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalGravel extends FallingBlock {
    public static final MapCodec<TemporalGravel> CODEC = simpleCodec(TemporalGravel::new);

    public TemporalGravel(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.GRAVEL);
    }
}
