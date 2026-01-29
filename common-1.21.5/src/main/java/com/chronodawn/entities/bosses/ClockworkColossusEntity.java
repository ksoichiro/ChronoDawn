package com.chronodawn.entities.bosses;

import com.chronodawn.ChronoDawn;
import com.chronodawn.registry.ModSounds;
import com.chronodawn.worldgen.protection.BlockProtectionHandler;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Clockwork Colossus (機械仕掛けの巨人) - Phase 1 Mini-Boss
 *
 * Massive mechanical guardian built by ancient clockworkers.
 * Powered by temporal gears and relentless in protecting its factory.
 *
 * Stats:
 * - HP: 200 (100 hearts)
 * - Attack Damage: 12
 * - Armor: 8
 * - Movement Speed: 0.18
 * - Knockback Resistance: 1.0 (immune)
 * - XP Reward: 100
 *
 * Phase 1 (100%-50% HP): Normal Mode
 * - Standard melee attacks
 * - Gear Shot: Every 8 seconds, fires spinning gear projectile (range 16 blocks, damage 8)
 *
 * Phase 2 (50%-0% HP): Overcharge Mode
 * - Overcharge: Self-buffs with Speed II (permanent)
 * - Repair Protocol: ONE-TIME at 40% HP - recovers 15% HP (30 HP)
 * - Ground Slam: Linear shockwave attack (10 blocks, 3 blocks wide, damage 6)
 *
 * Special Trait:
 * - Immune to Time Effects (Time Clock, Time Arrow, Time Distortion)
 *
 * Reference: research.md (Boss 2: Clockwork Colossus)
 * Task: T235 [P] Additional Bosses - Clockwork Colossus
 */
public class ClockworkColossusEntity extends Monster implements RangedAttackMob {
    // Boss bar for tracking health
    private final ServerBossEvent bossEvent = new ServerBossEvent(
        this.getDisplayName(),
        BossEvent.BossBarColor.RED,
        BossEvent.BossBarOverlay.PROGRESS
    );

    // Entity data accessors for phase tracking
    private static final EntityDataAccessor<Integer> PHASE =
        SynchedEntityData.defineId(ClockworkColossusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> OVERCHARGED =
        SynchedEntityData.defineId(ClockworkColossusEntity.class, EntityDataSerializers.BOOLEAN);

    // Phase constants
    private static final int PHASE_1 = 1; // 100%-50% HP: Normal Mode
    private static final int PHASE_2 = 2; // 50%-0% HP: Overcharge Mode

    // Combat timing
    private int gearShotCooldown = 0;
    private int groundSlamCooldown = 0;
    private boolean repairProtocolUsed = false;
    private int repairCooldown = 0;

    // Gear Shot timing (both phases)
    private static final int GEAR_SHOT_COOLDOWN_TICKS = 160; // 8 seconds

    // Ground Slam timing (Phase 2)
    private static final int GROUND_SLAM_COOLDOWN_TICKS = 200; // 10 seconds
    private static final int GROUND_SLAM_RANGE = 10; // blocks
    private static final int GROUND_SLAM_WIDTH = 3; // blocks
    private static final float GROUND_SLAM_DAMAGE = 6.0f;

    // Repair Protocol
    private static final float REPAIR_PROTOCOL_THRESHOLD = 0.4f; // 40% HP
    private static final float REPAIR_PROTOCOL_HEAL = 30.0f; // 15% of max HP
    private static final int REPAIR_PROTOCOL_COOLDOWN = 3600; // 180 seconds (prevents re-trigger)

    public ClockworkColossusEntity(EntityType<? extends ClockworkColossusEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 100;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, PHASE_1);
        builder.define(OVERCHARGED, false);
    }

