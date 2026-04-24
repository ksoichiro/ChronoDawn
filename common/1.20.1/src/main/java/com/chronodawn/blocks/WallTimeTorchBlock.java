package com.chronodawn.blocks;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Wall Time Torch - Wall-mounted torch with colored variants.
 *
 * Properties:
 * - Emits light level 12
 * - Breaks instantly
 * - Can be placed on vertical surfaces
 *
 * Variants:
 * - Wall Purple Time Torch
 * - Wall Orange Time Torch
 * - Wall Pink Time Torch
 */
public class WallTimeTorchBlock extends WallTorchBlock {

    public WallTimeTorchBlock(SimpleParticleType particle, BlockBehaviour.Properties properties) {
        super(properties, particle);
    }

    /**
     * Factory method for creating block properties.
     * @param blockId The block ID for setId (e.g., "wall_purple_time_torch")
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
