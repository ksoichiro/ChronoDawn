package com.chronosphere.worldgen.decorators;

import com.chronosphere.blocks.FruitOfTimeBlock;
import com.chronosphere.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Fruit Decorator - Places Fruit of Time blocks on Time Wood Logs during tree generation.
 *
 * Similar to Minecraft's CocoaDecorator, this decorator:
 * - Randomly places Fruit of Time blocks on the sides of Time Wood Log blocks
 * - Sets initial age randomly (0-2) for variety
 * - Places fruit with a certain probability (not every log gets fruit)
 * - Ensures fruit is attached to valid log blocks with proper facing direction
 *
 * Behavior:
 * - Probability: 20% chance per log block
 * - Placement: On horizontal sides (NORTH, SOUTH, EAST, WEST) only
 * - Age: Random initial age (0-2) for natural variation
 * - Requirement: Target block must be air and adjacent block must be Time Wood Log
 *
 * Reference: T080q [US1] Implement FruitDecorator class
 * Related: FruitOfTimeBlock (the block being placed)
 */
public class FruitDecorator extends TreeDecorator {
    /**
     * Codec for serialization/deserialization.
     * MapCodec is used for tree decorator types in Minecraft 1.21.1+
     */
    public static final MapCodec<FruitDecorator> CODEC = MapCodec.unit(FruitDecorator::new);

    /**
     * Probability of placing fruit on each log block (0.0 - 1.0).
     * 0.2 = 20% chance per log.
     */
    private static final float FRUIT_PROBABILITY = 0.2f;

    /**
     * All horizontal directions for fruit placement.
     */
    private static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource random = context.random();

        // Iterate through all log positions in the tree
        for (BlockPos logPos : context.logs()) {
            // Check probability: should we place fruit here?
            if (random.nextFloat() >= FRUIT_PROBABILITY) {
                continue;  // Skip this log
            }

            // Try all horizontal directions
            for (Direction direction : HORIZONTAL_DIRECTIONS) {
                // Calculate position for fruit placement (offset from log)
                BlockPos fruitPos = logPos.relative(direction);

                // Check if target position is air (suitable for fruit placement)
                if (context.isAir(fruitPos)) {
                    // Random initial age (0-2) for variety
                    int age = random.nextInt(FruitOfTimeBlock.MAX_AGE + 1);

                    // Place Fruit of Time block with facing direction and age
                    context.setBlock(
                        fruitPos,
                        ModBlocks.FRUIT_OF_TIME_BLOCK.get().defaultBlockState()
                            .setValue(FruitOfTimeBlock.FACING, direction)
                            .setValue(FruitOfTimeBlock.AGE, age)
                    );

                    // Only place one fruit per log (don't try other directions)
                    break;
                }
            }
        }
    }

    @Override
    protected TreeDecoratorType<?> type() {
        // Return the registered TreeDecoratorType for JSON datapack support
        return com.chronosphere.registry.ModTreeDecoratorTypes.FRUIT_DECORATOR;
    }
}
