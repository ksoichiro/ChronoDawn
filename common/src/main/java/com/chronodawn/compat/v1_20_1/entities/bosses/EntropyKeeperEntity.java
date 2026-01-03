package com.chronodawn.entities.bosses;

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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
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

import java.util.List;

/**
 * Entropy Keeper Entity - Phase 2 mini-boss
 *
 * Aberrant entity that governs temporal decay and corruption.
 *
 * Stats:
 * - HP: 160 (80 hearts)
 * - Attack: 10
 * - Armor: 6
 * - Speed: 0.20
 * - Knockback Resistance: 0.5
 *
 * Phase 1 (100%-50% HP): Decay Attacks
 * - Decay Aura: 4-block radius Wither I (every 2s, duration 3s)
 * - Corrosion Touch: Melee attacks deal -5 durability to equipment
 * - Temporal Rot: Places 3x3 corruption patches (Slowness II + Poison I)
 *
 * Phase 2 (50%-0% HP): Entropy Acceleration
 * - Degradation: +2 attack damage every 60 seconds (max 3 stacks = +6 damage)
 * - Entropy Burst: ONE-TIME explosion at 30% HP (6-block radius, 10 damage, Wither II 10s)
 *
 * Reference: research.md (Boss 4: Entropy Keeper)
 * Task: T237 [Phase 2] Implement Entropy Keeper
 */
public class EntropyKeeperEntity extends Monster {
    private static final EntityDataAccessor<Integer> DEGRADATION_STACKS =
        SynchedEntityData.defineId(EntropyKeeperEntity.class, EntityDataSerializers.INT);

    private static final int PHASE_1 = 1; // 100%-50% HP
    private static final int PHASE_2 = 2; // 50%-0% HP

    private static final float PHASE_2_THRESHOLD = 0.5f;
    private static final float ENTROPY_BURST_THRESHOLD = 0.3f;
    private static final float DECAY_AURA_RADIUS = 4.0f;
    private static final int DECAY_AURA_INTERVAL = 40; // 2 seconds
    private static final int TEMPORAL_ROT_COOLDOWN = 160; // 8 seconds
    private static final int DEGRADATION_INTERVAL = 1200; // 60 seconds
    private static final int MAX_DEGRADATION_STACKS = 3;

    private final ServerBossEvent bossEvent;
    private int decayAuraTicks = 0;
    private int temporalRotCooldown = 0;
    private int degradationTimer = 0;
    private boolean entropyBurstTriggered = false;

