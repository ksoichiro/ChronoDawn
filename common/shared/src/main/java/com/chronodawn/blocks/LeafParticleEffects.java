package com.chronodawn.blocks;

import com.chronodawn.compat.CompatLevelTime;
import com.chronodawn.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Client-side ambient particles for the Time Wood leaf blocks.
 *
 * Called from the leaf blocks' animateTick. Uses only Level and BlockState APIs that
 * are stable across every supported version so it can stay in common/shared.
 */
public final class LeafParticleEffects {
    /** Chance per animateTick, matching vanilla falling leaves on versions without the hook. */
    private static final float SPAWN_CHANCE = 0.01f;

    public enum Kind {
        TIME_WOOD,
        ANCIENT_TIME_WOOD,
        DARK_TIME_WOOD
    }

    private LeafParticleEffects() {
    }

    /**
     * Rolls the spawn chance, then spawns one effect.
     */
    public static void trySpawn(Kind kind, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < SPAWN_CHANCE) {
            spawn(kind, level, pos, random);
        }
    }

    /**
     * Spawns one effect for the given leaf kind without rolling the chance.
     */
    public static void spawn(Kind kind, Level level, BlockPos pos, RandomSource random) {
        switch (kind) {
            case TIME_WOOD -> spawnBelow(ModParticles.TIME_WOOD_LEAF.get(), level, pos, random);
            case ANCIENT_TIME_WOOD -> spawnAbove(ModParticles.ANCIENT_TIME_WOOD_LEAF.get(), level, pos, random);
            case DARK_TIME_WOOD -> {
                spawnBelow(ModParticles.DARK_TIME_WOOD_LEAF.get(), level, pos, random);
                if (ChronoBloomStage.isNight(CompatLevelTime.dayTime(level))) {
                    spawnBelow(ModParticles.NIGHT_BLOSSOM_PETAL.get(), level, pos, random);
                }
            }
        }
    }

    private static void spawnBelow(ParticleOptions particle, Level level, BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        // A solid top face would hide the particle inside the block
        if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return;
        }
        level.addParticle(particle,
            pos.getX() + random.nextDouble(), pos.getY() - 0.05, pos.getZ() + random.nextDouble(),
            0.0, 0.0, 0.0);
    }

    private static void spawnAbove(ParticleOptions particle, Level level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN)) {
            return;
        }
        level.addParticle(particle,
            pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(),
            0.0, 0.0, 0.0);
    }
}
