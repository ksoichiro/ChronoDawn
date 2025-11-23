package com.chronosphere.blocks;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Decorative Water Block
 *
 * A water block that looks and behaves exactly like vanilla water,
 * but has a different block ID to distinguish it from Aquifer water
 * during structure generation.
 *
 * Usage:
 * - Use this block in NBT structure files for decorative water features
 * - During structure generation, Aquifer water (minecraft:water) is removed
 *   to prevent waterlogging, but this block is preserved
 * - A processor converts this block to minecraft:water after placement
 *
 * This approach allows decorative water to coexist with waterlogging prevention.
 */
public class DecorativeWaterBlock extends LiquidBlock {
    public DecorativeWaterBlock(FlowingFluid fluid, Properties properties) {
        // Fluid is passed from ModBlocks registration
        super(fluid, properties);
    }
}
