package com.chronosphere.blocks;

import com.chronosphere.registry.ModBlocks;
import net.minecraft.world.level.block.Block;

/**
 * Chrono Melon - Full melon block that drops slices when broken.
 *
 * Properties:
 * - Full block (16x16x16)
 * - Grown by Chrono Melon Stems
 * - Drops 3-7 Chrono Melon Slices when broken (without Silk Touch)
 * - Fortune enchantment increases drop count
 * - Silk Touch drops the full block
 *
 * Visual Theme:
 * - Time-themed melon with golden/amber appearance
 * - Similar to vanilla melon but with chronosphere aesthetic
 *
 * Crafting:
 * - 9x Chrono Melon Slices → 1x Chrono Melon Block
 * - 1x Chrono Melon Block → 9x Chrono Melon Slices
 *
 * Note:
 * - Drops are handled by loot table (chrono_melon.json)
 * - No special logic needed beyond standard Block class
 *
 * Reference: WORK_NOTES.md (Crop 2: Chrono Melon)
 * Task: T212 [US1] Create Chrono Melon block
 */
public class ChronoMelonBlock extends Block {
    public ChronoMelonBlock(Properties properties) {
        super(properties);
    }
}

