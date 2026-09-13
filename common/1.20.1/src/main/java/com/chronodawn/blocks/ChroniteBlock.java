package com.chronodawn.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Chronite Block - Storage block for Chronite Shards.
 *
 * Crafted from 9x Chronite Shard items, and can be uncrafted back into shards.
 */
public class ChroniteBlock extends Block {
    public ChroniteBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f, 6.0f) // hardness, blast resistance
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }
}
