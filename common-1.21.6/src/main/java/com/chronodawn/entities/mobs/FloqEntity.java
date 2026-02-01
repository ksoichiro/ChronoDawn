package com.chronodawn.entities.mobs;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Floq - A slime-like hostile mob with jumping movement
 *
 * A small cube-shaped creature that moves by jumping like vanilla slimes.
 *
 * Attributes:
 * - Health: 16 (8 hearts)
 * - Attack Damage: 3 (1.5 hearts)
 * - Movement Speed: 0.3 (similar to slimes)
 *
 * Special Mechanics:
 * - Jumps to move (similar to vanilla slimes)
 * - Hostile behavior, attacks players
 * - Spawns in dark areas like other monsters
 *
 * Reference: Custom implementation based on vanilla Slime behavior
 */
public class FloqEntity extends Monster {
    // Animation state for squish effect
    public float targetSquish;
    public float squish;
    public float oSquish; // Old squish for interpolation

    public FloqEntity(EntityType<? extends FloqEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FloqMoveControl(this);
        this.xpReward = 5; // Monster XP
    }

    /**
     * Define AI goals for Floq behavior
     */
    @Override
    protected void registerGoals() {
        // Priority 1: Attack players
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));

        // Priority 2: Jump randomly
        this.goalSelector.addGoal(2, new FloqRandomDirectionGoal(this));

        // Priority 3: Float in water
        this.goalSelector.addGoal(3, new FloqFloatGoal(this));

        // Priority 4: Keep jumping
        this.goalSelector.addGoal(4, new FloqKeepOnJumpingGoal(this));

        // Target players
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Create attribute supplier for Floq
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 16.0) // 8 hearts
            .add(Attributes.MOVEMENT_SPEED, 0.4) // Moderate speed with rapid jumping
            .add(Attributes.ATTACK_DAMAGE, 3.0); // 1.5 hearts damage
    }

    /**
     * Floq jumps when moving
     */
    @Override
    public void jumpFromGround() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x, this.getJumpPower(), vec3.z);
        this.hasImpulse = true;
    }

    /**
     * Override spawn rules for Floq (monster spawn rules)
     * Spawns in dark areas like vanilla monsters
     */
    public static boolean checkFloqSpawnRules(
        EntityType<FloqEntity> entityType,
        ServerLevelAccessor level,
        EntitySpawnReason spawnType,
        net.minecraft.core.BlockPos pos,
        RandomSource random
    ) {
        // Use standard monster spawn rules - dark areas only
        return Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random);
    }

    /**
     * Play jump sound
     */
    protected SoundEvent getJumpSound() {
        return SoundEvents.SLIME_JUMP;
    }

    /**
     * Override hurt sound
     */
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SLIME_HURT;
    }

    /**
     * Override death sound
     */
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    /**
     * Floq should always try to jump
     */
    protected boolean canJump() {
        return true;
    }

    /**
     * Update squish animation on every tick
     */
    @Override
    public void tick() {
        // Update squish animation (like vanilla Slime)
        this.oSquish = this.squish;

        // When on ground, squish increases (compressed state)
        // When in air, squish decreases (stretched/normal state)
        if (this.onGround() && !this.wasOnGround) {
            // Just landed - maximum squish
            this.targetSquish = -0.5F;
        } else if (!this.onGround() && this.wasOnGround) {
            // Just jumped - start stretching
            this.targetSquish = 1.0F;
        } else {
            // Gradually return to normal
            this.targetSquish += (0.0F - this.targetSquish) * 0.1F;
        }

        // Smoothly interpolate squish towards target
        this.squish += (this.targetSquish - this.squish) * 0.5F;

        // Clamp squish to prevent extreme values
        this.squish = Mth.clamp(this.squish, -0.5F, 1.0F);

        super.tick();
    }

    private boolean wasOnGround = false;

    /**
     * Track ground state for squish animation
     */
    @Override
    public void aiStep() {
        this.wasOnGround = this.onGround();
        super.aiStep();
    }

    /**
     * Custom move control for Floq (based on Slime's movement)
     */
    static class FloqMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final FloqEntity floq;
        private boolean isAggressive;

        public FloqMoveControl(FloqEntity floq) {
            super(floq);
            this.floq = floq;
            this.yRot = 180.0F * floq.getYRot() / (float)Math.PI;
        }

        public void setDirection(float yRot, boolean aggressive) {
            this.yRot = yRot;
            this.isAggressive = aggressive;
        }

        public void setWantedMovement(double speed) {
            this.speedModifier = speed;
            this.operation = Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();

            if (this.operation != Operation.MOVE_TO) {
                this.mob.setZza(0.0F);
            } else {
                this.operation = Operation.WAIT;

                if (this.mob.onGround()) {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));

                    if (this.jumpDelay-- <= 0) {
                        this.jumpDelay = this.floq.getJumpDelay();
                        if (this.isAggressive) {
                            this.jumpDelay /= 3;
                        }

                        this.floq.getJumpControl().jump();
                        if (this.floq.canJump()) {
                            this.floq.playSound(this.floq.getJumpSound(), this.floq.getSoundVolume(), this.floq.getVoicePitch());
                        }
                    } else {
                        this.floq.xxa = 0.0F;
                        this.floq.zza = 0.0F;
                        this.mob.setSpeed(0.0F);
                    }
                } else {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }
            }
        }
    }

    /**
     * Get jump delay (time between jumps in ticks)
     */
    protected int getJumpDelay() {
        return this.random.nextInt(5) + 3; // 3-8 ticks (much shorter delay for rapid jumping)
    }

    /**
     * Goal: Float in water
     */
    static class FloqFloatGoal extends Goal {
        private final FloqEntity floq;

        public FloqFloatGoal(FloqEntity floq) {
            this.floq = floq;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
            floq.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return (this.floq.isInWater() || this.floq.isInLava()) && this.floq.getMoveControl() instanceof FloqMoveControl;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.floq.getRandom().nextFloat() < 0.8F) {
                this.floq.getJumpControl().jump();
            }

            ((FloqMoveControl)this.floq.getMoveControl()).setWantedMovement(1.2);
        }
    }

    /**
     * Goal: Keep jumping while moving
     */
    static class FloqKeepOnJumpingGoal extends Goal {
        private final FloqEntity floq;

        public FloqKeepOnJumpingGoal(FloqEntity floq) {
            this.floq = floq;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.floq.isPassenger();
        }

        @Override
        public void tick() {
            ((FloqMoveControl)this.floq.getMoveControl()).setWantedMovement(1.0);
        }
    }

    /**
     * Goal: Face random direction and jump
     */
    static class FloqRandomDirectionGoal extends Goal {
        private final FloqEntity floq;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public FloqRandomDirectionGoal(FloqEntity floq) {
            this.floq = floq;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.floq.getTarget() == null && (this.floq.onGround() || this.floq.isInWater() || this.floq.isInLava() || this.floq.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION)) && this.floq.getMoveControl() instanceof FloqMoveControl;
        }

        @Override
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.floq.getRandom().nextInt(60));
                this.chosenDegrees = (float)this.floq.getRandom().nextInt(360);
            }

            ((FloqMoveControl)this.floq.getMoveControl()).setDirection(this.chosenDegrees, false);
        }
    }
}
