package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class TimeTorchBlock extends TorchBlock {

    // TorchBlock requires its flame particle at construction time, but mod particle
    // types are not registered yet when blocks are created (NeoForge fires the BLOCK
    // RegisterEvent before PARTICLE_TYPE), so the colored flame is resolved lazily here.
    private final Supplier<? extends SimpleParticleType> flame;

    public TimeTorchBlock(Supplier<? extends SimpleParticleType> flame, BlockBehaviour.Properties properties) {
        super(ParticleTypes.FLAME, properties);
        this.flame = flame;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(flame.get(), x, y, z, 0.0, 0.0, 0.0);
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
