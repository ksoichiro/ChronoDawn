package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TemporalSeagrassBlock extends SeagrassBlock {
    public static final MapCodec<TemporalSeagrassBlock> CODEC = simpleCodec(TemporalSeagrassBlock::new);

    public TemporalSeagrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<SeagrassBlock> codec() {
        return (MapCodec<SeagrassBlock>) (MapCodec<?>) CODEC;
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
