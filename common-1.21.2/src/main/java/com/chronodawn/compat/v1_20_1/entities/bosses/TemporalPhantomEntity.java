package com.chronodawn.compat.v1_20_1.entities.bosses;

import com.chronodawn.entities.projectiles.TimeBlastEntity;
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
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Temporal Phantom Entity - Phase 2 mini-boss
 *
 * A spectral mage trapped between time, uses teleportation and phantom clones.
 * Specializes in ranged attacks - keeps distance and bombards with magic projectiles.
 *
 * Stats:
 * - HP: 150 (75 hearts)
 * - Attack: 8 (melee - emergency only)
 * - Armor: 5
 * - Speed: 0.3 (fast, mobile)
 * - Knockback Resistance: 0.3
 *
 * Phase 1 (100%-50% HP): Phantom Magic
 * - Phase Shift: 30% chance to dodge physical attacks (becomes semi-transparent)
 * - Warp Bolt: Purple magic projectile (range 20, interval 2s, Slowness II + Mining Fatigue I 5s)
 *
 * Phase 2 (<50% HP): Phantom Army (requires HP below 50%)
 * - Phantom Clone: Summons 2 clones every 20 seconds (HP 20, 15s duration)
 * - Blink Strike: Teleports behind player and attacks (cooldown 12s, safe teleport validation)
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
            .add(Attributes.MOVEMENT_SPEED, 0.3)  // Increased from 0.25 for better mobility
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Ranged attack only (removed melee to prevent AI goal conflicts)
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0, 40, 20.0f)); // 2 seconds, 20 blocks
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE_SHIFT_ACTIVE, false);
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
                ModSounds.BOSS_TELEPORT.get(),
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
        // Warp Bolt: Purple magic projectile with Slowness II (using Time Blast)
        if (this.level() instanceof ServerLevel serverLevel) {
            // Create Time Blast projectile
            TimeBlastEntity blast = new TimeBlastEntity(this.level(), this);

            // Calculate trajectory from phantom's eye position to target's center
            double targetX = target.getX();
            double targetY = target.getY() + target.getEyeHeight() * 0.5;
            double targetZ = target.getZ();

            double shootX = this.getX();
            double shootY = this.getY() + this.getEyeHeight();
            double shootZ = this.getZ();

            double deltaX = targetX - shootX;
            double deltaY = targetY - shootY;
            double deltaZ = targetZ - shootZ;

            // Normalize direction
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            deltaX /= distance;
            deltaY /= distance;
            deltaZ /= distance;

            // Set projectile velocity (1.5 is standard projectile speed)
            blast.setDeltaMovement(deltaX * 1.5, deltaY * 1.5, deltaZ * 1.5);

            // Set projectile position
            blast.setPos(shootX, shootY, shootZ);

            // Spawn projectile
            this.level().addFreshEntity(blast);

            // Play cast sound
            this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                ModSounds.BOSS_CAST_SPELL.get(),
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
            ModSounds.BOSS_PREPARE_SUMMON.get(),
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
        BlockPos targetBlockPos = BlockPos.containing(targetPos);

        // Find safe teleport position
        BlockPos safePos = findSafeGroundPosition(targetBlockPos);
        if (safePos == null) {
            // No safe position found - abort teleport
            return;
        }

        // Teleport using safe position
        if (!teleportToPosition(Vec3.atBottomCenterOf(safePos))) {
            // Teleport failed (entity stuck in blocks) - abort attack
            return;
        }

        // Immediate melee attack after successful teleport
        this.doHurtTarget(target);
    }

    /**
     * Find a safe ground position for teleportation.
     * Searches vertically (±3 blocks) for valid landing spot.
     */
    private BlockPos findSafeGroundPosition(BlockPos startPos) {
        // First try the exact position
        if (isSafeTeleportPosition(startPos)) {
            return startPos;
        }

        // Search upward (for when target is on lower ground)
        for (int offset = 1; offset <= 3; offset++) {
            BlockPos testPos = startPos.above(offset);
            if (isSafeTeleportPosition(testPos)) {
                return testPos;
            }
        }

        // Search downward (for when target is on higher ground)
        for (int offset = 1; offset <= 3; offset++) {
            BlockPos testPos = startPos.below(offset);
            if (isSafeTeleportPosition(testPos)) {
                return testPos;
            }
        }

        return null;
    }

    /**
     * Check if a position is safe for teleportation.
     * Validates solid ground below and 3 blocks of clearance above.
     */
    private boolean isSafeTeleportPosition(BlockPos pos) {
        // Check ground is solid
        if (!this.level().getBlockState(pos.below()).isSolid()) {
            return false;
        }

        // Check space is clear (3 blocks tall - entity height ~2.0 + margin 1.0)
        for (int i = 0; i < 3; i++) {
            BlockState state = this.level().getBlockState(pos.above(i));
            if (state.isSolid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Teleport to the specified position with safety validation.
     * Reverts teleport if entity gets stuck in blocks.
     */
    private boolean teleportToPosition(Vec3 pos) {
        // Store original position for potential revert
        Vec3 originalPos = this.position();

        // Spawn particles at departure
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
        this.teleportTo(pos.x, pos.y, pos.z);

        // Validate position after teleport - check if entity is stuck in blocks
        if (isStuckInBlocks()) {
            // Revert to original position
            this.teleportTo(originalPos.x, originalPos.y, originalPos.z);
            return false;
        }

        // Spawn particles at arrival
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
            this.blockPosition(),
            ModSounds.PHANTOM_TELEPORT.get(),
            SoundSource.HOSTILE,
            1.0f, 1.0f
        );

        return true;
    }

    /**
     * Check if entity is stuck in solid blocks after teleportation.
     */
    private boolean isStuckInBlocks() {
        BlockPos feetPos = this.blockPosition();

        // Check 3x3 horizontal area, 3 blocks vertical (entity height ~2.0 + margin 1.0)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy < 3; dy++) {
                    BlockPos checkPos = feetPos.offset(dx, dy, dz);
                    BlockState state = this.level().getBlockState(checkPos);
                    // Check both isSolid and isSuffocating for comprehensive coverage
                    if (state.isSolid() || state.isSuffocating(this.level(), checkPos)) {
                        return true;
                    }
                }
            }
        }

        return false;
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
