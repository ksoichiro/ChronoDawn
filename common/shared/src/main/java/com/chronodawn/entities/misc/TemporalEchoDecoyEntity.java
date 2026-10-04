package com.chronodawn.entities.misc;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

/**
 * Afterimage left behind by Echoing Time Boots (Temporal Echo).
 *
 * <p>The entity itself is not rendered (NoopRenderer); its only visual is the particle
 * trail emitted here. Nearby mobs that were targeting the wearer are pointed at it, and
 * once it disappears their targeting goals drop it and pick the player back up.
 *
 * <p>It extends {@link Mob} rather than LivingEntity so equipment-slot methods, whose
 * signatures differ across supported versions, are inherited instead of overridden.
 * The entity type is registered with noSave(), so a decoy never outlives a restart.
 */
public class TemporalEchoDecoyEntity extends Mob {

    /** Lifetime of the decoy, which is also how long it holds aggro (3 seconds). */
    public static final int LIFETIME_TICKS = 60;

    public TemporalEchoDecoyEntity(EntityType<? extends Mob> entityType, net.minecraft.world.level.Level level) {
        super(entityType, level);
        this.setNoAi(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.tickCount >= LIFETIME_TICKS) {
            serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                20, 0.3, 0.6, 0.3, 0.05
            );
            this.discard();
            return;
        }
        if (this.tickCount % 2 == 0) {
            serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 0.9, this.getZ(),
                4, 0.25, 0.5, 0.25, 0.0
            );
        }
    }

    // Not pickable so the invisible decoy never blocks the player's clicks or arrows.
    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
