package com.chronodawn.blocks;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

public class TemporalStoneButton extends ButtonBlock {
    public TemporalStoneButton(BlockBehaviour.Properties properties) {
        super(BlockSetType.STONE, 20, properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .noCollission()
                .strength(0.5f)
                .sound(SoundType.STONE);
    }
}
