package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import com.chronodawn.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Coarse Temporal Dirt Block - Coarse variant of temporal dirt for the ChronoDawn biome.
 *
 * Behaves like vanilla coarse dirt, but when tilled with a hoe it converts to
 * TemporalDirt (not vanilla dirt), preserving the temporal biome theme.
 */
public class CoarseTemporalDirtBlock extends Block {
    public static final MapCodec<CoarseTemporalDirtBlock> CODEC = simpleCodec(CoarseTemporalDirtBlock::new);

    public CoarseTemporalDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.COARSE_DIRT);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // useItemOn does not exist in this version; check main hand for a hoe
        net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof HoeItem) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlockAndUpdate(pos, ModBlocks.TEMPORAL_DIRT.get().defaultBlockState());
                stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
