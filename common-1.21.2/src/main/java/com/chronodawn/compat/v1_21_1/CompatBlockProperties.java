package com.chronodawn.compat.v1_21_1;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Compatibility wrapper for Block.Properties methods (Minecraft 1.21.1).
 *
 * Provides version-independent methods for creating block properties.
 */
public class CompatBlockProperties {
    /**
     * Create properties by copying from another block (1.21.1 version).
     *
     * @param block Block to copy properties from
     * @return Block properties
     */
    public static BlockBehaviour.Properties ofFullCopy(Block block) {
        return BlockBehaviour.Properties.ofFullCopy(block);
    }

    private CompatBlockProperties() {
        // Utility class - prevent instantiation
    }
}
