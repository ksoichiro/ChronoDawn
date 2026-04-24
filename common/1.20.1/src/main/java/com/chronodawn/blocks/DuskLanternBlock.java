package com.chronodawn.blocks;

import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Dusk Lantern - Time-themed lantern that's brighter during the night.
 *
 * Properties:
 * - Emits light level 10 (fixed for 1.20.1 - no dynamic time-based lighting)
 * - Breaks instantly
 * - Can be placed on floors, ceilings, or walls
 * - No occlusion (light passes through)
 *
 * Note: In 1.21.1+, this lantern dynamically changes light level based on time of day
 * (bright during night, dim during day). In 1.20.1, it uses a fixed light level.
 */
public class DuskLanternBlock extends LanternBlock {
    private static final int FIXED_LIGHT = 13;

    public DuskLanternBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
            .instabreak()
            .sound(SoundType.LANTERN)
            .lightLevel(state -> FIXED_LIGHT)
            .noOcclusion();
    }
}
