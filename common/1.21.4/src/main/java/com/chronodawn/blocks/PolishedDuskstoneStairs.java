package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PolishedDuskstoneStairs extends StairBlock {
    public PolishedDuskstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.POLISHED_DUSKSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return PolishedDuskstoneBlock.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "polished_duskstone_stairs")));
    }
}
