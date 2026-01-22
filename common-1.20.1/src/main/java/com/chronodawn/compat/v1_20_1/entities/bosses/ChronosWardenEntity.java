package com.chronodawn.entities.bosses;

import com.chronodawn.entities.ai.GroundSlamGoal;
import com.chronodawn.registry.ModItems;
import com.chronodawn.registry.ModSounds;
import com.chronodawn.worldgen.protection.BlockProtectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Chronos Warden (クロノスの監視者) - Mini-Boss Entity
 *
 * An ancient guardian statue that protects underground vaults.
 * Awakens when intruders approach.
 *
 * Attributes:
 * - Health: 180 (90 hearts)
 * - Armor: 12 (high defense)
 * - Attack Damage: 9
 * - Movement Speed: 0.15 (slow but tanky)
 * - Knockback Resistance: 0.8
 *
 * AI Phases:
 * - Phase 1 (HP 100%-60%): Basic Combat
 *   - Standard melee attacks
 *   - Guardian's Burden: Applies Mining Fatigue II (5 seconds) on hit
 *   - Ground Slam: Every 10 seconds, knockback in 4-block radius (2 damage)
 *
 * - Phase 2 (HP 60%-0%): Defensive Stance
 *   - Stone Stance: Every 20 seconds, freezes for 5 seconds
 *     - Takes 80% reduced damage
 *     - Glowing weak spot appears on back (normal damage from behind)
 *     - Cannot move or attack during stance
 *   - Ground Slam frequency increases to every 7 seconds
 *
 * Drops:
 * - Guardian Stone (100%)
 * - Enhanced Clockstone x2-4 (100%)
 *
 * Reference: research.md (Additional Bosses Implementation Plan - Phase 1)
 * Task: T234a [Phase 1] Create ChronosWardenEntity.java
 */
public class ChronosWardenEntity extends Monster {
    // Boss bar for tracking health
    private final ServerBossEvent bossEvent = new ServerBossEvent(
        this.getDisplayName(),
        BossEvent.BossBarColor.BLUE,
        BossEvent.BossBarOverlay.PROGRESS
    );

    // Entity data accessor for phase tracking
    private static final EntityDataAccessor<Integer> PHASE =
        SynchedEntityData.defineId(ChronosWardenEntity.class, EntityDataSerializers.INT);

    // Entity data accessor for Stone Stance state
    private static final EntityDataAccessor<Boolean> IN_STONE_STANCE =
        SynchedEntityData.defineId(ChronosWardenEntity.class, EntityDataSerializers.BOOLEAN);

    // Phase constants
    private static final int PHASE_1 = 1; // 100%-60% HP: Basic Combat
    private static final int PHASE_2 = 2; // 60%-0% HP: Defensive Stance

    // Combat timing
    private int groundSlamCooldown = 0;
    private int stoneStanceCooldown = 0;
    private int stoneStanceDuration = 0; // Remaining ticks in stance

    // Ground Slam timing
    private static final int GROUND_SLAM_COOLDOWN_PHASE1 = 200; // 10 seconds
    private static final int GROUND_SLAM_COOLDOWN_PHASE2 = 140; // 7 seconds
    private static final double GROUND_SLAM_RANGE = 4.0;
    private static final float GROUND_SLAM_DAMAGE = 2.0f; // 1 heart

    // Stone Stance timing (Phase 2)
    private static final int STONE_STANCE_COOLDOWN_TICKS = 400; // 20 seconds
    private static final int STONE_STANCE_DURATION_TICKS = 100; // 5 seconds
    private static final float STONE_STANCE_DAMAGE_REDUCTION = 0.8f; // 80% reduction

