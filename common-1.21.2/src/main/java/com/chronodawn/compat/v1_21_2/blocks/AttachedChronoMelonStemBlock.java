package com.chronodawn.compat.v1_21_2.blocks;

import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.world.level.block.AttachedStemBlock;

/**
 * Attached Chrono Melon Stem - Stem block that connects to a grown Chrono Melon.
 *
 * This block appears when a mature Chrono Melon Stem successfully grows a melon
 * on an adjacent block. The stem then "attaches" to the melon and points toward it.
 *
 * Properties:
 * - Has a "facing" property indicating which direction the melon is
 * - Breaks when the attached melon is broken
 * - Drops seeds when broken
 *
 * Visual Theme:
 * - Time-themed attached stem with golden/amber tint (via BlockColor)
 * - Single grayscale texture like vanilla attached stems
 * - Uses same color as mature stem (golden-amber)
 *
 * Note:
 * - Extends AttachedStemBlock for vanilla-compatible behavior
 * - Uses BlockColor for tinting (registered client-side)
 * - Paired with ChronoMelonStemBlock
 *
 * Reference: WORK_NOTES.md (Crop 2: Chrono Melon)
 */
public class AttachedChronoMelonStemBlock extends AttachedStemBlock {

    public AttachedChronoMelonStemBlock(Properties properties) {
        super(
            ModBlocks.CHRONO_MELON.getKey(),          // Fruit block (first parameter)
            ModBlocks.CHRONO_MELON_STEM.getKey(),     // Growing stem block (second parameter)
            ModItems.CHRONO_MELON_SEEDS.getKey(),     // Seed item (third parameter)
            properties
        );
    }
}
