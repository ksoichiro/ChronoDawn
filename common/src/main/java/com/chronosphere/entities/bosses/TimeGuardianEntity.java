package com.chronosphere.entities.bosses;

import com.chronosphere.core.time.MobAICanceller;
import com.chronosphere.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Time Guardian (時の番人) - Mini-Boss Entity
 *
 * A powerful guardian that protects the secrets of the Chronosphere.
 *
 * Attributes (from data-model.md):
 * - Health: 200 (100 hearts)
 * - Armor: 10
 * - Attack Damage: 12
 * - Attack Speed: 0.5秒間隔
 * - Movement Speed: 0.2
 *
 * AI Phases:
 * - Phase 1 (HP 100%-50%): Melee-focused attacks
 * - Phase 2 (HP 50%-0%): Teleport attacks + Area of Effect
 *
 * Special Mechanics:
 * - Affected by Slowness IV (dimension-wide effect)
 * - Triggers reversed resonance on defeat (30 seconds)
 *
 * Drops:
 * - Key to Master Clock (100%)
 * - Enhanced Clockstone x3-5 (100%)
 *
 * Reference: data-model.md (Entities - Time Guardian)
 * Task: T110 [US2] Create Time Guardian entity
 */
public class TimeGuardianEntity extends Monster {
    // Boss bar for tracking health
    private final ServerBossEvent bossEvent = new ServerBossEvent(
        this.getDisplayName(),
        BossEvent.BossBarColor.YELLOW,
        BossEvent.BossBarOverlay.PROGRESS
    );

    // Entity data accessor for phase tracking
    private static final EntityDataAccessor<Integer> PHASE =
        SynchedEntityData.defineId(TimeGuardianEntity.class, EntityDataSerializers.INT);

    // Phase constants
    private static final int PHASE_1 = 1; // 100%-50% HP: Melee-focused
    private static final int PHASE_2 = 2; // 50%-0% HP: Teleport + AoE

    // Combat timing
    private int teleportCooldown = 0;
    private int aoeCooldown = 0;

    // Teleport timing (Phase 2)
    private static final int TELEPORT_COOLDOWN_TICKS = 100; // 5 seconds

    // AoE timing (Phase 2)
    private static final int AOE_COOLDOWN_TICKS = 80; // 4 seconds
    private static final double AOE_RANGE = 5.0;
    private static final float AOE_DAMAGE = 6.0f;

