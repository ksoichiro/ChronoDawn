package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TallTemporalSeagrassBlock extends TallSeagrassBlock {
    public static final MapCodec<TallTemporalSeagrassBlock> CODEC = simpleCodec(TallTemporalSeagrassBlock::new);

    public TallTemporalSeagrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<TallSeagrassBlock> codec() {
        return (MapCodec<TallSeagrassBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties(String id) {
        return BlockBehaviour.Properties.of()
            .noCollision()
            .instabreak()
            .sound(SoundType.WET_GRASS)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, id)));
    }
}
