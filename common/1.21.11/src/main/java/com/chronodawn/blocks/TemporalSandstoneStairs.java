package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSandstoneStairs extends StairBlock {
    public TemporalSandstoneStairs(BlockBehaviour.Properties properties) {
        super(ModBlocks.TEMPORAL_SANDSTONE.get().defaultBlockState(), properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return TemporalSandstone.createProperties()
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_sandstone_stairs")));
    }
}
