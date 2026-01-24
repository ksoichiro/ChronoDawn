package com.chronodawn.entities.mobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * Moment Creeper (モーメント・クリーパー) - Hostile Mob
 *
 * A creeper variant with glass-like body containing a frozen explosion effect.
 * Briefly "completely stops" before exploding. Explosion has less terrain damage
 * but stronger time debuff.
 *
 * Attributes:
 * - Health: 22 (11 hearts)
 * - Attack Damage: N/A (explosion-based)
 * - Movement Speed: 0.25 (standard creeper speed)
 *
 * Special Mechanics:
 * - Briefly freezes before exploding (future implementation)
 * - Explosion has less terrain damage, stronger time debuff (future implementation)
 *
 * Drops:
 * - Compressed Moment (future implementation)
 */
public class MomentCreeperEntity extends Monster {
    // EntityDataAccessor for swell direction (-1: shrinking, 0: stopped, 1: swelling)
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR =
        SynchedEntityData.defineId(MomentCreeperEntity.class, EntityDataSerializers.INT);

    // EntityDataAccessor for swell time (synchronized to client)
    private static final EntityDataAccessor<Integer> DATA_SWELL_TIME =
        SynchedEntityData.defineId(MomentCreeperEntity.class, EntityDataSerializers.INT);

    // Explosion parameters
    private static final int MAX_SWELL_TIME = 30; // 1.5 seconds (30 ticks)
    private static final int EXPLOSION_RADIUS = 3;
    private static final int FREEZE_DURATION = 10; // 0.5 seconds freeze before explosion

    // Instance fields
    private int oldSwellTime = 0; // Previous tick's swell time (client-side for interpolation)
    private boolean isFreezing = false; // Freeze state before explosion

    public MomentCreeperEntity(EntityType<? extends MomentCreeperEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5; // Standard XP reward
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELL_DIR, -1); // Default: not swelling
        builder.define(DATA_SWELL_TIME, 0); // Default: no swelling progress
    }

    /**
     * Define AI goals for Moment Creeper behavior
     */
    @Override
    protected void registerGoals() {
        // Priority 0: Float in water
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Priority 1: Swell when near player
        this.goalSelector.addGoal(1, new MomentCreeperSwellGoal(this));

        // Priority 2: Melee attack (approach player)
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));

        // Priority 3: Random movement
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));

        // Priority 4: Look at player
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));

        // Priority 5: Random look around
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Target selection: Aggressive towards players
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.oldSwellTime = this.getSwellTime();

            if (!this.level().isClientSide) {
                // Update swell direction for client synchronization
                int swellDir = this.getSwellDir();
                int currentSwellTime = this.getSwellTime();

                if (swellDir > 0 && currentSwellTime == 0) {
                    // Play ignite sound when starting to swell
                    this.playSound(net.minecraft.sounds.SoundEvents.CREEPER_PRIMED, 1.0f, 0.5f);
                }

                currentSwellTime += swellDir;

                // Check if freezing before explosion
                if (currentSwellTime >= MAX_SWELL_TIME - FREEZE_DURATION && !this.isFreezing) {
                    this.isFreezing = true;
                    // Apply temporary slowness for freeze effect
                    this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
                }

                // Explode when swell time reaches max
                if (currentSwellTime >= MAX_SWELL_TIME) {
                    currentSwellTime = MAX_SWELL_TIME;
                    this.setSwellTime(currentSwellTime);
                    this.explode();
                    return;
                }

                // Reset swell if target is out of range
                if (currentSwellTime < 0) {
                    currentSwellTime = 0;
                    this.isFreezing = false;
                }

                // Sync swell time to client
                this.setSwellTime(currentSwellTime);
            }
        }

        super.tick();
    }

    /**
     * Get the current swell direction
     * @return 1 if swelling, -1 if shrinking, 0 if stopped
     */
    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    /**
     * Set the swell direction
     */
    public void setSwellDir(int swellDir) {
        this.entityData.set(DATA_SWELL_DIR, swellDir);
    }

    /**
     * Get the current swell time (synchronized from server)
     */
    public int getSwellTime() {
        return this.entityData.get(DATA_SWELL_TIME);
    }

    /**
     * Set the swell time (server-side only, will be synchronized to client)
     */
    public void setSwellTime(int swellTime) {
        this.entityData.set(DATA_SWELL_TIME, swellTime);
    }

    /**
     * Get the current swell progress (0.0 to 1.0)
     */
    public float getSwelling(float partialTick) {
        return net.minecraft.util.Mth.lerp(partialTick, (float)this.oldSwellTime, (float)this.getSwellTime()) / (float)(MAX_SWELL_TIME - 1);
    }

    /**
     * Custom explosion with reduced terrain damage and time debuffs
     */
    private void explode() {
        if (!this.level().isClientSide) {
            // Reduced explosion power: vanilla creeper is 3.0, this is 1.5 (50%)
            float explosionPower = 1.5f;

            // Apply time debuffs to nearby players
            AABB damageBox = this.getBoundingBox().inflate(EXPLOSION_RADIUS);
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, damageBox)) {
                if (entity instanceof Player) {
                    // Apply Slowness II for 10 seconds
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
                    // Apply Mining Fatigue I for 10 seconds
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 0));
                    // Apply Weakness I for 5 seconds
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                }

                // Deal direct damage
                double distance = entity.distanceTo(this);
                if (distance < EXPLOSION_RADIUS) {
                    double damageMultiplier = 1.0 - (distance / EXPLOSION_RADIUS);
                    float damage = (float)((int)((damageMultiplier * damageMultiplier + damageMultiplier) * 7.0 * EXPLOSION_RADIUS + 1.0));
                    entity.hurt(this.damageSources().explosion(this, this), damage);
                }
            }

            // Create explosion effect with reduced terrain damage (MOB interaction)
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                explosionPower, Level.ExplosionInteraction.MOB);

            // Remove entity
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Fuse", (short)this.getSwellTime());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Fuse")) {
            this.setSwellTime(tag.getShort("Fuse"));
        }
    }

    /**
     * Create attribute supplier for Moment Creeper
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 22.0) // 11 hearts
            .add(Attributes.MOVEMENT_SPEED, 0.25) // Standard creeper speed
            .add(Attributes.ATTACK_DAMAGE, 0.0) // No melee damage
            .add(Attributes.FOLLOW_RANGE, 35.0); // Standard follow range
    }

    /**
     * Override spawn rules to allow spawning in daylight
     */
    public static boolean checkMomentCreeperSpawnRules(
        EntityType<MomentCreeperEntity> entityType,
        ServerLevelAccessor level,
        MobSpawnType spawnType,
        net.minecraft.core.BlockPos pos,
        RandomSource random
    ) {
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

    /**
     * AI Goal for Moment Creeper swelling behavior
     */
    static class MomentCreeperSwellGoal extends Goal {
        private final MomentCreeperEntity creeper;

        public MomentCreeperSwellGoal(MomentCreeperEntity creeper) {
            this.creeper = creeper;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.creeper.getTarget();
            return target != null && this.creeper.distanceToSqr(target) < 9.0; // 3 blocks
        }

        @Override
        public void start() {
            this.creeper.setSwellDir(1);
        }

        @Override
        public void stop() {
            this.creeper.setSwellDir(-1);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
    }
}
