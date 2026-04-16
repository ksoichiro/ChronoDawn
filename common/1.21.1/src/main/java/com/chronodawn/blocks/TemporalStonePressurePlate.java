package com.chronodawn.blocks;

import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

public class TemporalStonePressurePlate extends PressurePlateBlock {
    public TemporalStonePressurePlate(BlockBehaviour.Properties properties) {
        super(BlockSetType.STONE, properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .noCollission()
                .strength(0.5f)
                .sound(SoundType.STONE);
    }
}
