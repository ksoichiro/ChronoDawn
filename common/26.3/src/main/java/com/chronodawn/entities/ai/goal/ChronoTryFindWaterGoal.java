package com.chronodawn.entities.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Steers a stranded water mob back toward the nearest water block.
 *
 * 26.3 removed vanilla's {@code net.minecraft.world.entity.ai.goal.TryFindWaterGoal}
 * entirely (no replacement class), so ChronoDawn's water mobs (GlideFish, ChronoTurtle)
 * need their own copy of the same behavior: while out of water, search the
 * immediately surrounding blocks for water and swim toward it.
 */
public class ChronoTryFindWaterGoal extends Goal {
    private final PathfinderMob mob;

    public ChronoTryFindWaterGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !this.mob.level().getFluidState(this.mob.blockPosition()).is(FluidTags.WATER)
                && this.mob.getY() >= this.mob.level().getMinY();
    }

    @Override
    public void tick() {
        BlockPos target = null;

        for (BlockPos candidate : BlockPos.betweenClosed(
                Mth.floor(this.mob.getX() - 2.0), Mth.floor(this.mob.getY() - 2.0), Mth.floor(this.mob.getZ() - 2.0),
                Mth.floor(this.mob.getX() + 2.0), Mth.floor(this.mob.getY() - 1.0), Mth.floor(this.mob.getZ() + 2.0))) {
            if (this.mob.level().getFluidState(candidate).is(FluidTags.WATER)) {
                target = candidate.immutable();
                break;
            }
        }

        if (target == null) {
            target = BlockPos.containing(this.mob.getX(), this.mob.getY() - 2.0, this.mob.getZ());
        }

        this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0);
    }
}
