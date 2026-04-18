package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class TemporalSand extends FallingBlock {
    public static final MapCodec<TemporalSand> CODEC = simpleCodec(TemporalSand::new);

    public TemporalSand(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return MapColor.SAND.col;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SAND)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_sand")));
    }
}
