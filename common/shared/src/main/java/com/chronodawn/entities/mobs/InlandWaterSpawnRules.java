package com.chronodawn.entities.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Spawn rule for Chrono Dawn water animals that also live in inland water.
 *
 * Vanilla {@code WaterAnimal#checkSurfaceWaterAnimalSpawnRules} only accepts
 * positions between sea level and 13 blocks below it, with water both below and
 * above the spawn position. Inland rivers, ponds, and mountain pools in the
 * Chrono Dawn dimension are often too shallow or sit above sea level, so that
 * rule left them empty. This rule instead accepts any open-sky water body at
 * least two blocks deep, and rejects underground water such as aquifers by
 * requiring the water column to reach the motion-blocking surface. A frozen
 * surface counts as open sky, so fish still live under the ice of snowy lakes.
 */
public final class InlandWaterSpawnRules {
    private InlandWaterSpawnRules() {
    }

    public static boolean isOpenSkyWater(LevelReader level, BlockPos pos) {
        if (!level.getFluidState(pos).is(FluidTags.WATER)
            || !level.getFluidState(pos.below()).is(FluidTags.WATER)) {
            return false;
        }
        // MOTION_BLOCKING counts fluids and ice, so the block just below it is
        // the top of the water column when nothing else covers the water.
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) - 1;
        if (pos.getY() > surfaceY) {
            return false;
        }
        BlockPos surface = new BlockPos(pos.getX(), surfaceY, pos.getZ());
        return level.getFluidState(surface).is(FluidTags.WATER)
            || level.getBlockState(surface).is(BlockTags.ICE);
    }
}
