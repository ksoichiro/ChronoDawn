package com.chronodawn.worldgen.decorators;

import com.chronodawn.registry.ModBlocks;
import com.chronodawn.registry.ModDimensions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/**
 * Turns the vanilla dirt under a sapling-grown trunk into Temporal Dirt, but only inside the
 * ChronoDawn dimension.
 *
 * Why: sapling features place vanilla dirt under the trunk so that saplings grown on vanilla
 * ground do not leak Temporal Dirt into the Overworld. Inside ChronoDawn that dirt looks out of
 * place next to naturally generated trees, which use Temporal Dirt.
 */
public class TemporalRootDirtDecorator extends TreeDecorator {
    public static final MapCodec<TemporalRootDirtDecorator> CODEC =
        MapCodec.unit(TemporalRootDirtDecorator::new);

    @Override
    public void place(TreeDecorator.Context context) {
        if (!(context.level() instanceof WorldGenLevel worldGenLevel)
                || !worldGenLevel.getLevel().dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }
        for (BlockPos logPos : context.logs()) {
            BlockPos below = logPos.below();
            if (context.level().isStateAtPosition(below, state -> state.is(Blocks.DIRT))) {
                context.setBlock(below, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState());
            }
        }
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return com.chronodawn.registry.ModTreeDecoratorTypes.TEMPORAL_ROOT_DIRT_DECORATOR.get();
    }
}
