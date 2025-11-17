package com.chronosphere.entities.ai;

import com.chronosphere.entities.bosses.TimeGuardianEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

/**
 * Custom ranged attack goal for Time Guardian.
 *
 * Features:
 * - Cooldown: 10 seconds between ranged attacks
 * - Minimum range: Only attacks targets 7+ blocks away
 * - Attack interval: 5 seconds (100 ticks)
 *
 * This prevents the Time Guardian from spamming ranged attacks continuously,
 * making the boss fight more balanced.
 *
 * Reference: T210 - Add ranged attack capability to Time Guardian
 */
public class TimeGuardianRangedAttackGoal extends Goal {
    private final TimeGuardianEntity entity;
    private final double speedModifier;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float minAttackDistance;
    private final int cooldownTicks;

    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    /**
     * Create a ranged attack goal for Time Guardian.
     *
     * @param entity The Time Guardian entity
     * @param speedModifier Movement speed modifier when approaching
     * @param attackIntervalTicks Attack interval in ticks
     * @param attackRadius Maximum attack range in blocks
     * @param minAttackDistance Minimum attack range in blocks (won't use ranged attack if closer)
     * @param cooldownTicks Cooldown between ranged attacks in ticks
     */
    public TimeGuardianRangedAttackGoal(TimeGuardianEntity entity, double speedModifier, int attackIntervalTicks,
                                        float attackRadius, float minAttackDistance, int cooldownTicks) {
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalTicks;
        this.attackIntervalMax = attackIntervalTicks;
        this.attackRadius = attackRadius * attackRadius; // Square for distance comparison
        this.minAttackDistance = minAttackDistance * minAttackDistance; // Square for distance comparison
        this.cooldownTicks = cooldownTicks;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.entity.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Check if cooldown is active
        if (this.entity.getRangedAttackCooldown() > 0) {
            return false;
        }

        // Check if target is within range
        double distanceSquared = this.entity.distanceToSqr(target);

        // Only use ranged attack if target is within max range but not too close
        return distanceSquared >= this.minAttackDistance && distanceSquared <= this.attackRadius;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.entity.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        // Stop if cooldown becomes active
        if (this.entity.getRangedAttackCooldown() > 0) {
            return false;
        }

        return this.canUse() || !this.entity.getNavigation().isDone();
    }

    @Override
    public void start() {
        super.start();
        this.entity.setAggressive(true);
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public void stop() {
        super.stop();
        this.entity.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.entity.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.entity.getTarget();
        if (target == null) {
            return;
        }

        double distanceSquared = this.entity.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSee = this.entity.getSensing().hasLineOfSight(target);

        if (canSee) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        // Move towards target if too far
        if (distanceSquared > this.attackRadius) {
            this.entity.getNavigation().moveTo(target, this.speedModifier);
        } else {
            this.entity.getNavigation().stop();
        }

        // Look at target
        this.entity.getLookControl().setLookAt(target, 30.0f, 30.0f);

        // Attack logic
        if (--this.attackTime == 0) {
            if (!canSee) {
                return;
            }

            float f = (float) Math.sqrt(distanceSquared) / this.attackRadius;
            float clampedDistance = net.minecraft.util.Mth.clamp(f, 0.1f, 1.0f);

            // Perform ranged attack
            this.entity.performRangedAttack(target, clampedDistance);

            // Set cooldown
            this.entity.setRangedAttackCooldown(this.cooldownTicks);

            // Reset attack timer
            this.attackTime = net.minecraft.util.Mth.floor(
                f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin
            );
        } else if (this.attackTime < 0) {
            this.attackTime = net.minecraft.util.Mth.floor(
                net.minecraft.util.Mth.lerp(
                    Math.sqrt(distanceSquared) / this.attackRadius,
                    (double) this.attackIntervalMin,
                    (double) this.attackIntervalMax
                )
            );
        }
    }
}
