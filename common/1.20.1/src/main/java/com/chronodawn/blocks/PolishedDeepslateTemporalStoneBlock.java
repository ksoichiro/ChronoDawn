package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class PolishedDeepslateTemporalStoneBlock extends Block {
    public PolishedDeepslateTemporalStoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(3.5f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