    /**
     * Create attribute supplier for Clockwork Colossus.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 200.0) // 100 hearts
            .add(Attributes.ARMOR, 8.0)
            .add(Attributes.ATTACK_DAMAGE, 12.0) // 6 hearts
            .add(Attributes.ATTACK_KNOCKBACK, 1.5)
            .add(Attributes.MOVEMENT_SPEED, 0.18)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0); // Immune to knockback
    }

    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Gear Shot (ranged attack) - implemented via RangedAttackMob interface
        // Will be handled in tick() method

        // Priority 2: Ground Slam (Phase 2 only) - implemented in tick()

        // Priority 3: Melee attack
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));

        // Priority 4: Move towards target
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 0.9, 32.0f));

        // Priority 5: Wander around
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));

        // Priority 6: Look at players
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 7: Random look around
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

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

            // Decrement cooldowns
            if (gearShotCooldown > 0) {
                gearShotCooldown--;
            }
            if (groundSlamCooldown > 0) {
                groundSlamCooldown--;
            }
            if (repairCooldown > 0) {
                repairCooldown--;
            }

            // Phase 2 abilities
            if (getPhase() == PHASE_2) {
                handlePhase2Abilities();
            }

            // Gear Shot attack (both phases)
            if (this.getTarget() != null && gearShotCooldown <= 0) {
                double distance = this.distanceTo(this.getTarget());
                if (distance >= 5.0 && distance <= 16.0) {
                    performGearShot(this.getTarget());
                    gearShotCooldown = GEAR_SHOT_COOLDOWN_TICKS;
                }
            }
        }
    }

    /**
     * Update current phase based on health percentage.
     */
    private void updatePhase() {
        float healthPercent = this.getHealth() / this.getMaxHealth();

        int newPhase = healthPercent > 0.5f ? PHASE_1 : PHASE_2;

        if (newPhase != getPhase()) {
            setPhase(newPhase);

            // Transition to Phase 2: Overcharge
            if (newPhase == PHASE_2 && !isOvercharged()) {
                activateOvercharge();
            }
        }
    }

    /**
     * Activate Overcharge: Speed II (permanent).
     */
    private void activateOvercharge() {
        this.entityData.set(OVERCHARGED, true);

        // Apply permanent buff (duration: infinite = 999999 ticks)
        this.addEffect(new MobEffectInstance(
            MobEffects.SPEED,
            999999,
            1 // Speed II
        ));

        // Visual/sound feedback
        this.level().playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            ModSounds.COLOSSUS_ACTIVATE.get(),
            SoundSource.HOSTILE,
            1.0f,
            1.2f
        );

