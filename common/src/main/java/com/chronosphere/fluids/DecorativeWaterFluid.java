package com.chronosphere.fluids;

import com.chronosphere.registry.ModBlocks;
import com.chronosphere.registry.ModFluids;
import com.chronosphere.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Decorative Water Fluid
 *
 * A custom fluid that looks and behaves exactly like vanilla water,
 * but has a different fluid ID to distinguish it from Aquifer water
 * during structure generation.
 *
 * Usage:
 * - Use this fluid in NBT structure files for decorative water features
 * - During structure generation, Aquifer water (minecraft:water) is removed
 *   to prevent waterlogging, but this fluid is preserved
 * - A processor converts this fluid to minecraft:water after placement
 */
public abstract class DecorativeWaterFluid extends FlowingFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.DECORATIVE_WATER_FLOWING.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.DECORATIVE_WATER.get();
    }

    @Override
    public Item getBucket() {
        return ModItems.DECORATIVE_WATER_BUCKET.get();
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        // Behave like water - can create infinite sources
        return true;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        // Same as water - drop block entity contents if any
        BlockEntity blockentity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockentity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        // Same as water - spreads 4 blocks horizontally
        return 4;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        // Same as water - decreases by 1 per block
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        // Same as water - 5 ticks between updates
        return 5;
    }

    @Override
    protected float getExplosionResistance() {
        // Same as water
        return 100.0F;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return ModBlocks.DECORATIVE_WATER.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        // Only same if it's our decorative water (not vanilla water)
        return fluid == ModFluids.DECORATIVE_WATER.get() || fluid == ModFluids.DECORATIVE_WATER_FLOWING.get();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos pos, Fluid fluid, Direction direction) {
        // Same as water - can be replaced by any fluid flowing downward
        return direction == Direction.DOWN && !isSame(fluid);
    }

    /**
     * Source (still) water
     */
    public static class Source extends DecorativeWaterFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8; // Full source block
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    /**
     * Flowing water
     */
    public static class Flowing extends DecorativeWaterFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
