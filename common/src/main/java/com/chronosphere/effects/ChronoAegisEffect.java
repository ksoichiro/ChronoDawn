package com.chronosphere.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Chrono Aegis Buff Effect
 *
 * Provides protection against Time Tyrant's abilities:
 * 1. Time Stop Resistance: Reduces Time Tyrant's Time Stop (Slowness V → Slowness II)
 * 2. Dimensional Anchor: Prevents Time Tyrant teleportation for 3s after teleport
 * 3. Temporal Shield: Reduces Time Tyrant's AoE damage by 50%
 * 4. Time Reversal Disruption: Reduces Time Tyrant's HP recovery (10% → 5%)
 * 5. Clarity: Auto-cleanses Slowness/Weakness/Mining Fatigue periodically
 *
 * Duration: 10 minutes (12000 ticks)
 *
 * Task: T238 [US3] Implement Chrono Aegis buff effect
 */
public class ChronoAegisEffect extends MobEffect {
    public ChronoAegisEffect() {
        super(
            MobEffectCategory.BENEFICIAL, // Beneficial effect
            0x4169E1 // Royal Blue color (time/chrono theme)
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Temporarily disabled: Clarity cleansing causes NBT save crash
        // TODO: Implement safe Clarity cleansing mechanism
        return false;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Clarity feature temporarily disabled to prevent crash
        // The crash occurs when removing effects during applyEffectTick
        // while the game is saving entity NBT data

        // TODO: Implement Clarity in a safe way:
        // Option 1: Use LivingEntity.tick() override in a mixin
        // Option 2: Use forge/fabric events for effect removal
        // Option 3: Schedule removal for next tick using entity flags

        return true;
    }
}
