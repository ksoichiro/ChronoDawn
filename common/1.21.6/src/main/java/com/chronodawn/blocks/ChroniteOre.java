package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

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
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
                .strength(3.0f, 3.0f) // hardness, blast resistance
                .requiresCorrectToolForDrops()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "chronite_ore")));
    }
}
