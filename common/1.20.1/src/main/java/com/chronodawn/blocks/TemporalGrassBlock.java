package com.chronodawn.blocks;

import com.chronodawn.compat.CompatBlockProperties;
import com.chronodawn.registry.ModBlocks;
import com.chronodawn.worldgen.TemporalBonemealFeatures;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Temporal Grass Block - Custom grass block for the ChronoDawn biome.
 *
 * Extends SpreadingSnowyDirtBlock to inherit snowy dirt spreading behaviour.
 * Spreads between TemporalDirt blocks and reverts to TemporalDirt (not vanilla
 * dirt) when light is blocked.
 */
public class TemporalGrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {
    public TemporalGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties createProperties() {
        return CompatBlockProperties.ofFullCopy(Blocks.GRASS_BLOCK);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof HoeItem) {
            if (!level.isClientSide()) {
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlockAndUpdate(pos, ModBlocks.TEMPORAL_FARMLAND.get().defaultBlockState());
                player.getItemInHand(hand).hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    /**
     * Returns true if the grass block can remain as grass (light is not blocked above).
     */
    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (aboveState.getFluidState().getAmount() == 8) {
            return false;
        } else {
            // Use sky light level check as a proxy for light blocking
            return level.getRawBrightness(above, 0) >= 1;
        }
    }

    /**
     * Returns true if grass can propagate to the given position.
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
                                level.getBlockState(target.above()).is(Blocks.SNOW)));
                    }
                }
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClientSide) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos above = pos.above();
        BlockState grass = Blocks.GRASS.defaultBlockState();
        bonemealAttempts:
        for (int attempt = 0; attempt < 128; attempt++) {
            BlockPos testPos = above;
            for (int i = 0; i < attempt / 16; i++) {
                testPos = testPos.offset(random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1);
                if (!level.getBlockState(testPos.below()).is(this)
                        || level.getBlockState(testPos).isCollisionShapeFullBlock(level, testPos)) {
                    continue bonemealAttempts;
                }
            }

            BlockState testState = level.getBlockState(testPos);
            if (testState.is(grass.getBlock()) && random.nextInt(10) == 0) {
                ((BonemealableBlock) grass.getBlock()).performBonemeal(level, random, testPos, testState);
            }

            if (testState.isAir()) {
                Holder<PlacedFeature> feature;
                if (random.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> features = level.getBiome(testPos).value()
                            .getGenerationSettings().getFlowerFeatures();
                    if (features.isEmpty()) {
                        continue;
                    }
                    feature = ((RandomPatchConfiguration) features.get(0).config()).feature();
                } else {
                    var selectedFeature = TemporalBonemealFeatures.select(level.getBiome(testPos), random);
                    Optional<Holder.Reference<PlacedFeature>> groundCoverFeature = level.registryAccess()
                            .registry(Registries.PLACED_FEATURE)
                            .flatMap(registry -> registry.getHolder(selectedFeature));
                    if (groundCoverFeature.isEmpty()) {
                        continue;
                    }
                    feature = groundCoverFeature.get();
                }

                feature.value().place(level, level.getChunkSource().getGenerator(), random, testPos);
            }
        }
    }
}
