package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalKelpBlock extends KelpBlock {
    public static final MapCodec<TemporalKelpBlock> CODEC = simpleCodec(TemporalKelpBlock::new);

    public TemporalKelpBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<KelpBlock> codec() {
        return (MapCodec<KelpBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    protected Block getBodyBlock() {
        return ModBlocks.TEMPORAL_KELP_PLANT.get();
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollision()
            .randomTicks()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
