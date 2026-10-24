package com.chronodawn.worldgen.features;

import com.chronodawn.blocks.WatchmakerChestBlock;
import com.chronodawn.registry.ModBlocks;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Turns the vanilla loot chest of a freshly placed Watchmaker's Camp into a Watchmaker's Chest,
 * unless Lootr is installed. With Lootr the vanilla chest and its loot table stay, so Lootr
 * converts the chest into its own per-player chest.
 *
 * <p>1.20.1: the RandomizableContainer interface does not exist yet, so the loot table is cleared
 * through RandomizableContainerBlockEntity.
 */
public final class WatchmakerCampChests {
    private static final String LOOTR_MOD_ID = "lootr";

    private WatchmakerCampChests() {
        // Utility class
    }

    /**
     * @return whether Lootr is installed. Only its mod id is checked, so no Lootr class is needed.
     */
    public static boolean lootrLoaded() {
        return Platform.isModLoaded(LOOTR_MOD_ID);
    }

    /**
     * Replaces each vanilla chest the template placed with a Watchmaker's Chest facing the same
     * way. Call it after the template has been placed with the same position and settings.
     *
     * @param lootrLoaded whether Lootr is installed. When true nothing is changed
     * @return the number of chests replaced
     */
    public static int swapChests(WorldGenLevel level, StructureTemplate template, BlockPos placePos,
                                 StructurePlaceSettings settings, boolean lootrLoaded) {
        if (lootrLoaded) {
            return 0;
        }
        int swapped = 0;
        for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(placePos, settings, Blocks.CHEST)) {
            BlockPos pos = info.pos();
            BlockState placed = level.getBlockState(pos);
            if (!placed.is(Blocks.CHEST)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            // Removing a chest drops its contents, and reading the contents of a chest with a
            // loot table rolls it first. Clearing the table keeps its loot from spilling.
            if (blockEntity instanceof RandomizableContainerBlockEntity container) {
                container.setLootTable(null, 0L);
            }
            if (blockEntity instanceof Clearable clearable) {
                clearable.clearContent();
            }
            // As vanilla template placement does when it replaces a block entity: a block
            // without one in between removes the old block entity. Without it, a chunk still in
            // generation would keep the chest's block entity at the new block's position.
            level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_INVISIBLE);
            level.setBlock(pos, ModBlocks.WATCHMAKER_CHEST.get().defaultBlockState()
                .setValue(WatchmakerChestBlock.FACING, placed.getValue(ChestBlock.FACING)), Block.UPDATE_CLIENTS);
            swapped++;
        }
        return swapped;
    }
}
