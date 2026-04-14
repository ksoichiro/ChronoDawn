package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DeepslateTemporalStoneBlock extends Block {
    public DeepslateTemporalStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
