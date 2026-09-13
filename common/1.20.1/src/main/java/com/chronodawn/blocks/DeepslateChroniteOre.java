package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Deepslate Chronite Ore - deepslate variant of Chronite Ore.
 *
 * Softer than Deepslate Clockstone Ore and mineable with a stone pickaxe, because an
 * early player must be able to reach the Time Compass reliably.
 */
public class DeepslateChroniteOre extends Block {
    public DeepslateChroniteOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
    }
}
