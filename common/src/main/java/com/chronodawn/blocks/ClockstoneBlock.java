package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Clockstone Block - Portal frame building material.
 *
 * This block is crafted from Clockstone items and used to construct portal frames
 * for traveling to the ChronoDawn dimension.
 *
 * Properties:
 * - Hardness: 5.0 (harder than stone, similar to iron block)
 * - Blast Resistance: 6.0 (more resistant than stone)
 * - Requires pickaxe to mine
 * - Used as portal frame material
 *
 * Crafting:
 * - 9x Clockstone items → 1x Clockstone Block (3x3 crafting)
 * - Reversible: 1x Clockstone Block → 9x Clockstone items
 *
 * Usage:
 * 1. Mine Clockstone Ore to obtain Clockstone items
 * 2. Craft Clockstone items into Clockstone Blocks
 * 3. Build portal frame (4x5 to 23x23 rectangle)
 * 4. Ignite with Time Hourglass to activate portal
 *
 * Reference: data-model.md (Portal System → Portal Frame)
 * Task: T045 [US1] Portal Frame Validator (requires portal frame block)
 */
public class ClockstoneBlock extends Block {
    public ClockstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Block.
     *
     * @return Block properties with appropriate settings for portal frame material
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)  // Purple color for time-themed block
                .strength(5.0f, 6.0f)              // Harder than stone
                .requiresCorrectToolForDrops()      // Requires pickaxe
                .sound(SoundType.METAL);            // Metallic sound for mystical material
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // When Clockstone frame block is broken, destroy all connected portal blocks
        if (!state.is(newState.getBlock())) {
            destroyConnectedPortal(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /**
     * Destroy all portal blocks connected to this frame position.
     * Uses BFS to find and destroy all portal blocks that form a connected portal.
     *
     * @param level The level
     * @param framePos Position of the broken frame block
     */
    private void destroyConnectedPortal(Level level, BlockPos framePos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        // Start BFS from all 6 adjacent positions
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = framePos.relative(direction);
            if (level.getBlockState(adjacent).is(ModBlocks.CHRONO_DAWN_PORTAL.get())) {
                queue.add(adjacent);
                visited.add(adjacent);
            }
        }

        // BFS to find all connected portal blocks
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // Remove this portal block
            level.removeBlock(current, false);

            // Check all 6 adjacent positions for more portal blocks
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = current.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacent);

                if (adjacentState.is(ModBlocks.CHRONO_DAWN_PORTAL.get()) && !visited.contains(adjacent)) {
                    queue.add(adjacent);
                    visited.add(adjacent);
                }
            }
        }

        if (!visited.isEmpty()) {
            ChronoDawn.LOGGER.info("Destroyed {} connected portal blocks due to frame break at {}", visited.size(), framePos);
        }
    }
}
