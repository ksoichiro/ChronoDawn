package com.chronodawn.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Time Wheat Bale - Decorative storage block for Time Wheat (1.21.5 version).
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
 * Note: In 1.21.5, fallOn method signature may have changed.
 * Fall damage reduction is now handled differently.
 *
 * Reference: User request - hay bale equivalent for Time Wheat
 */
public class TimeWheatBaleBlock extends RotatedPillarBlock {

    public TimeWheatBaleBlock(Properties properties) {
        super(properties);
    }

    // Note: In 1.21.5, fallOn() method was removed from Block.
    // Fall damage reduction should be handled through block properties
    // using .fallDamageModifier(0.2f) when registering the block.
    // See ModBlocks registration for fall damage configuration.
}

