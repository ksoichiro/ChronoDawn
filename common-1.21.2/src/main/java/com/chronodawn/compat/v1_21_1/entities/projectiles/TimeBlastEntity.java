package com.chronodawn.compat.v1_21_1.entities.projectiles;

import com.chronodawn.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Time Blast Entity - Time Guardian's ranged attack projectile.
 *
 * A time-themed magical projectile fired by Time Guardian during combat.
 * When this projectile hits a player, it applies temporal distortion effects:
 * - Slowness II (5 seconds) - Movement speed reduction
 * - Mining Fatigue I (5 seconds) - Block breaking speed reduction
 *
 * Visual Effects:
 * - Orange/gold particles trailing the projectile (time theme color)
 * - Portal particles on impact
 *
 * Reference: T210 - Add ranged attack capability to Time Guardian
 */
public class TimeBlastEntity extends ThrowableProjectile {
    /**
     * Constructor for creating Time Blast entity.
     *
     * @param entityType The entity type
     * @param level The world level
     */
    public TimeBlastEntity(EntityType<? extends TimeBlastEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Constructor for creating Time Blast entity from a shooter.
     *
     * @param level The world level
     * @param shooter The entity that fired the projectile
     */
    public TimeBlastEntity(Level level, LivingEntity shooter) {
        super(ModEntities.TIME_BLAST.get(), shooter, level);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // No additional synched data needed for Time Blast
    }

    @Override
    public void tick() {
        super.tick();

        // Spawn trailing particles on client side
        if (this.level().isClientSide) {
            // Orange/gold particles trailing the projectile
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0
                );
            }

            // Portal particles for time theme
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1
                );
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        // Only process on server side
        if (!this.level().isClientSide) {
            // Spawn impact particles
            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 1.0;
                double offsetY = (this.random.nextDouble() - 0.5) * 1.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 1.0;

                this.level().addParticle(
                    ParticleTypes.PORTAL,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    offsetX * 0.1,
                    offsetY * 0.1,
                    offsetZ * 0.1
                );
            }

            // Remove projectile after impact
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        // Only apply effects on server side
        if (this.level().isClientSide) {
            return;
        }

        // Apply effects to living entities (typically players)
        if (result.getEntity() instanceof LivingEntity target) {
            // Damage the target (4.0 = 2 hearts)
            target.hurt(this.damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), 4.0f);

            // Apply Slowness II for 5 seconds
            target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                100, // 5 seconds (100 ticks)
                1    // Slowness II (amplifier 1)
            ));

            // Apply Mining Fatigue I for 5 seconds
            target.addEffect(new MobEffectInstance(
                MobEffects.DIG_SLOWDOWN,
                100, // 5 seconds (100 ticks)
                0    // Mining Fatigue I (amplifier 0)
            ));
        }
    }
}
