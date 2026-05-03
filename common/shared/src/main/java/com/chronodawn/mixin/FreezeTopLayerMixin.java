package com.chronodawn.mixin;

import com.chronodawn.registry.ModDimensions;
import com.chronodawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.block.SnowyDirtBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowAndFreezeFeature.class)
public abstract class FreezeTopLayerMixin {

    private static final int TRANSITION_RADIUS = 8;
    private static final int SURFACE_BLEND_OUTER_RADIUS = 4;
    private static final int SAMPLE_STEP = 2;
    private static final int COARSE_SAMPLE_STEP = 8;
    private static final int SAMPLE_PADDING = TRANSITION_RADIUS * 2;
    private static final int SAMPLE_AREA_SIZE = 16 + SAMPLE_PADDING * 2;
    private static final int CHRONO_DAWN_MIN_Y = -64;
    private static final float MAX_ADD_PROBABILITY = 0.7f;
    private static final float MAX_REMOVE_PROBABILITY = 0.8f;
    private static final float MAX_SURFACE_BLEND_PROBABILITY = 0.9f;

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void chronodawn$skipUnneededFreezeTopLayer(
            FeaturePlaceContext<NoneFeatureConfiguration> context,
            CallbackInfoReturnable<Boolean> cir) {
        WorldGenLevel level = context.level();

        if (!level.getLevel().dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        BlockPos origin = context.origin();
        int startX = origin.getX() & ~0xF;
        int startZ = origin.getZ() & ~0xF;
        ServerChunkCache chunkSource = level.getLevel().getChunkSource();
        BiomeSource biomeSource = chunkSource.getGenerator().getBiomeSource();
        Climate.Sampler climateSampler = chunkSource.randomState().sampler();

        if (!hasSnowyBiomeNearby(biomeSource, climateSampler, startX, startZ)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void chronodawn$extendSnowTransition(
            FeaturePlaceContext<NoneFeatureConfiguration> context,
            CallbackInfoReturnable<Boolean> cir) {
        WorldGenLevel level = context.level();

        if (!level.getLevel().dimension().equals(ModDimensions.CHRONO_DAWN_DIMENSION)) {
            return;
        }

        BlockPos origin = context.origin();
        int startX = origin.getX() & ~0xF;
        int startZ = origin.getZ() & ~0xF;
        ServerChunkCache chunkSource = level.getLevel().getChunkSource();
        BiomeSource biomeSource = chunkSource.getGenerator().getBiomeSource();
        Climate.Sampler climateSampler = chunkSource.randomState().sampler();

        if (!hasSnowyBoundaryNearby(biomeSource, climateSampler, startX, startZ)) {
            return;
        }

        boolean[][] snowySamples = new boolean[SAMPLE_AREA_SIZE][SAMPLE_AREA_SIZE];
        boolean[][] precipitationSamples = new boolean[SAMPLE_AREA_SIZE][SAMPLE_AREA_SIZE];
        boolean hasSnowyBiome = false;
        boolean hasWarmBiome = false;

        for (int sampleX = 0; sampleX < SAMPLE_AREA_SIZE; sampleX++) {
            for (int sampleZ = 0; sampleZ < SAMPLE_AREA_SIZE; sampleZ++) {
                Biome biome = getNoiseBiome(biomeSource, climateSampler,
                        startX + sampleX - SAMPLE_PADDING,
                        startZ + sampleZ - SAMPLE_PADDING);
                boolean snowy = biome.getBaseTemperature() < 0.15f;
                snowySamples[sampleX][sampleZ] = snowy;
                precipitationSamples[sampleX][sampleZ] = biome.hasPrecipitation();
                hasSnowyBiome |= snowy;
                hasWarmBiome |= !snowy;
            }
        }

        if (!hasSnowyBiome || !hasWarmBiome) {
            return;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;
                int sampleX = localX + SAMPLE_PADDING;
                int sampleZ = localZ + SAMPLE_PADDING;

                if (snowySamples[sampleX][sampleZ]) {
                    continue;
                }

                if (!precipitationSamples[sampleX][sampleZ]) {
                    continue;
                }

                int snowyCount = 0;
                int minDist = TRANSITION_RADIUS + 1;

                for (int dx = -TRANSITION_RADIUS; dx <= TRANSITION_RADIUS; dx += SAMPLE_STEP) {
                    for (int dz = -TRANSITION_RADIUS; dz <= TRANSITION_RADIUS; dz += SAMPLE_STEP) {
                        if (snowySamples[sampleX + dx][sampleZ + dz]) {
                            snowyCount++;
                            int dist = Math.max(Math.abs(dx), Math.abs(dz));
                            minDist = Math.min(minDist, dist);
                        }
                    }
                }

                if (snowyCount == 0) {
                    continue;
                }

                int totalSamples = ((TRANSITION_RADIUS * 2) / SAMPLE_STEP + 1)
                        * ((TRANSITION_RADIUS * 2) / SAMPLE_STEP + 1);
                float snowyRatio = (float) snowyCount / totalSamples;
                float distFactor = Math.max(0.0f, 1.0f - ((float) (minDist - 1) / TRANSITION_RADIUS));
                float probability = MAX_ADD_PROBABILITY * snowyRatio * distFactor;

                if (positionHash(worldX, worldZ) >= probability) {
                    continue;
                }

                int snowY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
                mutable.set(worldX, snowY, worldZ);
                if (level.getBlockState(mutable).isAir()) {
                    mutable.setY(snowY - 1);
                    BlockState belowState = level.getBlockState(mutable);
                    if (belowState.isFaceSturdy(level, mutable, Direction.UP)) {
                        mutable.setY(snowY);
                        level.setBlock(mutable, Blocks.SNOW.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Pass 2: Remove snow from snowy biome columns near non-snowy boundaries
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;
                int sampleX = localX + SAMPLE_PADDING;
                int sampleZ = localZ + SAMPLE_PADDING;

                if (!snowySamples[sampleX][sampleZ]) {
                    continue;
                }

                int nonSnowyCount = 0;
                int minDist = TRANSITION_RADIUS + 1;

                for (int dx = -TRANSITION_RADIUS; dx <= TRANSITION_RADIUS; dx += SAMPLE_STEP) {
                    for (int dz = -TRANSITION_RADIUS; dz <= TRANSITION_RADIUS; dz += SAMPLE_STEP) {
                        if (dx == 0 && dz == 0) continue;
                        if (!snowySamples[sampleX + dx][sampleZ + dz]) {
                            nonSnowyCount++;
                            int dist = Math.max(Math.abs(dx), Math.abs(dz));
                            minDist = Math.min(minDist, dist);
                        }
                    }
                }

                if (nonSnowyCount == 0) {
                    continue;
                }

                int totalSamples = ((TRANSITION_RADIUS * 2) / SAMPLE_STEP + 1)
                        * ((TRANSITION_RADIUS * 2) / SAMPLE_STEP + 1) - 1;
                float nonSnowyRatio = (float) nonSnowyCount / totalSamples;
                float distFactor = Math.max(0.0f, 1.0f - ((float) (minDist - 1) / TRANSITION_RADIUS));
                float removeProbability = MAX_REMOVE_PROBABILITY * nonSnowyRatio * distFactor;

                // Offset hash to avoid correlation with Pass 1
                if (positionHash(worldX + 32768, worldZ + 32768) >= removeProbability) {
                    continue;
                }

                int snowY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
                mutable.set(worldX, snowY, worldZ);
                if (level.getBlockState(mutable).is(Blocks.SNOW)) {
                    level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                } else {
                    mutable.setY(snowY - 1);
                    if (level.getBlockState(mutable).is(Blocks.SNOW)) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        blendSurfaceBlocks(level, startX, startZ, snowySamples, mutable);
    }

    private static Biome getNoiseBiome(BiomeSource biomeSource, Climate.Sampler climateSampler, int x, int z) {
        return biomeSource.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(0), QuartPos.fromBlock(z),
                climateSampler).value();
    }

    private static boolean hasSnowyBoundaryNearby(BiomeSource biomeSource, Climate.Sampler climateSampler,
            int startX, int startZ) {
        boolean hasSnowyBiome = false;
        boolean hasWarmBiome = false;

        for (int sampleX = 0; sampleX < SAMPLE_AREA_SIZE; sampleX += COARSE_SAMPLE_STEP) {
            for (int sampleZ = 0; sampleZ < SAMPLE_AREA_SIZE; sampleZ += COARSE_SAMPLE_STEP) {
                Biome biome = getNoiseBiome(biomeSource, climateSampler,
                        startX + sampleX - SAMPLE_PADDING,
                        startZ + sampleZ - SAMPLE_PADDING);
                if (biome.getBaseTemperature() < 0.15f) {
                    hasSnowyBiome = true;
                } else {
                    hasWarmBiome = true;
                }

                if (hasSnowyBiome && hasWarmBiome) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasSnowyBiomeNearby(BiomeSource biomeSource, Climate.Sampler climateSampler,
            int startX, int startZ) {
        for (int sampleX = 0; sampleX < SAMPLE_AREA_SIZE; sampleX += COARSE_SAMPLE_STEP) {
            for (int sampleZ = 0; sampleZ < SAMPLE_AREA_SIZE; sampleZ += COARSE_SAMPLE_STEP) {
                Biome biome = getNoiseBiome(biomeSource, climateSampler,
                        startX + sampleX - SAMPLE_PADDING,
                        startZ + sampleZ - SAMPLE_PADDING);
                if (biome.getBaseTemperature() < 0.15f) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void blendSurfaceBlocks(WorldGenLevel level, int startX, int startZ, boolean[][] snowySamples,
            BlockPos.MutableBlockPos mutable) {
        for (int localX = -SURFACE_BLEND_OUTER_RADIUS; localX < 16 + SURFACE_BLEND_OUTER_RADIUS; localX++) {
            for (int localZ = -SURFACE_BLEND_OUTER_RADIUS; localZ < 16 + SURFACE_BLEND_OUTER_RADIUS; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;
                int sampleX = localX + SAMPLE_PADDING;
                int sampleZ = localZ + SAMPLE_PADDING;
                boolean snowy = snowySamples[sampleX][sampleZ];

                int oppositeBiomeCount = 0;
                int minDist = TRANSITION_RADIUS + 1;

                for (int dx = -TRANSITION_RADIUS; dx <= TRANSITION_RADIUS; dx += SAMPLE_STEP) {
                    for (int dz = -TRANSITION_RADIUS; dz <= TRANSITION_RADIUS; dz += SAMPLE_STEP) {
                        if (dx == 0 && dz == 0) continue;
                        if (snowySamples[sampleX + dx][sampleZ + dz] != snowy) {
                            oppositeBiomeCount++;
                            int dist = Math.max(Math.abs(dx), Math.abs(dz));
                            minDist = Math.min(minDist, dist);
                        }
                    }
                }

                if (oppositeBiomeCount == 0) {
                    continue;
                }

                int totalSamples = ((TRANSITION_RADIUS * 2) / SAMPLE_STEP + 1)
                        * ((TRANSITION_RADIUS * 2) / SAMPLE_STEP + 1) - 1;
                float oppositeRatio = (float) oppositeBiomeCount / totalSamples;
                float distFactor = Math.max(0.0f, 1.0f - ((float) (minDist - 1) / TRANSITION_RADIUS));
                float probability = MAX_SURFACE_BLEND_PROBABILITY * oppositeRatio * distFactor;

                if (positionHash(worldX + 65536, worldZ - 65536) >= probability) {
                    continue;
                }

                int surfaceY = findSurfaceY(level, worldX, worldZ, mutable);
                if (surfaceY == Integer.MIN_VALUE) {
                    continue;
                }

                mutable.set(worldX, surfaceY, worldZ);
                BlockState surfaceState = level.getBlockState(mutable);
                if (snowy) {
                    blendSnowySurfaceTowardWarm(level, mutable, surfaceState);
                } else {
                    blendWarmSurfaceTowardSnowy(level, mutable, surfaceState);
                }
            }
        }
    }

    private static int findSurfaceY(WorldGenLevel level, int x, int z, BlockPos.MutableBlockPos mutable) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
        int minY = CHRONO_DAWN_MIN_Y;

        while (y >= minY) {
            mutable.set(x, y, z);
            BlockState state = level.getBlockState(mutable);
            if (!state.isAir() && !state.is(Blocks.SNOW)) {
                return y;
            }
            y--;
        }

        return Integer.MIN_VALUE;
    }

    private static void blendSnowySurfaceTowardWarm(WorldGenLevel level, BlockPos.MutableBlockPos pos,
            BlockState surfaceState) {
        if (surfaceState.is(Blocks.ICE)) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
        } else if (isTemporalSoil(surfaceState)) {
            level.setBlock(pos, temporalGrassState(level, pos), 2);
        }
    }

    private static void blendWarmSurfaceTowardSnowy(WorldGenLevel level, BlockPos.MutableBlockPos pos,
            BlockState surfaceState) {
        if (surfaceState.is(Blocks.WATER)) {
            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 2);
        } else if (isTemporalSoil(surfaceState)) {
            level.setBlock(pos, temporalGrassState(level, pos), 2);
        }
    }

    private static boolean isTemporalSoil(BlockState state) {
        return state.is(ModBlocks.TEMPORAL_GRASS_BLOCK.get())
                || state.is(ModBlocks.TEMPORAL_DIRT.get())
                || state.is(ModBlocks.COARSE_TEMPORAL_DIRT.get());
    }

    private static BlockState temporalGrassState(WorldGenLevel level, BlockPos pos) {
        boolean snowy = level.getBlockState(pos.above()).is(Blocks.SNOW);
        return ModBlocks.TEMPORAL_GRASS_BLOCK.get().defaultBlockState().setValue(SnowyDirtBlock.SNOWY, snowy);
    }

    private static float positionHash(int x, int z) {
        long seed = (long) x * 341873128712L + (long) z * 132897987541L;
        seed = (seed ^ (seed >>> 13)) * 2654435769L;
        seed = seed ^ (seed >>> 16);
        return (seed & 0xFFFFFF) / (float) 0xFFFFFF;
    }
}
