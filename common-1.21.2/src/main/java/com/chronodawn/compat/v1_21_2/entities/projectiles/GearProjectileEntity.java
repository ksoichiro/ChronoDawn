package com.chronodawn.compat.v1_21_2.entities.projectiles;

import com.chronodawn.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Gear Projectile Entity - Spinning gear fired by Clockwork Colossus
 *
 * A mechanical spinning gear projectile that deals damage and knockback on impact.
 * Similar to Arrow/Trident but with unique visual (rotating gear).
 *
 * Properties:
 * - Damage: 8 (4 hearts)
 * - Speed: 1.5
 * - Range: 16 blocks
 * - Knockback: Medium
 * - Particles: Iron particles on trail
 * - Animation: Rotates while flying
 *
 * Reference: research.md (Boss 2: Clockwork Colossus - Projectile)
 * Task: T235b [P] GearProjectileEntity implementation
 */
public class GearProjectileEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> ROTATION =
        SynchedEntityData.defineId(GearProjectileEntity.class, EntityDataSerializers.FLOAT);

    private static final float DAMAGE = 8.0f;
    private static final float KNOCKBACK = 0.5f;

    private int ticksInAir = 0;

    public GearProjectileEntity(EntityType<? extends GearProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public GearProjectileEntity(Level level, LivingEntity shooter) {
        super(com.chronodawn.registry.ModEntities.GEAR_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Define rotation data for spinning animation
        builder.define(ROTATION, 0.0f);
    }

    /**
     * Shoot the gear projectile towards a target.
     *
     * @param deltaX X-axis direction
     * @param deltaY Y-axis direction
     * @param deltaZ Z-axis direction
     * @param velocity Projectile speed
     * @param inaccuracy Random spread
     */
    public void shoot(double deltaX, double deltaY, double deltaZ, float velocity, float inaccuracy) {
        Vec3 vec3 = (new Vec3(deltaX, deltaY, deltaZ))
            .normalize()
            .add(
                this.random.triangle(0.0, 0.0172275 * (double)inaccuracy),
                this.random.triangle(0.0, 0.0172275 * (double)inaccuracy),
                this.random.triangle(0.0, 0.0172275 * (double)inaccuracy)
            )
            .scale((double)velocity);
        this.setDeltaMovement(vec3);

        double horizontalDistance = vec3.horizontalDistance();
        this.setYRot((float)(Math.atan2(vec3.x, vec3.z) * 180.0 / Math.PI));
        this.setXRot((float)(Math.atan2(vec3.y, horizontalDistance) * 180.0 / Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public void tick() {
        super.tick();

        ticksInAir++;

        // Remove projectile after 100 ticks (5 seconds)
        if (ticksInAir > 100) {
            this.discard();
            return;
        }

        // Spawn iron particles on trail (server-side)
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (ticksInAir % 2 == 0) {
                serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    1,
                    0.1, 0.1, 0.1,
                    0.0
                );
            }
        }

        // Update rotation for visual spinning (both client and server)
        float currentRotation = this.entityData.get(ROTATION);
        this.entityData.set(ROTATION, (currentRotation + 20.0f) % 360.0f);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        Entity owner = this.getOwner();

        // Damage the hit entity
        DamageSource damageSource = this.damageSources().mobProjectile(this, owner instanceof LivingEntity living ? living : null);

        if (entity.hurt(damageSource, DAMAGE)) {
            // Apply knockback
            if (entity instanceof LivingEntity livingEntity) {
                Vec3 movement = this.getDeltaMovement();
                double knockbackX = movement.x * KNOCKBACK;
                double knockbackZ = movement.z * KNOCKBACK;
                livingEntity.knockback(KNOCKBACK, -knockbackX, -knockbackZ);
            }

            // Play impact sound
            this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                ModSounds.GEAR_IMPACT.get(),
                SoundSource.HOSTILE,
                0.5f,
                1.2f
            );

            // Impact particles
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    10,
                    0.2, 0.2, 0.2,
                    0.1
                );
            }
        }

        // Remove projectile after hit
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // Play impact sound
        this.level().playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            ModSounds.GEAR_GROUND_IMPACT.get(),
            SoundSource.HOSTILE,
            0.5f,
            1.0f
        );

        // Impact particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                this.getX(),
                this.getY(),
                this.getZ(),
                5,
                0.1, 0.1, 0.1,
                0.02
            );
        }

        // Remove projectile after hitting block
        this.discard();
    }

    /**
     * Get current rotation angle for rendering.
     *
     * @return Rotation angle in degrees
     */
    public float getRotation() {
        return this.entityData.get(ROTATION);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // Don't hit the shooter
        return super.canHitEntity(entity) && !entity.equals(this.getOwner());
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false; // Projectile cannot be damaged
    }
}
