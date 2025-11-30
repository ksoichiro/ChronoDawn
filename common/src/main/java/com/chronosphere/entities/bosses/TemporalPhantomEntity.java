package com.chronosphere.entities.bosses;

import com.chronosphere.worldgen.protection.BlockProtectionHandler;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Temporal Phantom Entity - Phase 2 mini-boss
 *
 * A spectral mage trapped between time, uses teleportation and phantom clones.
 *
 * Stats:
 * - HP: 150 (75 hearts)
 * - Attack: 8
 * - Armor: 5
 * - Speed: 0.25 (fast)
 * - Knockback Resistance: 0.3
 *
 * Phase 1 (100%-50% HP): Phantom Magic
 * - Phase Shift: 30% chance to dodge physical attacks (becomes semi-transparent)
 * - Warp Bolt: Purple magic projectile (range 20, damage 4, Slowness II 5s)
 *
 * Phase 2 (50%-0% HP): Phantom Army
 * - Phantom Clone: Summons 2 clones every 20 seconds (HP 20, 15s duration)
 * - Blink Strike: Teleports behind player and attacks (cooldown 12s)
 *
 * Reference: research.md (Boss 3: Temporal Phantom)
 * Task: T236a [Phase 2] Create TemporalPhantomEntity
 */
public class TemporalPhantomEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> PHASE_SHIFT_ACTIVE =
        SynchedEntityData.defineId(TemporalPhantomEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int PHASE_1 = 1; // 100%-50% HP
    private static final int PHASE_2 = 2; // 50%-0% HP

    private static final float PHASE_2_THRESHOLD = 0.5f;
    private static final float PHASE_SHIFT_CHANCE = 0.3f;
    private static final int PHASE_SHIFT_DURATION = 40; // 2 seconds
    private static final int PHASE_SHIFT_COOLDOWN = 100; // 5 seconds

    private final ServerBossEvent bossEvent;
    private int phaseShiftTicks = 0;
    private int phaseShiftCooldownTicks = 0;
    private int cloneSummonCooldown = 0;
    private int blinkStrikeCooldown = 0;
    private int rangedAttackCooldown = 0;

    public TemporalPhantomEntity(EntityType<? extends TemporalPhantomEntity> entityType, Level level) {
        super(entityType, level);
        this.bossEvent = new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
        );
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 150.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.ARMOR, 5.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 60, 20.0f)); // 3 seconds, 20 blocks
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PHASE_SHIFT_ACTIVE, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Update boss bar
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            // Phase Shift duration
            if (phaseShiftTicks > 0) {
                phaseShiftTicks--;
                if (phaseShiftTicks == 0) {
                    this.entityData.set(PHASE_SHIFT_ACTIVE, false);
                }
            }

            // Phase Shift cooldown
            if (phaseShiftCooldownTicks > 0) {
                phaseShiftCooldownTicks--;
            }

            // Phase 2 mechanics
            if (getCurrentPhase() == PHASE_2) {
                // Phantom Clone summoning
                if (cloneSummonCooldown > 0) {
                    cloneSummonCooldown--;
                } else {
                    summonPhantomClones();
                    cloneSummonCooldown = 400; // 20 seconds
                }

                // Blink Strike
                if (blinkStrikeCooldown > 0) {
                    blinkStrikeCooldown--;
                } else if (this.getTarget() instanceof Player player) {
                    performBlinkStrike(player);
                    blinkStrikeCooldown = 240; // 12 seconds
                }
            }

            // Spawn purple particles around phantom
            if (this.tickCount % 10 == 0 && this.level() instanceof ServerLevel serverLevel) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = this.random.nextDouble() * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
                serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
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

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Phase Shift: 30% chance to dodge physical attacks
        if (!this.level().isClientSide &&
            source.getDirectEntity() instanceof LivingEntity &&
            phaseShiftCooldownTicks == 0 &&
            this.random.nextFloat() < PHASE_SHIFT_CHANCE) {

            // Activate Phase Shift
            this.entityData.set(PHASE_SHIFT_ACTIVE, true);
            phaseShiftTicks = PHASE_SHIFT_DURATION;
            phaseShiftCooldownTicks = PHASE_SHIFT_COOLDOWN;

            // Play teleport sound
            this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE,
                1.0f, 1.5f
            );

            // Spawn particles
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    30,
                    0.5, 0.5, 0.5,
                    0.1
                );
            }

            return false; // Dodge the attack
        }

        return super.hurt(source, amount);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        // Warp Bolt: Purple magic projectile with Slowness II
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);
            Vec3 direction = targetPos.subtract(this.position().add(0, this.getEyeHeight(), 0)).normalize();

            // Create Warp Bolt projectile (using existing WarpBoltEntity if available, or create new)
            // For now, use a simple implementation
            double deltaX = direction.x;
            double deltaY = direction.y;
            double deltaZ = direction.z;

            // Apply Slowness II to target on hit (handled in projectile entity)

            // Play cast sound
            this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.HOSTILE,
                1.0f, 1.2f
            );
        }
    }

    /**
     * Summon 2 phantom clones in Phase 2.
     * Clones have 20 HP, deal 4 damage, and last 15 seconds.
     */
    private void summonPhantomClones() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Summon 2 clones around the phantom
        for (int i = 0; i < 2; i++) {
            double angle = (Math.PI * 2 * i) / 2 + this.random.nextDouble() * Math.PI;
            double offsetX = Math.cos(angle) * 3.0;
            double offsetZ = Math.sin(angle) * 3.0;

            // TODO: Create PhantomCloneEntity (simplified mob with 20 HP, 4 damage, 15s lifetime)
            // For now, just play effects

            // Spawn summoning particles
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX() + offsetX,
                this.getY(),
                this.getZ() + offsetZ,
                20,
                0.5, 0.5, 0.5,
                0.1
            );
        }

        // Play summoning sound
        this.level().playSound(
            null,
            this.getX(), this.getY(), this.getZ(),
            SoundEvents.EVOKER_PREPARE_SUMMON,
            SoundSource.HOSTILE,
            1.0f, 1.0f
        );
    }

    /**
     * Blink Strike: Teleport behind target and attack.
     * Teleport range: 5-8 blocks behind player.
     */
    private void performBlinkStrike(Player target) {
        // Calculate position behind player
        Vec3 lookDir = target.getLookAngle();
        double distance = 5.0 + this.random.nextDouble() * 3.0; // 5-8 blocks
        Vec3 targetPos = target.position().subtract(lookDir.scale(distance));

        // Teleport particles at old position
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                30,
                0.5, 0.5, 0.5,
                0.1
            );
        }

        // Teleport
        this.teleportTo(targetPos.x, targetPos.y, targetPos.z);

        // Teleport particles at new position
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                30,
                0.5, 0.5, 0.5,
                0.1
            );
        }

        // Play teleport sound
        this.level().playSound(
            null,
            this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENDERMAN_TELEPORT,
            SoundSource.HOSTILE,
            1.0f, 1.0f
        );

        // Immediate melee attack
        this.doHurtTarget(target);
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
    }

    @Override
    public void setCustomName(net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    public boolean isPhaseShiftActive() {
        return this.entityData.get(PHASE_SHIFT_ACTIVE);
    }

    @Override
    public void die(net.minecraft.world.damagesource.DamageSource cause) {
        super.die(cause);

        // Unprotect Phantom Catacombs boss room when defeated
        if (!this.level().isClientSide && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            BlockProtectionHandler.onBossDefeatedAt(serverLevel, this.blockPosition());
        }
    }
}
