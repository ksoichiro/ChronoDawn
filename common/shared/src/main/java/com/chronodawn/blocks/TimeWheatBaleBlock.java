package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Time Wheat Bale - Decorative storage block for Time Wheat.
 *
 * Features:
 * - Crafted from 9 Time Wheat items
 * - Can be rotated in 3 axes (like logs)
 * - Reduces fall damage by 80% (same as vanilla hay bale)
 * - Can be unpacked back into 9 Time Wheat items
 *
 * Appearance:
 * - Gold/yellow tones for the main body (wheat stems)
 * - Blue accents for the grain parts (matching Time Wood theme)
 *
 * Reference: User request - hay bale equivalent for Time Wheat
 */
public class TimeWheatBaleBlock extends RotatedPillarBlock {

    public TimeWheatBaleBlock(Properties properties) {
        super(properties);
    }

    /**
     * Reduces fall damage when landing on this block.
     * Same behavior as vanilla hay bale - reduces damage by 80%.
     */
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // Reduce fall damage to 20% of original (80% reduction)
        entity.causeFallDamage(fallDistance, 0.2F, level.damageSources().fall());
    }
}
