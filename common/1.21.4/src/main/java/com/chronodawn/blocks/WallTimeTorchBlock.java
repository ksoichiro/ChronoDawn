package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class WallTimeTorchBlock extends WallTorchBlock {
    public static final MapCodec<WallTimeTorchBlock> CODEC = simpleCodec(
        props -> new WallTimeTorchBlock(() -> ParticleTypes.FLAME, props)
    );

    // TorchBlock requires its flame particle at construction time, but mod particle
    // types are not registered yet when blocks are created (NeoForge fires the BLOCK
    // RegisterEvent before PARTICLE_TYPE), so the colored flame is resolved lazily here.
    private final Supplier<? extends SimpleParticleType> flame;

    public WallTimeTorchBlock(Supplier<? extends SimpleParticleType> flame, BlockBehaviour.Properties properties) {
        super(ParticleTypes.FLAME, properties);
        this.flame = flame;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction opposite = state.getValue(FACING).getOpposite();
        double x = pos.getX() + 0.5 + 0.27 * opposite.getStepX();
        double y = pos.getY() + 0.7 + 0.22;
        double z = pos.getZ() + 0.5 + 0.27 * opposite.getStepZ();
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(flame.get(), x, y, z, 0.0, 0.0, 0.0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MapCodec<WallTorchBlock> codec() {
        return (MapCodec<WallTorchBlock>) (MapCodec<?>) CODEC;
    }

    public static BlockBehaviour.Properties createProperties(String blockId) {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .lightLevel(state -> 12)
            .sound(SoundType.WOOD)
            .setId(ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, blockId)));
    }
}
