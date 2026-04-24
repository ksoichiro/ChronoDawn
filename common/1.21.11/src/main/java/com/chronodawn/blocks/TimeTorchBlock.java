package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TimeTorchBlock extends TorchBlock {
    public static final MapCodec<TimeTorchBlock> CODEC = simpleCodec(
        props -> new TimeTorchBlock(ParticleTypes.FLAME, props)
    );

    public TimeTorchBlock(SimpleParticleType particle, BlockBehaviour.Properties properties) {
        super(particle, properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<TorchBlock> codec() {
        return (MapCodec<TorchBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties(String blockId) {
        return BlockBehaviour.Properties.of()
            .noCollision()
            .noOcclusion()
            .instabreak()
            .lightLevel(state -> 12)
            .sound(SoundType.WOOD)
            .setId(ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, blockId)));
    }
}
