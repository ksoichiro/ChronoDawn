package com.chronodawn.worldgen.features;

import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Clears the loot table of a block entity, such as a placed loot chest.
 *
 * <p>1.20.1 has its own copy of this class: the RandomizableContainer interface it uses here
 * does not exist before 1.20.3.
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
        if (blockEntity instanceof RandomizableContainer container) {
            container.setLootTable(null);
        }
    }
}
