package com.chronodawn.blocks;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DeepslateTemporalAmberOre extends DropExperienceBlock {
    public DeepslateTemporalAmberOre(BlockBehaviour.Properties properties) {
        super(UniformInt.of(2, 5), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
