package com.chronodawn.mixin;

import com.chronodawn.registry.ModDimensions;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowAndFreezeFeature.class)
public abstract class FreezeTopLayerMixin {

    private static final int TRANSITION_RADIUS = 8;
    private static final int SAMPLE_STEP = 2;
    private static final int SAMPLE_AREA_SIZE = 16 + TRANSITION_RADIUS * 2;
    private static final float MAX_ADD_PROBABILITY = 0.7f;
    private static final float MAX_REMOVE_PROBABILITY = 0.8f;

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
        boolean[][] snowySamples = new boolean[SAMPLE_AREA_SIZE][SAMPLE_AREA_SIZE];
        boolean[][] precipitationSamples = new boolean[SAMPLE_AREA_SIZE][SAMPLE_AREA_SIZE];
        boolean hasSnowyBiome = false;
        boolean hasWarmBiome = false;

        for (int sampleX = 0; sampleX < SAMPLE_AREA_SIZE; sampleX++) {
            for (int sampleZ = 0; sampleZ < SAMPLE_AREA_SIZE; sampleZ++) {
                Biome biome = getNoiseBiome(biomeSource, climateSampler,
                        startX + sampleX - TRANSITION_RADIUS,
                        startZ + sampleZ - TRANSITION_RADIUS);
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
                int sampleX = localX + TRANSITION_RADIUS;
                int sampleZ = localZ + TRANSITION_RADIUS;

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
                int sampleX = localX + TRANSITION_RADIUS;
                int sampleZ = localZ + TRANSITION_RADIUS;

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
    }

    private static Biome getNoiseBiome(BiomeSource biomeSource, Climate.Sampler climateSampler, int x, int z) {
        return biomeSource.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(0), QuartPos.fromBlock(z),
                climateSampler).value();
    }

    private static float positionHash(int x, int z) {
        long seed = (long) x * 341873128712L + (long) z * 132897987541L;
        seed = (seed ^ (seed >>> 13)) * 2654435769L;
        seed = seed ^ (seed >>> 16);
        return (seed & 0xFFFFFF) / (float) 0xFFFFFF;
    }
}
