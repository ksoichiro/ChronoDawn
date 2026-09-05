package com.chronodawn.entities.bosses;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

/**
 * Extended melee attack goal with configurable attack range.
 *
 * Standard MeleeAttackGoal uses a fixed attack range based on entity bounding box.
 * This custom implementation allows specifying a custom attack range for bosses or special mobs.
 *
 * Based on vanilla MeleeAttackGoal but with extended reach capability.
 */
public class ExtendedMeleeAttackGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;
    private final double attackRange;
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;
    private long lastCanUseCheck;

    /**
     * Create extended melee attack goal with custom attack range.
     *
     * @param mob The pathfinder mob
     * @param speedModifier Movement speed multiplier when pursuing target
     * @param followingTargetEvenIfNotSeen Whether to follow target even when not visible
     * @param attackRange Maximum attack range in blocks
     */
    public ExtendedMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen, double attackRange) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.attackRange = attackRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.mob.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < 20L) {
            return false;
        } else {
            this.lastCanUseCheck = gameTime;
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(target, 0);
                return this.path != null || this.getAttackReachSqr(target) >= this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return false;
        } else if (!target.isAlive()) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        } else {
            return this.mob.isWithinHome();
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }

    @Override
    public void stop() {
        LivingEntity target = this.mob.getTarget();
        if (!canContinueToUse()) {
            this.mob.setTarget(null);
        }

        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distanceSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);

        if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(target))
                && this.ticksUntilNextPathRecalculation <= 0
                && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D
                || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D
                || this.mob.getRandom().nextFloat() < 0.05F)) {
            this.pathedTargetX = target.getX();
            this.pathedTargetY = target.getY();
            this.pathedTargetZ = target.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);

            if (distanceSqr > 1024.0D) {
                this.ticksUntilNextPathRecalculation += 10;
            } else if (distanceSqr > 256.0D) {
                this.ticksUntilNextPathRecalculation += 5;
            }

            if (!this.mob.getNavigation().moveTo(target, this.speedModifier)) {
                this.ticksUntilNextPathRecalculation += 15;
            }

            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
        }

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        this.checkAndPerformAttack(target, distanceSqr);
    }

    /**
     * Check if target is in range and perform attack.
     */
    protected void checkAndPerformAttack(LivingEntity target, double distanceSqr) {
        double attackReachSqr = this.getAttackReachSqr(target);
        if (distanceSqr <= attackReachSqr && this.ticksUntilNextAttack <= 0) {
            // Special check for Time Guardian: Don't attack during post-teleport delay
            if (this.mob instanceof TimeGuardianEntity guardian && guardian.isInPostTeleportDelay()) {
                return;
            }

            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(getServerLevel(target), target);
        }
    }

    /**
     * Reset attack cooldown based on attack speed.
     */
    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.adjustedTickDelay(20);
    }

    /**
     * Get attack reach squared.
     * Uses custom attack range instead of default calculation.
     */
    protected double getAttackReachSqr(LivingEntity target) {
        return this.attackRange * this.attackRange;
    }
}
