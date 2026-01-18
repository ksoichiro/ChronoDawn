package com.chronodawn.compat.v1_20_1.blocks;

import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModItems;
import net.minecraft.world.level.block.StemBlock;

/**
 * Chrono Melon Stem - Stem block that grows Chrono Melons.
 *
 * Growth Stages:
 * - 8 stages (0-7) like vanilla melon/pumpkin stems
 * - Stage 7 is fully mature and can produce melons
 *
 * Properties:
 * - Must be planted on farmland
 * - Requires light level 9+ to grow
 * - Random tick growth like vanilla stems
 * - When mature, attempts to place melon on adjacent blocks
 *
 * Drops:
 * - Drops seeds when broken (configured in loot table)
 *
 * Visual Theme:
 * - Time-themed stem with golden/amber tint (via BlockColor)
 * - Single grayscale texture like vanilla melon/pumpkin stems
 * - Color changes from green to golden-amber as it matures
 *
 * Note:
 * - Extends StemBlock for vanilla-compatible behavior
 * - Uses BlockColor for tinting (registered client-side)
 * - Shares AGE property with vanilla stems (BlockStateProperties.AGE_7)
 *
 * Reference: WORK_NOTES.md (Crop 2: Chrono Melon)
 * Task: T212 [US1] Create Chrono Melon Stem block
 */
public class ChronoMelonStemBlock extends StemBlock {

    public ChronoMelonStemBlock(Properties properties) {
        super(
            (net.minecraft.world.level.block.StemGrownBlock) ModBlocks.CHRONO_MELON.get(),  // Fruit block instance (now implements StemGrownBlock)
            ModItems.CHRONO_MELON_SEEDS,  // Seed item supplier
            properties
        );
    }
}
