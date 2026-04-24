package com.chronodawn.blocks;

import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Temporal Lantern - Time-themed lantern with constant light.
 *
 * Properties:
 * - Emits constant light level 13
 * - Breaks instantly
 * - Can be placed on floors, ceilings, or walls
 * - No occlusion (light passes through)
 */
public class TemporalLanternBlock extends LanternBlock {

    public TemporalLanternBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .instabreak()
            .sound(SoundType.LANTERN)
            .lightLevel(state -> 13)
            .noOcclusion();
    }
}