    public ChronosWardenEntity(EntityType<? extends ChronosWardenEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 80; // Experience points on defeat
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, PHASE_1);
        this.entityData.define(IN_STONE_STANCE, false);
    }

    /**
     * Create attribute supplier for Chronos Warden.
     * Sets base attributes like health, armor, attack damage, and movement speed.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 180.0) // 90 hearts
            .add(Attributes.ARMOR, 12.0) // High defense
            .add(Attributes.ATTACK_DAMAGE, 9.0) // 4.5 hearts
            .add(Attributes.ATTACK_KNOCKBACK, 1.0)
            .add(Attributes.MOVEMENT_SPEED, 0.15) // Slow
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Ground Slam attack
        // Activates when target is 2-6 blocks away
        // Deals AoE damage and knockback
        this.goalSelector.addGoal(1, new GroundSlamGoal(this, 2.0, 6.0));

        // Priority 2: Melee attack with extended range (3.0 blocks)
        this.goalSelector.addGoal(2, new ExtendedMeleeAttackGoal(this, 1.0, false, 3.0));

        // Priority 3: Move towards target
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 0.9, 32.0f));

        // Priority 4: Wander around
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));

        // Priority 5: Look at players
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 6: Random look around
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // Target selection
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Update phase based on health
            updatePhase();

            // Handle cooldowns
            if (groundSlamCooldown > 0) {
                groundSlamCooldown--;
            }

            if (stoneStanceCooldown > 0) {
                stoneStanceCooldown--;
            }

            // Handle Stone Stance duration
            if (isInStoneStance()) {
                stoneStanceDuration--;
                if (stoneStanceDuration <= 0) {
                    exitStoneStance();
                }

                // Particle effects during stance
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        ParticleTypes.SOUL,
                        this.getX(),
                        this.getY() + 1.0,
                        this.getZ(),
                        2, // count
                        0.5, 0.5, 0.5, // spread
                        0.01 // speed
                    );
                }
            }

            // Phase 2 special abilities
            if (getPhase() == PHASE_2) {
                handlePhase2Abilities();
            }
        }
    }

    /**
     * Update phase based on current health percentage.
     */
    private void updatePhase() {
        float healthPercentage = this.getHealth() / this.getMaxHealth();
        int newPhase = healthPercentage > 0.6f ? PHASE_1 : PHASE_2;

        if (newPhase != getPhase()) {
            setPhase(newPhase);
            // Announce phase change
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(
                    null,
                    this.blockPosition(),
                    ModSounds.WARDEN_ROAR.get(),
                    SoundSource.HOSTILE,
                    1.0f,
                    0.8f
                );
            }
        }
    }

    /**
     * Handle Phase 2 special abilities.
     */
    private void handlePhase2Abilities() {
        // Stone Stance ability
        if (!isInStoneStance() && stoneStanceCooldown == 0 && this.getTarget() != null) {
            enterStoneStance();
        }
    }

    /**
     * Enter Stone Stance defensive mode.
     */
    private void enterStoneStance() {
        this.entityData.set(IN_STONE_STANCE, true);
        this.stoneStanceDuration = STONE_STANCE_DURATION_TICKS;
        this.stoneStanceCooldown = STONE_STANCE_COOLDOWN_TICKS;

        // Play sound effect
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.TIME_NEEDLE_BREAK.get(),
            SoundSource.HOSTILE,
            1.0f,
            0.5f
        );

        // Apply Glowing effect (weak spot indicator)
        this.addEffect(new MobEffectInstance(
            MobEffects.GLOWING,
            STONE_STANCE_DURATION_TICKS,
            0,
            false,
            false
        ));

        // Stop movement
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Exit Stone Stance defensive mode.
     */
    private void exitStoneStance() {
        this.entityData.set(IN_STONE_STANCE, false);

        // Play sound effect
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.TIME_NEEDLE_PLACE.get(),
            SoundSource.HOSTILE,
            1.0f,
            0.8f
        );
    }

    /**
     * Check if currently in Stone Stance.
     */
    public boolean isInStoneStance() {
        return this.entityData.get(IN_STONE_STANCE);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        // Cannot attack during Stone Stance
        if (isInStoneStance()) {
            return false;
        }

        boolean success = super.doHurtTarget(target);

        if (success && target instanceof LivingEntity livingTarget) {
            // Guardian's Burden: inflict Mining Fatigue II
            livingTarget.addEffect(new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN,
                100, // 5 seconds
                1    // Mining Fatigue II (amplifier 1)
            ));
        }

        return success;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // During Stone Stance: 80% damage reduction
        if (isInStoneStance()) {
            // Check if damage is from behind (weak spot)
            if (source.getEntity() instanceof LivingEntity attacker) {
                Vec3 attackerPos = attacker.position();
                Vec3 myPos = this.position();
                Vec3 toAttacker = attackerPos.subtract(myPos).normalize();
                Vec3 myLookVec = this.getLookAngle();

                // Dot product < 0 means attacker is behind
                double dot = myLookVec.dot(toAttacker);
                if (dot < 0) {
                    // Attack from behind - take normal damage
                    return super.hurt(source, amount);
                }
            }

            // Attack from front/side - reduce damage
            amount *= (1.0f - STONE_STANCE_DAMAGE_REDUCTION);
        }

        return super.hurt(source, amount);
    }

    /**
     * Perform Ground Slam attack.
     * Deals damage and knockback to nearby entities.
     */
    public void performGroundSlam() {
        if (this.level().isClientSide || isInStoneStance()) {
            return;
        }

        // Play sound effect
        this.level().playSound(
            null,
            this.blockPosition(),
            ModSounds.BOSS_POWER_UP.get(),
            SoundSource.HOSTILE,
            0.5f,
            0.8f
        );

        // Particle effects
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                this.getX(),
                this.getY() + 0.1,
                this.getZ(),
                8, // count
                2.0, 0.1, 2.0, // spread
                0.0 // speed
            );
        }

        // Damage and knockback nearby entities
        AABB area = new AABB(this.blockPosition()).inflate(GROUND_SLAM_RANGE);
        this.level().getEntitiesOfClass(LivingEntity.class, area).forEach(entity -> {
            if (entity != this && entity instanceof Player) {
                // Apply damage
                entity.hurt(this.damageSources().mobAttack(this), GROUND_SLAM_DAMAGE);

                // Apply knockback
                Vec3 direction = entity.position().subtract(this.position()).normalize();
                entity.setDeltaMovement(entity.getDeltaMovement().add(
                    direction.x * 0.5,
                    0.3, // Upward knockback
                    direction.z * 0.5
                ));
                entity.hurtMarked = true;
            }
        });

        // Set cooldown
        int cooldown = getPhase() == PHASE_1 ? GROUND_SLAM_COOLDOWN_PHASE1 : GROUND_SLAM_COOLDOWN_PHASE2;
        this.groundSlamCooldown = cooldown;
    }

    /**
     * Check if Ground Slam is ready to use.
     */
    public boolean canGroundSlam() {
        return groundSlamCooldown == 0 && !isInStoneStance();
    }

    /**
     * Get current phase.
     */
    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    /**
     * Set current phase.
     */
    private void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        this.entityData.set(PHASE, compound.getInt("Phase"));
        this.entityData.set(IN_STONE_STANCE, compound.getBoolean("InStoneStance"));
        this.groundSlamCooldown = compound.getInt("GroundSlamCooldown");
        this.stoneStanceCooldown = compound.getInt("StoneStanceCooldown");
        this.stoneStanceDuration = compound.getInt("StoneStanceDuration");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Phase", getPhase());
        compound.putBoolean("InStoneStance", isInStoneStance());
        compound.putInt("GroundSlamCooldown", this.groundSlamCooldown);
        compound.putInt("StoneStanceCooldown", this.stoneStanceCooldown);
        compound.putInt("StoneStanceDuration", this.stoneStanceDuration);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        // Prevent movement during Stone Stance
        if (isInStoneStance()) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);

        // Unprotect Guardian Vault boss room when defeated
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // Use position-based detection to find and unprotect the boss room
            BlockProtectionHandler.onBossDefeatedAt(serverLevel, this.blockPosition());
        }
    }
}
