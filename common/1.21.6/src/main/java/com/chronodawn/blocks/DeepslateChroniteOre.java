package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

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
        return BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "deepslate_chronite_ore")));
    }
}
