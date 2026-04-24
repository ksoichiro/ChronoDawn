package com.chronodawn.blocks;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

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

    public TimeTorchBlock(SimpleParticleType particle, BlockBehaviour.Properties properties) {
        super(properties, particle);
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
