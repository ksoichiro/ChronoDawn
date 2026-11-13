package com.chronodawn.worldgen.features;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

/**
 * Clears the loot table of a block entity, such as a placed loot chest.
 *
 * <p>1.20.1: the RandomizableContainer interface does not exist yet, so the loot table is cleared
 * through RandomizableContainerBlockEntity.
 */
final class LootTableClearer {
    private LootTableClearer() {
        // Utility class
    }

    /**
     * Removes the loot table of the block entity if it has one, so its loot is not rolled when the
     * block entity is read or removed.
     */
    static void clear(BlockEntity blockEntity) {
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(null, 0L);
        }
    }
}
