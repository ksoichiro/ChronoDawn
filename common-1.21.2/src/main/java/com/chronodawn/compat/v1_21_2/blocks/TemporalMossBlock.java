package com.chronodawn.compat.v1_21_2.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Temporal Moss - Decorative moss block exclusive to swamp biome.
 *
 * This block serves as a decorative ground cover in ChronoDawn swamp biomes.
 * It can spread to adjacent blocks similar to vanilla moss, adding visual variety
 * to swamp terrain.
 *
 * Properties:
 * - Hardness: 0.1 (very soft, like vanilla moss)
 * - Blast Resistance: 0.1 (fragile)
 * - Tool: Can be mined with any tool or by hand (hoe is fastest)
 * - Moss sound (same as vanilla moss carpet)
 * - Spreads to adjacent dirt/stone blocks over time
 *
 * Generation:
 * - Exclusive to chronodawn_swamp biome
 * - Generates as patches on the ground
 *
 * Behavior:
 * - Can spread to adjacent dirt, stone, or similar blocks
 * - Spreading triggered by random tick
 * - Does not require light to spread (works in dark swamps)
 *
 * Visual:
 * - Moss-like texture with time/temporal theme
 * - Green color with subtle purple/blue tint
 *
 * Task: T244 [P] [US1] Create Temporal Moss block
 */
public class TemporalMossBlock extends Block implements BonemealableBlock {
    public TemporalMossBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Temporal Moss.
     *
     * @return Block properties with appropriate settings for moss block
     */
    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)      // Green color for moss
                .strength(0.1f, 0.1f)                // Very soft, fragile
                .sound(SoundType.MOSS_CARPET)         // Moss sound (same as vanilla moss)
                .randomTicks();                       // Enable random tick for spreading
    }

    /**
     * Random tick behavior for moss spreading.
     * Attempts to spread to adjacent blocks.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 10% chance to spread on each random tick
        if (random.nextInt(10) == 0) {
            // Try to spread to a random adjacent block
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos targetPos = pos.relative(direction);
            BlockState targetState = level.getBlockState(targetPos);

            // Can spread to dirt, stone, or similar natural blocks
            if (canSpreadTo(targetState)) {
                level.setBlockAndUpdate(targetPos, this.defaultBlockState());
            }
        }
    }

    /**
     * Check if moss can spread to the target block.
     */
    private boolean canSpreadTo(BlockState state) {
        // Can spread to dirt, stone, and similar natural blocks
        Block block = state.getBlock();
        String blockId = block.toString();

        return blockId.contains("dirt") ||
               blockId.contains("stone") ||
               blockId.contains("grass_block");
    }

    /**
     * Bonemeal can be used to spread moss faster.
     */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // Spread to multiple adjacent blocks when bonemealed
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos targetPos = pos.relative(direction);
            BlockState targetState = level.getBlockState(targetPos);

            if (canSpreadTo(targetState) && random.nextBoolean()) {
                level.setBlockAndUpdate(targetPos, this.defaultBlockState());
            }
        }
    }
}
