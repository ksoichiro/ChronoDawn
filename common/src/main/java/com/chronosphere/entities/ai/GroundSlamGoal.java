package com.chronosphere.entities.ai;

import com.chronosphere.entities.bosses.ChronosWardenEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * AI Goal for Chronos Warden's Ground Slam attack.
 *
 * This goal causes the Chronos Warden to perform a ground slam attack
 * when it has a target and the cooldown has expired.
 *
 * Ground Slam deals area-of-effect damage and knockback to nearby players.
 *
 * Task: T234c [Phase 1] Implement GroundSlamGoal.java
 */
public class GroundSlamGoal extends Goal {
    private final ChronosWardenEntity warden;
    private final double minDistanceToTarget;
    private final double maxDistanceToTarget;

    /**
     * Create a new Ground Slam goal.
     *
     * @param warden The Chronos Warden entity
     * @param minDistance Minimum distance to target to use Ground Slam
     * @param maxDistance Maximum distance to target to use Ground Slam
     */
    public GroundSlamGoal(ChronosWardenEntity warden, double minDistance, double maxDistance) {
        this.warden = warden;
        this.minDistanceToTarget = minDistance * minDistance; // Square for faster distance checks
        this.maxDistanceToTarget = maxDistance * maxDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * Check if this goal can be used.
     * Returns true if:
     * - Warden has a target
     * - Warden is not in Stone Stance
     * - Ground Slam is off cooldown
     * - Target is within distance range
     */
    @Override
    public boolean canUse() {
        LivingEntity target = this.warden.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (this.warden.isInStoneStance()) {
            return false;
        }

        if (!this.warden.canGroundSlam()) {
            return false;
        }

        double distanceSquared = this.warden.distanceToSqr(target);
        return distanceSquared >= minDistanceToTarget && distanceSquared <= maxDistanceToTarget;
    }

    /**
     * Check if this goal should continue executing.
     * Continues as long as canUse() conditions are met.
     */
    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    /**
     * Execute the ground slam attack.
     */
    @Override
    public void start() {
        // Look at target before slamming
        LivingEntity target = this.warden.getTarget();
        if (target != null) {
            this.warden.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }

        // Perform the ground slam
        this.warden.performGroundSlam();
    }

    /**
     * Reset state when goal stops.
     */
    @Override
    public void stop() {
        // No cleanup needed
    }
}
