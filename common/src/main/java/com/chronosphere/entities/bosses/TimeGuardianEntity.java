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
    private int postTeleportDelay = 0; // Delay after teleport before attacking

    // Teleport timing (Phase 2)
    private static final int TELEPORT_COOLDOWN_TICKS = 100; // 5 seconds
    private static final int POST_TELEPORT_DELAY_TICKS = 15; // 0.75 seconds delay after teleport

    // AoE timing (Phase 2)
    private static final int AOE_COOLDOWN_TICKS = 80; // 4 seconds
    private static final double AOE_RANGE = 4.0; // Reduced from 5.0 for balance
    private static final float AOE_DAMAGE = 4.0f; // Reduced from 6.0 for balance

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
            .add(Attributes.ATTACK_KNOCKBACK, 1.0) // Knockback when attacking
            .add(Attributes.MOVEMENT_SPEED, 0.2)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.8);
    }

    @Override
    protected void registerGoals() {
        // Priority 1: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 2: Melee attack with extended range (3.0 blocks)
        // Balanced for 21x21 boss room with Phase 2 teleport mechanics
        this.goalSelector.addGoal(1, new ExtendedMeleeAttackGoal(this, 1.0, false, 3.0));

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

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean success = super.doHurtTarget(target);

        if (success && target instanceof LivingEntity livingTarget) {
            // Time Guardian's melee attack inflicts temporal slowdown (Slowness I)
            livingTarget.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                100, // 5 seconds (100 ticks)
                0    // Slowness I (amplifier 0)
            ));
        }

        return success;
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

                // Spawn particles (server-side)
                if (this.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 30; i++) {
                        double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                        double offsetY = this.random.nextDouble() * 2.0;
                        double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                        serverLevel.sendParticles(
                            ParticleTypes.PORTAL,
                            this.getX() + offsetX,
                            this.getY() + offsetY,
                            this.getZ() + offsetZ,
                            3, // particle count
                            0, 0, 0, // delta
                            0.0 // speed
                        );
                    }
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
        if (postTeleportDelay > 0) {
            postTeleportDelay--;
        }

        // Teleport ability
        if (teleportCooldown <= 0 && this.getTarget() != null && postTeleportDelay <= 0) {
            performTeleport();
            teleportCooldown = TELEPORT_COOLDOWN_TICKS;
        }

        // AoE ability (only when not in post-teleport delay)
        if (aoeCooldown <= 0 && this.getTarget() != null && postTeleportDelay <= 0) {
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

        // Teleport with particles (server-side)
        if (this.level() instanceof ServerLevel serverLevel) {
            // Particles at departure point
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                20, // particle count
                0.5, 0.5, 0.5, // delta X, Y, Z
                0.1 // speed
            );

            this.teleportTo(validTeleportPos.x, validTeleportPos.y, validTeleportPos.z);

            // Particles at arrival point
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                validTeleportPos.x, validTeleportPos.y + 1.0, validTeleportPos.z,
                20, // particle count
                0.5, 0.5, 0.5, // delta X, Y, Z
                0.1 // speed
            );
        } else {
            this.teleportTo(validTeleportPos.x, validTeleportPos.y, validTeleportPos.z);
        }

        this.level().playSound(
            null,
            validTeleportPos.x, validTeleportPos.y, validTeleportPos.z,
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.HOSTILE,
            1.0f, 1.0f
        );

        // Reset navigation and set post-teleport delay
        this.getNavigation().stop();
        this.postTeleportDelay = POST_TELEPORT_DELAY_TICKS;
    }

    /**
     * Check if a position is safe for teleporting.
     * A safe position has:
     * - Air blocks for entity body (3 blocks high, 2x2 horizontal area)
     * - Solid ground beneath (2x2 area)
     * - Not surrounded by walls (check adjacent blocks)
     * - Within reasonable range of spawn position (to stay in structure)
     *
     * Enhanced safety check to prevent teleporting into walls or tight spaces.
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

        // Time Guardian has a 2x2 horizontal footprint (approximate)
        // Check 2x2 area for ground and body space
        for (int xOffset = 0; xOffset <= 1; xOffset++) {
            for (int zOffset = 0; zOffset <= 1; zOffset++) {
                BlockPos basePos = blockPos.offset(xOffset, 0, zOffset);

                // Check ground beneath (must be solid)
                BlockPos groundPos = basePos.below();
                if (!this.level().getBlockState(groundPos).isSolid()) {
                    return false;
                }

                // Check entity body space (3 blocks high)
                for (int yOffset = 0; yOffset < 3; yOffset++) {
                    BlockPos checkPos = basePos.above(yOffset);
                    var blockState = this.level().getBlockState(checkPos);

                    // Position must be air or passable (not solid blocks)
                    if (blockState.isSolid()) {
                        return false;
                    }
                }
            }
        }

        // Additional check: Ensure not completely surrounded by walls
        // Check 4 cardinal directions at body level (y+1)
        int wallCount = 0;
        BlockPos[] adjacentPositions = {
            blockPos.north(),
            blockPos.south(),
            blockPos.east(),
            blockPos.west()
        };

        for (BlockPos adjacentPos : adjacentPositions) {
            if (this.level().getBlockState(adjacentPos.above()).isSolid()) {
                wallCount++;
            }
        }

        // If surrounded by 3 or 4 walls, position is not safe
        if (wallCount >= 3) {
            return false;
        }

        return true;
    }

    /**
     * Perform Area of Effect attack, damaging nearby players.
     * Time-themed AoE: Inflicts temporal distortion effects (Slowness II + Mining Fatigue)
     */
    private void performAoEAttack() {
        // Trigger attack animation (raise arms)
        this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

        AABB aoeBox = this.getBoundingBox().inflate(AOE_RANGE);

        this.level().getEntitiesOfClass(Player.class, aoeBox).forEach(player -> {
            // Physical damage (reduced from 6.0 to 4.0 for balance)
            player.hurt(this.damageSources().mobAttack(this), AOE_DAMAGE);

            // Time distortion effects: Slowness II (severe slowdown) + Mining Fatigue (cannot break blocks quickly)
            player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                80, // 4 seconds (80 ticks) - shorter duration to avoid overlapping with teleport + AoE combo
                1   // Slowness II (amplifier 1)
            ));
            player.addEffect(new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN,
                80, // 4 seconds (80 ticks) - shorter duration to avoid overlapping with teleport + AoE combo
                0   // Mining Fatigue I (amplifier 0)
            ));
        });

        // Visual feedback - purple circle on ground (time-themed)
        // Must use ServerLevel.sendParticles() when running on server side
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 100; i++) {
                double angle = (Math.PI * 2.0 * i) / 100;
                double offsetX = Math.cos(angle) * AOE_RANGE;
                double offsetZ = Math.sin(angle) * AOE_RANGE;

                // Ground-level purple particles (DRAGON_BREATH for visibility)
                serverLevel.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    this.getX() + offsetX,
                    this.getY() + 0.1,
                    this.getZ() + offsetZ,
                    1, // particle count per position
                    0, 0.05, 0, // delta X, Y, Z
                    0.0 // speed
                );

                // Additional PORTAL particles for time effect
                if (i % 5 == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        this.getX() + offsetX,
                        this.getY() + 0.5,
                        this.getZ() + offsetZ,
                        2, // particle count
                        0, 0, 0, // delta X, Y, Z
                        0.0 // speed
                    );
                }
            }
        }

        // Sound feedback - time magic AoE (using evoker spell sound for magical feel)
        this.level().playSound(
            null,
            this.getX(), this.getY(), this.getZ(),
            SoundEvents.EVOKER_CAST_SPELL,
            SoundSource.HOSTILE,
            1.0f, 1.0f
        );
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    public void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
    }

    /**
     * Check if entity is in post-teleport delay.
     * During this delay, the entity should not attack to prevent
     * unfair hits right after teleporting to the player.
     *
     * @return true if currently in post-teleport delay
     */
    public boolean isInPostTeleportDelay() {
        return this.postTeleportDelay > 0;
    }

    /**
     * Check if player has reached the boss floor (top floor of Desert Clock Tower).
     * Boss bar should only appear when player is on the top floor.
     *
     * Conditions:
     * - Player Y coordinate must be at or above Time Guardian's Y coordinate (not below)
     * - Player Y coordinate must be within 5 blocks above Time Guardian
     * - Player is within horizontal range (20 blocks) of the tower
     *
     * This ensures boss bar only appears when player is on the same floor as Time Guardian,
     * not on lower floors.
     *
     * @param player The player to check
     * @return true if player is on the boss floor
     */
    private boolean isPlayerOnBossFloor(ServerPlayer player) {
        // Y coordinate check: Player must NOT be below Time Guardian (lower floors excluded)
        double yDifference = player.getY() - this.getY();

        // Player is below Time Guardian (on a lower floor) - exclude
        if (yDifference < -2.0) {
            return false;
        }

        // Player is too far above Time Guardian - exclude
        if (yDifference > 5.0) {
            return false;
        }

        // Horizontal distance check: Player must be within tower range (21x21 tower = ~20 block radius)
        double horizontalDistance = Math.sqrt(
            Math.pow(player.getX() - this.getX(), 2) +
            Math.pow(player.getZ() - this.getZ(), 2)
        );
        if (horizontalDistance > 20.0) {
            return false;
        }

        return true;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        // Only add player to boss bar if they've reached the boss floor
        if (isPlayerOnBossFloor(player)) {
            this.bossEvent.addPlayer(player);
        }
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

        // Dynamically manage boss bar visibility based on player position
        // Add players who reached boss floor, remove players who left
        if (this.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                boolean isOnBossFloor = isPlayerOnBossFloor(player);
                boolean hasPlayerInBossEvent = this.bossEvent.getPlayers().contains(player);

                if (isOnBossFloor && !hasPlayerInBossEvent) {
                    // Player reached boss floor - add to boss bar
                    this.bossEvent.addPlayer(player);
                } else if (!isOnBossFloor && hasPlayerInBossEvent) {
                    // Player left boss floor - remove from boss bar
                    this.bossEvent.removePlayer(player);
                }
            }
        }
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
        this.postTeleportDelay = tag.getInt("PostTeleportDelay");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", getPhase());
        tag.putInt("TeleportCooldown", this.teleportCooldown);
        tag.putInt("AoeCooldown", this.aoeCooldown);
        tag.putInt("PostTeleportDelay", this.postTeleportDelay);
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