    public EntropyKeeperEntity(EntityType<? extends EntropyKeeperEntity> entityType, Level level) {
        super(entityType, level);
        this.bossEvent = new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS
        );
        this.xpReward = 90;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 160.0)
            .add(Attributes.ATTACK_DAMAGE, 10.0)
            .add(Attributes.ARMOR, 6.0)
            .add(Attributes.MOVEMENT_SPEED, 0.20)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 0.9, 32.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DEGRADATION_STACKS, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Update boss bar
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            // Decay Aura (Passive)
            decayAuraTicks++;
            if (decayAuraTicks >= DECAY_AURA_INTERVAL) {
                applyDecayAura();
                decayAuraTicks = 0;
            }

            // Temporal Rot (corruption patches)
            if (temporalRotCooldown > 0) {
                temporalRotCooldown--;
            } else if (this.getTarget() != null) {
                placeTemporalRot();
                temporalRotCooldown = TEMPORAL_ROT_COOLDOWN;
            }

            // Phase 2 mechanics
            if (getCurrentPhase() == PHASE_2) {
                // Degradation: +2 attack every 60 seconds
                degradationTimer++;
                if (degradationTimer >= DEGRADATION_INTERVAL) {
                    applyDegradation();
                    degradationTimer = 0;
                }

                // Entropy Burst: ONE-TIME at 30% HP
                float healthRatio = this.getHealth() / this.getMaxHealth();
                if (!entropyBurstTriggered && healthRatio <= ENTROPY_BURST_THRESHOLD) {
                    performEntropyBurst();
                    entropyBurstTriggered = true;
                }
            }

            // Spawn decay particles
            if (this.tickCount % 5 == 0 && this.level() instanceof ServerLevel serverLevel) {
                double offsetX = (this.random.nextDouble() - 0.5) * 1.5;
                double offsetY = this.random.nextDouble() * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 1.5;
                serverLevel.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
                );
            }
        }
    }

    private int getCurrentPhase() {
        float healthRatio = this.getHealth() / this.getMaxHealth();
        return healthRatio <= PHASE_2_THRESHOLD ? PHASE_2 : PHASE_1;
    }

    /**
     * Decay Aura: Apply Wither I to nearby players (4-block radius).
     * Applied every 2 seconds, duration 3 seconds.
     */
    private void applyDecayAura() {
        AABB aabb = this.getBoundingBox().inflate(DECAY_AURA_RADIUS);
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, aabb);

        for (Player player : nearbyPlayers) {
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0)); // 3 seconds, Wither I
        }

        // Spawn aura particles
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 10; i++) {
                double angle = (Math.PI * 2 * i) / 10;
                double x = this.getX() + Math.cos(angle) * DECAY_AURA_RADIUS;
                double z = this.getZ() + Math.sin(angle) * DECAY_AURA_RADIUS;
                serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    x, this.getY() + 0.5, z,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
                );
            }
        }
    }

    /**
     * Corrosion Touch: Melee attacks deal extra durability damage.
     * Override doHurtTarget to apply equipment damage.
     */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);

        if (hit && target instanceof Player player) {
            // Deal -5 durability to all equipped items
            player.getInventory().armor.forEach(stack -> {
                if (!stack.isEmpty() && stack.isDamageableItem()) {
                    stack.hurtAndBreak(5, player, player.getEquipmentSlotForItem(stack));
                }
            });

            // Damage held item
            if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().isDamageableItem()) {
                player.getMainHandItem().hurtAndBreak(5, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
        }

        return hit;
    }

    /**
     * Temporal Rot: Place 3x3 corruption patch on ground.
     * Standing on patch: Slowness II + Poison I (3 seconds).
     */
    private void placeTemporalRot() {
        BlockPos targetPos = this.blockPosition();

        // Create 3x3 area of particle effects (visual only, no block changes)
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = targetPos.offset(x, 0, z);
                    serverLevel.sendParticles(
                        ParticleTypes.SCULK_SOUL,
                        pos.getX() + 0.5,
                        pos.getY() + 0.1,
                        pos.getZ() + 0.5,
                        5,
                        0.3, 0.0, 0.3,
                        0.0
                    );
                }
            }
        }

        // Apply effects to players standing in the area
        AABB aabb = new AABB(
            targetPos.getX() - 1, targetPos.getY(), targetPos.getZ() - 1,
            targetPos.getX() + 2, targetPos.getY() + 1, targetPos.getZ() + 2
        );
        List<Player> playersInPatch = this.level().getEntitiesOfClass(Player.class, aabb);

        for (Player player : playersInPatch) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)); // Slowness II, 3s
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0)); // Poison I, 3s
        }

        // Play sound
        this.level().playSound(
            null,
            targetPos,
            ModSounds.ENTROPY_ATTACK.get(),
            SoundSource.HOSTILE,
            1.0f, 0.8f
        );
    }

    /**
     * Degradation: Increase attack damage by 2 every 60 seconds (max 3 stacks).
     */
    private void applyDegradation() {
        int currentStacks = this.entityData.get(DEGRADATION_STACKS);
        if (currentStacks < MAX_DEGRADATION_STACKS) {
            this.entityData.set(DEGRADATION_STACKS, currentStacks + 1);

            // Increase attack damage
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
                10.0 + (currentStacks + 1) * 2.0
            );

            // Play sound
            this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                ModSounds.ENTROPY_WITHER_ANGER.get(),
                SoundSource.HOSTILE,
                1.0f, 1.2f
            );
        }
    }

    /**
     * Entropy Burst: ONE-TIME explosion at 30% HP.
     * 6-block radius, 10 damage, Wither II (10 seconds), high knockback.
     */
    private void performEntropyBurst() {
        AABB aabb = this.getBoundingBox().inflate(6.0);
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, aabb);

        for (LivingEntity entity : nearbyEntities) {
            if (entity == this) continue;

            // Deal damage
            entity.hurt(this.damageSources().mobAttack(this), 10.0f);

            // Apply Wither II
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1)); // 10 seconds, Wither II

            // Apply knockback
            Vec3 direction = entity.position().subtract(this.position()).normalize();
            entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(1.5)));
        }

        // Spawn explosion particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                this.getX(), this.getY() + 1.0, this.getZ(),
                20,
                3.0, 1.0, 3.0,
                0.1
            );
            serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                50,
                3.0, 1.0, 3.0,
                0.2
            );
        }

        // Play explosion sound
        this.level().playSound(
            null,
            this.getX(), this.getY(), this.getZ(),
            ModSounds.ENTROPY_BURST.get(),
            SoundSource.HOSTILE,
            2.0f, 0.8f
        );
    }

    public int getDegradationStacks() {
        return this.entityData.get(DEGRADATION_STACKS);
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
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        this.entropyBurstTriggered = compound.getBoolean("EntropyBurstTriggered");
        this.entityData.set(DEGRADATION_STACKS, compound.getInt("DegradationStacks"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("EntropyBurstTriggered", this.entropyBurstTriggered);
        compound.putInt("DegradationStacks", this.entityData.get(DEGRADATION_STACKS));
    }

    @Override
    public void setCustomName(net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource cause) {
        super.die(cause);

        // Unprotect Entropy Crypt boss room when defeated
        if (!this.level().isClientSide && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            BlockProtectionHandler.onBossDefeatedAt(serverLevel, this.blockPosition());
        }
    }
}