        // Particle effects
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 50; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = this.random.nextDouble() * 2.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    5,
                    0, 0, 0,
                    0.05
                );
            }
        }
    }

    /**
     * Handle Phase 2 special abilities.
     */
    private void handlePhase2Abilities() {
        // Repair Protocol: ONE-TIME at 40% HP
        float healthPercent = this.getHealth() / this.getMaxHealth();
        if (!repairProtocolUsed && healthPercent <= REPAIR_PROTOCOL_THRESHOLD && repairCooldown <= 0) {
            performRepairProtocol();
            repairProtocolUsed = true;
            repairCooldown = REPAIR_PROTOCOL_COOLDOWN;
        }

        // Ground Slam attack
        if (this.getTarget() != null && groundSlamCooldown <= 0) {
            double distance = this.distanceTo(this.getTarget());
            if (distance <= 8.0) {
                performGroundSlam();
                groundSlamCooldown = GROUND_SLAM_COOLDOWN_TICKS;
            }
        }
    }

    /**
     * Repair Protocol: Recover 15% HP (30 HP) once at 40% HP.
     */
    private void performRepairProtocol() {
        this.heal(REPAIR_PROTOCOL_HEAL);

        // Sound effect
        this.level().playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            ModSounds.COLOSSUS_STOMP.get(),
            SoundSource.HOSTILE,
            1.0f,
            1.5f
        );

        // Particle effects
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 30; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = this.random.nextDouble() * 2.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    3,
                    0, 0, 0,
                    0.0
                );
            }
        }
    }

    /**
     * Ground Slam: Linear shockwave towards target.
     */
    private void performGroundSlam() {
        if (this.getTarget() == null) {
            return;
        }

        // Trigger attack animation
        this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

        // Calculate direction towards target
        LivingEntity target = this.getTarget();
        double deltaX = target.getX() - this.getX();
        double deltaZ = target.getZ() - this.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance == 0) return;

        // Normalize direction
        double dirX = deltaX / distance;
        double dirZ = deltaZ / distance;

        // Sound effect
        this.level().playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            ModSounds.BOSS_POWER_UP.get(),
            SoundSource.HOSTILE,
            0.8f,
            0.6f
        );

        // Create shockwave in straight line towards target
        for (int range = 1; range <= GROUND_SLAM_RANGE; range++) {
            double centerX = this.getX() + dirX * range;
            double centerZ = this.getZ() + dirZ * range;

            // Perpendicular direction for width
            double perpX = -dirZ;
            double perpZ = dirX;

            // Check entities in width (3 blocks)
            for (int width = -1; width <= 1; width++) {
                double checkX = centerX + perpX * width;
                double checkZ = centerZ + perpZ * width;

                // Damage players within range
                this.level().getEntitiesOfClass(
                    Player.class,
                    new net.minecraft.world.phys.AABB(
                        checkX - 0.5, this.getY() - 1, checkZ - 0.5,
                        checkX + 0.5, this.getY() + 3, checkZ + 0.5
                    )
                ).forEach(player -> {
                    player.hurt(this.damageSources().mobAttack(this), GROUND_SLAM_DAMAGE);
                    // Small knockback
                    player.knockback(0.3, -dirX, -dirZ);
                });

                // Particle effects
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        checkX,
                        this.getY() + 0.1,
                        checkZ,
                        3,
                        0.2, 0.1, 0.2,
                        0.01
                    );
                }
            }
        }
    }

    /**
     * Perform Gear Shot ranged attack.
     */
    private void performGearShot(LivingEntity target) {
        // Create Gear Projectile
        com.chronodawn.entities.projectiles.GearProjectileEntity gearProjectile =
            new com.chronodawn.entities.projectiles.GearProjectileEntity(this.level(), this);

        // Calculate aim direction
        double deltaX = target.getX() - this.getX();
        double deltaY = target.getY(0.3333333333333333) - gearProjectile.getY();
        double deltaZ = target.getZ() - this.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Set projectile velocity with arc compensation
        gearProjectile.shoot(deltaX, deltaY + horizontalDistance * 0.1, deltaZ, 1.5f, 1.0f);

        // Play attack sound
        this.level().playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            ModSounds.GEAR_LAUNCH.get(),
            SoundSource.HOSTILE,
            1.0f,
            1.0f
        );

        // Spawn the projectile
        this.level().addFreshEntity(gearProjectile);

        // Trigger attack animation
        this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        // Implementation for RangedAttackMob interface
        performGearShot(target);
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    public void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
    }

    public boolean isOvercharged() {
        return this.entityData.get(OVERCHARGED);
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
    protected void customServerAiStep(ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        this.entityData.set(PHASE, tag.getIntOr("Phase", PHASE_1));
        this.entityData.set(OVERCHARGED, tag.getBooleanOr("Overcharged", false));
        this.repairProtocolUsed = tag.getBooleanOr("RepairProtocolUsed", false);
        this.gearShotCooldown = tag.getIntOr("GearShotCooldown", 0);
        this.groundSlamCooldown = tag.getIntOr("GroundSlamCooldown", 0);
        this.repairCooldown = tag.getIntOr("RepairCooldown", 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase());
        tag.putBoolean("Overcharged", isOvercharged());
        tag.putBoolean("RepairProtocolUsed", this.repairProtocolUsed);
        tag.putInt("GearShotCooldown", this.gearShotCooldown);
        tag.putInt("GroundSlamCooldown", this.groundSlamCooldown);
        tag.putInt("RepairCooldown", this.repairCooldown);
    }

    @Override
    public void setCustomName(net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        // Immune to Time Effects (Slowness from Time Clock, Time Arrow, Time Distortion)
        if (effect.getEffect() == MobEffects.SLOWNESS) {
            // Allow self-applied Speed buff but block external Slowness
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);

        // Unprotect Clockwork Depths boss room when defeated
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            BlockProtectionHandler.onBossDefeatedAt(serverLevel, this.blockPosition());
        }
    }
}
