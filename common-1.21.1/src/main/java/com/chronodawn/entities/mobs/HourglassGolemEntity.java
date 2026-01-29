package com.chronodawn.entities.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Hourglass Golem - Iron Golem-like hostile mob.
 *
 * Drops Time Hourglass.
 *
 * Attributes:
 * - Health: 80 (40 hearts)
 * - Attack Damage: 12 (6 hearts)
 * - Movement Speed: 0.25
 * - Follow Range: 32
 * - Knockback Resistance: 1.0
 *
 * Drops:
 * - Time Hourglass (0-1, with looting bonus)
 */
public class HourglassGolemEntity extends Monster {

    private int targetChangeTime;

    public HourglassGolemEntity(EntityType<? extends HourglassGolemEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 80.0)
            .add(Attributes.ATTACK_DAMAGE, 12.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    /**
     * Check if the player is looking at this entity's head (Enderman-like stare detection).
     */
    private boolean isLookingAtMe(Player player) {
        Vec3 viewVec = player.getViewVector(1.0F).normalize();
        Vec3 toEntity = new Vec3(
            this.getX() - player.getX(),
            this.getEyeY() - player.getEyeY(),
            this.getZ() - player.getZ()
        );
        double distance = toEntity.length();
        toEntity = toEntity.normalize();
        double dot = viewVec.dot(toEntity);
        return dot > 1.0 - 0.025 / distance && player.hasLineOfSight(this);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            Player nearestPlayer = this.level().getNearestPlayer(this, 64.0);
            if (nearestPlayer != null && isLookingAtMe(nearestPlayer) && this.getTarget() == null) {
                this.setTarget(nearestPlayer);
                this.targetChangeTime = this.tickCount;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && this.isAlive()) {
            teleportRandomly();
        }
        return result;
    }

    /**
     * Attempt to teleport to a random position within 16 blocks.
     */
    private boolean teleportRandomly() {
        if (this.level().isClientSide() || !this.isAlive()) {
            return false;
        }

        for (int i = 0; i < 16; i++) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 32.0;
            double y = this.getY() + (this.random.nextInt(16) - 8);
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 32.0;

            if (this.tryTeleportTo(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Teleport to specific coordinates if the position is valid.
     */
    private boolean tryTeleportTo(double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);

        while (pos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(pos).blocksMotion()) {
            pos.move(0, -1, 0);
        }

        if (!this.level().getBlockState(pos).blocksMotion()) {
            return false;
        }

        pos.move(0, 1, 0);

        if (!this.level().getBlockState(pos).isAir() || !this.level().getBlockState(pos.above()).isAir()) {
            return false;
        }

        this.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }

    /**
     * Spawn rules: similar to TemporalWraith - allows spawning in ChronoDawn daylight.
     */
    public static boolean checkHourglassGolemSpawnRules(
        EntityType<HourglassGolemEntity> entityType,
        ServerLevelAccessor level,
        MobSpawnType spawnType,
        BlockPos pos,
        RandomSource random
    ) {
        if (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) {
            return false;
        }
        return Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }
}
