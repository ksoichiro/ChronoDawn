package com.chronodawn.blocks;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Temporal Grass Block - Custom grass block for the ChronoDawn biome.
 *
 * Extends SpreadingSnowyDirtBlock (instead of GrassBlock) to avoid the sealed
 * codec issue in GrassBlock. Spreads between TemporalDirt blocks and reverts
 * to TemporalDirt (not vanilla dirt) when light is blocked.
 */
public class TemporalGrassBlock extends SpreadingSnowyDirtBlock {
    public static final MapCodec<TemporalGrassBlock> CODEC = simpleCodec(TemporalGrassBlock::new);

    public TemporalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends SpreadingSnowyDirtBlock> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
                .setId(ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "temporal_grass_block")));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof HoeItem) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlockAndUpdate(pos, ModBlocks.TEMPORAL_FARMLAND.get().defaultBlockState());
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    /**
     * Returns true if the grass block can remain as grass (light is not blocked above).
     * Mirrors vanilla SpreadingSnowyDirtBlock.canBeGrass logic.
     */
    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (aboveState.getFluidState().getAmount() == 8) {
            return false;
        } else {
            int lightBlock = LightEngine.getLightBlockInto(state, aboveState, Direction.UP, aboveState.getLightBlock());
            return lightBlock < 15;
        }
    }

    /**
     * Returns true if grass can propagate to the given position.
     * Same as canBeGrass but also checks that there is no water above.
     */
    private static boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        return canBeGrass(state, level, pos) && !level.getFluidState(above).is(FluidTags.WATER);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState());
        } else {
            if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
                BlockState grassState = this.defaultBlockState();
                for (int i = 0; i < 4; i++) {
                    BlockPos target = pos.offset(
                            random.nextInt(3) - 1,
                            random.nextInt(5) - 3,
                            random.nextInt(3) - 1);
                    // Only spread to temporal dirt blocks (not vanilla dirt)
                    if (level.getBlockState(target).is(ModBlocks.TEMPORAL_DIRT.get())
                            && canPropagate(grassState, level, target)) {
                        level.setBlockAndUpdate(target, grassState.setValue(SNOWY,
                                isSnowySetting(level.getBlockState(target.above()))));
                    }
                }
            }
        }
    }
}
