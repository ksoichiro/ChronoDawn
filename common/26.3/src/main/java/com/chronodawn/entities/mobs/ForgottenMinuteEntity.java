package com.chronodawn.entities.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Forgotten Minute (フォーゴトン・ミニット) - Hostile Mob
 *
 * A semi-transparent creature that suddenly appears and disappears after a short time.
 * When attacked, it strengthens nearby enemies.
 *
 * Attributes:
 * - Health: 14 (7 hearts)
 * - Attack Damage: 4 (2 hearts)
 * - Movement Speed: 0.4 (fast, similar to Vex)
 *
 * Special Mechanics:
 * - Suddenly appears and disappears after a short time (future implementation)
 * - When attacked, strengthens nearby enemies (future implementation)
 *
 * Drops:
 * - Faded Time Dust (future implementation)
 */
public class ForgottenMinuteEntity extends Monster {
    public ForgottenMinuteEntity(EntityType<? extends ForgottenMinuteEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5; // Standard XP reward
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        // Note: setCanPassDoors() was removed in 1.21.4
        return navigation;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
        // Flying mobs don't take fall damage
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel level) {
        super.customServerAiStep(level);
        // Ensure the mob stays airborne
        Vec3 movement = this.getDeltaMovement();
        if (!this.onGround() && movement.y < 0.0) {
            this.setDeltaMovement(movement.multiply(1.0, 0.6, 1.0));
        }
    }

    /**
     * Define AI goals for Forgotten Minute behavior
     */
    @Override
    protected void registerGoals() {
        // Priority 1: Charge attack (similar to Vex)
        this.goalSelector.addGoal(1, new ForgottenMinuteChargeAttackGoal());

        // Priority 2: Random floating movement
        this.goalSelector.addGoal(2, new ForgottenMinuteRandomMoveGoal());

        // Priority 3: Look at player
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Target selection: Aggressive towards players
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Create attribute supplier for Forgotten Minute
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 14.0) // 7 hearts
            .add(Attributes.ATTACK_DAMAGE, 4.0) // 2 hearts
            .add(Attributes.MOVEMENT_SPEED, 0.4) // Fast (Vex-like)
            .add(Attributes.FOLLOW_RANGE, 30.0) // Standard follow range
            .add(Attributes.FLYING_SPEED, 0.4); // Flying speed
    }

    /**
     * In 1.21.5, causeFallDamage is no longer overridable.
     * Flying mobs typically don't take fall damage through the flying navigation.
     * Fall damage immunity is handled by the mob's movement controller.
     */

    /**
     * Override spawn rules to allow spawning in daylight
     */
    public static boolean checkForgottenMinuteSpawnRules(
        EntityType<ForgottenMinuteEntity> entityType,
        ServerLevelAccessor level,
        EntitySpawnReason spawnType,
        net.minecraft.core.BlockPos pos,
        RandomSource random
    ) {
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

    /**
     * AI Goal for charging at targets (similar to Vex charge attack)
     */
    class ForgottenMinuteChargeAttackGoal extends Goal {
        public ForgottenMinuteChargeAttackGoal() {
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (ForgottenMinuteEntity.this.getTarget() != null
                && !ForgottenMinuteEntity.this.getMoveControl().hasWanted()
                && ForgottenMinuteEntity.this.random.nextInt(reducedTickDelay(2)) == 0) {
                return ForgottenMinuteEntity.this.distanceToSqr(ForgottenMinuteEntity.this.getTarget()) > 4.0;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return ForgottenMinuteEntity.this.getMoveControl().hasWanted()
                && ForgottenMinuteEntity.this.getTarget() != null
                && ForgottenMinuteEntity.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            net.minecraft.world.entity.LivingEntity target = ForgottenMinuteEntity.this.getTarget();
            if (target != null) {
                Vec3 targetPos = target.getEyePosition();
                ForgottenMinuteEntity.this.moveControl.setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 2.0);
            }
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            net.minecraft.world.entity.LivingEntity target = ForgottenMinuteEntity.this.getTarget();
            if (target != null) {
                double distanceSq = ForgottenMinuteEntity.this.distanceToSqr(target);

                // Attack if within 2.0 blocks (4.0 squared)
                if (distanceSq < 4.0) {
                    ForgottenMinuteEntity.this.doHurtTarget((net.minecraft.server.level.ServerLevel) ForgottenMinuteEntity.this.level(), target);
                    // Stop briefly after attacking
                    ForgottenMinuteEntity.this.getMoveControl().setWantedPosition(
                        ForgottenMinuteEntity.this.getX(),
                        ForgottenMinuteEntity.this.getY(),
                        ForgottenMinuteEntity.this.getZ(),
                        1.0
                    );
                } else if (distanceSq < 256.0) { // Continue charging if within 16 blocks
                    Vec3 targetPos = target.getEyePosition();
                    ForgottenMinuteEntity.this.moveControl.setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 2.0);
                }
            }
        }
    }

    /**
     * AI Goal for random floating movement (similar to Vex random move)
     */
    class ForgottenMinuteRandomMoveGoal extends Goal {
        public ForgottenMinuteRandomMoveGoal() {
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !ForgottenMinuteEntity.this.getMoveControl().hasWanted()
                && ForgottenMinuteEntity.this.random.nextInt(reducedTickDelay(2)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            BlockPos blockPos = ForgottenMinuteEntity.this.blockPosition();

            for (int i = 0; i < 3; ++i) {
                BlockPos randomPos = blockPos.offset(
                    ForgottenMinuteEntity.this.random.nextInt(15) - 7,
                    ForgottenMinuteEntity.this.random.nextInt(11) - 5,
                    ForgottenMinuteEntity.this.random.nextInt(15) - 7
                );

                if (ForgottenMinuteEntity.this.level().isEmptyBlock(randomPos)) {
                    ForgottenMinuteEntity.this.moveControl.setWantedPosition(
                        (double)randomPos.getX() + 0.5,
                        (double)randomPos.getY() + 0.5,
                        (double)randomPos.getZ() + 0.5,
                        1.5
                    );

                    if (ForgottenMinuteEntity.this.getTarget() == null) {
                        ForgottenMinuteEntity.this.getLookControl().setLookAt(
                            (double)randomPos.getX() + 0.5,
                            (double)randomPos.getY() + 0.5,
                            (double)randomPos.getZ() + 0.5,
                            180.0F,
                            20.0F
                        );
                    }
                    break;
                }
            }
        }
    }
}
