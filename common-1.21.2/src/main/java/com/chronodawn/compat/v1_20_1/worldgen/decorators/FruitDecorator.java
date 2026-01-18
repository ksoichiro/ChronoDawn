package com.chronodawn.compat.v1_20_1.worldgen.decorators;

import com.chronodawn.blocks.FruitOfTimeBlock;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Fruit Decorator - Places Fruit of Time blocks hanging from Time Wood Leaves during tree generation.
 *
 * Similar to natural fruit generation, this decorator:
 * - Randomly places Fruit of Time blocks below Time Wood Leaves blocks
 * - Sets initial age randomly (0-2) for variety
 * - Places fruit with a certain probability (not every leaf gets fruit)
 * - Ensures fruit hangs from valid leaves blocks
 *
 * Behavior:
 * - Probability: 5% chance per leaves block
 * - Placement: Below leaves (hanging downward)
 * - Age: Random initial age (0-2) for natural variation
 * - Requirement: Target block must be air and block above must be Time Wood Leaves
 *
 * Reference: T080q [US1] Implement FruitDecorator class
 * Related: FruitOfTimeBlock (the block being placed)
 */
public class FruitDecorator extends TreeDecorator {
    /**
     * Codec for serialization/deserialization.
     * Codec is used for tree decorator types in Minecraft 1.20.1
     */
    public static final Codec<FruitDecorator> CODEC = Codec.unit(FruitDecorator::new);

    /**
     * Probability of placing fruit on each leaves block (0.0 - 1.0).
     * 0.05 = 5% chance per leaves block.
     */
    private static final float FRUIT_PROBABILITY = 0.05f;

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource random = context.random();

        // Iterate through all leaves positions in the tree
        for (BlockPos leavesPos : context.leaves()) {
            // Check probability: should we place fruit here?
            if (random.nextFloat() >= FRUIT_PROBABILITY) {
                continue;  // Skip this leaves block
            }

            // Calculate position for fruit placement (below the leaves)
            BlockPos fruitPos = leavesPos.below();

            // Check if target position is air (suitable for fruit placement)
            if (context.isAir(fruitPos)) {
                // Random initial age (0-2) for variety
                int age = random.nextInt(FruitOfTimeBlock.MAX_AGE + 1);

                // Place Fruit of Time block hanging from leaves
                context.setBlock(
                    fruitPos,
                    ModBlocks.FRUIT_OF_TIME_BLOCK.get().defaultBlockState()
                        .setValue(FruitOfTimeBlock.AGE, age)
                );
            }
        }
    }

    @Override
    protected TreeDecoratorType<?> type() {
        // Return the registered TreeDecoratorType for JSON datapack support
        return com.chronodawn.registry.ModTreeDecoratorTypes.FRUIT_DECORATOR.get();
    }
}
