package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Parched Temporal Dirt Block - Cracked dry dirt for the Faded Plains biome.
 *
 * Acts as the surface and disk-feature material in chronodawn_faded_plains.
 * No special interaction; shoveled like vanilla coarse dirt.
 */
public class ParchedTemporalDirtBlock extends Block {
    public static final MapCodec<ParchedTemporalDirtBlock> CODEC = simpleCodec(ParchedTemporalDirtBlock::new);

    public ParchedTemporalDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT)
                .strength(0.5f)
                .setId(ResourceKey.create(Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "parched_temporal_dirt")));
    }
}
