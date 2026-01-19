package com.chronodawn.items.consumables;

import com.chronodawn.registry.ModBlocks;
import net.minecraft.world.item.BlockItem;

/**
 * Chrono Melon Seeds - Seeds for planting Chrono Melon Stems.
 *
 * Properties:
 * - Plantable on farmland
 * - Grows into Chrono Melon Stem
 * - Not edible (seeds only)
 *
 * Acquisition:
 * - Crafting: 1x Chrono Melon Slice → 1x Chrono Melon Seeds
 * - Cannot be obtained from breaking stems (only from slices)
 *
 * Usage:
 * - Plant on farmland to grow Chrono Melon Stems
 * - Stem will eventually grow melons on adjacent blocks
 *
 * Note:
 * - Similar to vanilla melon seeds
 * - Extends BlockItem with useBlockDescriptionPrefix (replaces ItemNameBlockItem in 1.21.2)
 *
 * Reference: WORK_NOTES.md (Crop 2: Chrono Melon)
 * Task: T212 [US1] Create Chrono Melon Seeds item
 */
public class ChronoMelonSeedsItem extends BlockItem {
    public ChronoMelonSeedsItem(Properties properties) {
        super(ModBlocks.CHRONO_MELON_STEM.get(), properties.useBlockDescriptionPrefix());
    }

    /**
     * Create default properties for Chrono Melon Seeds.
     *
     * @return Item properties (no food, just plantable)
     */
    public static Properties createProperties() {
        return new Properties();
    }
}
