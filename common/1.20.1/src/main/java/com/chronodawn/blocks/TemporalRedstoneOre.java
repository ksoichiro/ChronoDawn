package com.chronodawn.blocks;

import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class TemporalRedstoneOre extends RedStoneOreBlock {
    public TemporalRedstoneOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .randomTicks()
                .lightLevel(state -> state.getValue(RedStoneOreBlock.LIT) ? 9 : 0)
                .isRedstoneConductor((state, getter, pos) -> false);
    }
}
