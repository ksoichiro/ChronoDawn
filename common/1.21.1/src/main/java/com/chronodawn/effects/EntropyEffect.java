package com.chronodawn.effects;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Entropy MobEffect - damage over time applied by Entropy Crystal Sword on hit.
 *
 * Ticks once per second (20 game ticks) and deals 1 magic damage.
 * Duration is set by the sword when the effect is applied (default 5 seconds).
 *
 * 1.21.1 version: applyEffectTick returns boolean and takes (LivingEntity, int)
 * without ServerLevel. Tick cadence is controlled by shouldApplyEffectTickThisTick.
 */
public class EntropyEffect extends MobEffect {
    private static final int TICK_INTERVAL = 20;

    public EntropyEffect() {
        super(MobEffectCategory.HARMFUL, 0xC7A97A); // tan/sand color
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % TICK_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.isDeadOrDying()) {
            return false;
        }
        DamageSource source = entity.damageSources().magic();
        entity.hurt(source, 1.0f);
        return true;
    }
}