    public TimeGuardianEntity(EntityType<? extends TimeGuardianEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 100; // Experience points on defeat
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE, PHASE_1);
    }

    /**
     * Create attribute supplier for Time Guardian.
     * Sets base attributes like health, armor, attack damage, and movement speed.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 200.0) // 100 hearts
            .add(Attributes.ARMOR, 10.0)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.MOVEMENT_SPEED, 0.2)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerGoals() {
        // Priority 1: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 2: Melee attack
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));

        // Priority 3: Move towards target
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9, 32.0f));

        // Priority 4: Wander around
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));

        // Priority 5: Look at players
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 6: Random look around
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

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

            // Phase 2 special abilities
            if (getPhase() == PHASE_2) {
                handlePhase2Abilities();
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

            // Visual/sound feedback for phase transition
            if (newPhase == PHASE_2) {
                this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.WITHER_SPAWN,
                    SoundSource.HOSTILE,
                    1.0f,
                    1.5f
                );

                // Spawn particles
                for (int i = 0; i < 30; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                    double offsetY = this.random.nextDouble() * 2.0;
                    double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                    this.level().addParticle(
                        ParticleTypes.PORTAL,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0, 0
                    );
                }
            }
        }
    }

    /**
     * Handle Phase 2 special abilities (teleport + AoE).
     */
    private void handlePhase2Abilities() {
        // Decrement cooldowns
        if (teleportCooldown > 0) {
            teleportCooldown--;
        }
        if (aoeCooldown > 0) {
            aoeCooldown--;
        }

        // Teleport ability
        if (teleportCooldown <= 0 && this.getTarget() != null) {
            performTeleport();
            teleportCooldown = TELEPORT_COOLDOWN_TICKS;
        }

        // AoE ability
        if (aoeCooldown <= 0 && this.getTarget() != null) {
            performAoEAttack();
            aoeCooldown = AOE_COOLDOWN_TICKS;
        }
    }

    /**
     * Teleport behind or near the target player.
     */
    private void performTeleport() {
        LivingEntity target = this.getTarget();
        if (target == null) return;

        Vec3 targetPos = target.position();
        Vec3 targetLook = target.getLookAngle();

        // Try multiple teleport positions: behind target, then around target
        Vec3[] candidatePositions = {
            targetPos.subtract(targetLook.scale(3.0)),  // Behind target
            targetPos.add(3.0, 0, 0),                    // East
            targetPos.add(-3.0, 0, 0),                   // West
            targetPos.add(0, 0, 3.0),                    // South
            targetPos.add(0, 0, -3.0),                   // North
            targetPos.add(2.0, 0, 2.0),                  // Southeast
            targetPos.add(-2.0, 0, -2.0),                // Northwest
        };

        Vec3 validTeleportPos = null;

        for (Vec3 candidatePos : candidatePositions) {
            if (isSafeTeleportPosition(candidatePos)) {
                validTeleportPos = candidatePos;
                break;
            }
        }

        // If no safe position found, don't teleport
        if (validTeleportPos == null) {
            return;
        }

        // Teleport with particles
        this.level().addParticle(
            ParticleTypes.PORTAL,
            this.getX(), this.getY() + 1.0, this.getZ(),
            0, 0, 0
        );

        this.teleportTo(validTeleportPos.x, validTeleportPos.y, validTeleportPos.z);

        this.level().addParticle(
            ParticleTypes.PORTAL,
            validTeleportPos.x, validTeleportPos.y + 1.0, validTeleportPos.z,
            0, 0, 0
        );

        this.level().playSound(
            null,
            validTeleportPos.x, validTeleportPos.y, validTeleportPos.z,
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.HOSTILE,
            1.0f, 1.0f
        );
    }

    /**
     * Check if a position is safe for teleporting.
     * A safe position has:
     * - Air blocks for entity body (3 blocks high)
     * - Solid ground beneath
     * - Within reasonable range of spawn position (to stay in structure)
     *
     * @param pos The position to check
     * @return true if the position is safe for teleporting
     */
    private boolean isSafeTeleportPosition(Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos);

        // Check if position is too far from spawn (avoid teleporting outside structure)
        // Max distance: 20 blocks horizontally
        double distanceFromSpawn = Math.sqrt(
            Math.pow(blockPos.getX() - this.blockPosition().getX(), 2) +
            Math.pow(blockPos.getZ() - this.blockPosition().getZ(), 2)
        );
        if (distanceFromSpawn > 20.0) {
            return false;
        }

        // Check ground beneath (must be solid)
        BlockPos groundPos = blockPos.below();
        if (!this.level().getBlockState(groundPos).isSolid()) {
            return false;
        }

        // Check entity body space (3 blocks high for safety)
        for (int i = 0; i < 3; i++) {
            BlockPos checkPos = blockPos.above(i);
            var blockState = this.level().getBlockState(checkPos);

            // Position must be air or passable (not solid blocks)
            if (blockState.isSolid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Perform Area of Effect attack, damaging nearby players.
     */
    private void performAoEAttack() {
        AABB aoeBox = this.getBoundingBox().inflate(AOE_RANGE);

        this.level().getEntitiesOfClass(Player.class, aoeBox).forEach(player -> {
            player.hurt(this.damageSources().mobAttack(this), AOE_DAMAGE);
        });

        // Visual feedback
        for (int i = 0; i < 50; i++) {
            double angle = (Math.PI * 2.0 * i) / 50;
            double offsetX = Math.cos(angle) * AOE_RANGE;
            double offsetZ = Math.sin(angle) * AOE_RANGE;

            this.level().addParticle(
                ParticleTypes.SWEEP_ATTACK,
                this.getX() + offsetX,
                this.getY() + 0.5,
                this.getZ() + offsetZ,
                0, 0, 0
            );
        }

        this.level().playSound(
            null,
            this.getX(), this.getY(), this.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundSource.HOSTILE,
            1.0f, 0.8f
        );
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    public void setPhase(int phase) {
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
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        this.entityData.set(PHASE, tag.getInt("Phase"));
        this.teleportCooldown = tag.getInt("TeleportCooldown");
        this.aoeCooldown = tag.getInt("AoeCooldown");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase());
        tag.putInt("TeleportCooldown", this.teleportCooldown);
        tag.putInt("AoeCooldown", this.aoeCooldown);
    }

    @Override
    public void setCustomName(net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect) {
        // Time Guardian can be affected by Slowness IV (dimension-wide effect)
        return super.canBeAffected(effect);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);

        if (!this.level().isClientSide) {
            // Reversed resonance trigger will be implemented in EntityEventHandler (T115)
            // This is handled separately to maintain clean separation of concerns
        }
    }

    @Override
    public boolean isPersistenceRequired() {
        // Boss entities should persist (not despawn)
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // Boss should never despawn
        return false;
    }
}
