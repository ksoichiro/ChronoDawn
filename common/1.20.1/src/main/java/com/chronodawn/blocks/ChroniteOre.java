package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Chronite Ore - Overworld ore that drops Chronite Shards.
 *
 * Softer than Clockstone Ore and mineable with a stone pickaxe, because an
 * early player must be able to reach the Time Compass reliably.
 */
public class ChroniteOre extends Block {
    public ChroniteOre(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f) // hardness, blast resistance
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
