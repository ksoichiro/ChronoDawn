package com.chronodawn.blocks;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Time Torch - Floor-mounted torch with colored variants.
 *
 * Properties:
 * - Emits light level 12
 * - Breaks instantly
 * - Can be placed on any solid block
 *
 * Variants:
 * - Purple Time Torch
 * - Orange Time Torch
 * - Pink Time Torch
 */
public class TimeTorchBlock extends TorchBlock {

    // TorchBlock requires its flame particle at construction time, but mod particle
    // types are not registered yet when blocks are created (NeoForge fires the BLOCK
    // RegisterEvent before PARTICLE_TYPE), so the colored flame is resolved lazily here.
    private final Supplier<? extends SimpleParticleType> flame;

    public TimeTorchBlock(Supplier<? extends SimpleParticleType> flame, BlockBehaviour.Properties properties) {
        super(properties, ParticleTypes.FLAME);
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

    /**
     * Factory method for creating block properties.
     * @param blockId The block ID for setId (e.g., "purple_time_torch")
     */
    public static BlockBehaviour.Properties createProperties(String blockId) {
        return BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .instabreak()
            .lightLevel(state -> 12)
            .sound(SoundType.WOOD);
    }
}
