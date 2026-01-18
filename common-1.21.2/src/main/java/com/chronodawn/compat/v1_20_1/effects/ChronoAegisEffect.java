package com.chronodawn.compat.v1_20_1.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Chrono Aegis Buff Effect
 *
 * Provides protection against Time Tyrant's abilities:
 * 1. Time Stop Resistance: Reduces Time Tyrant's Time Stop (Slowness V → Slowness II)
 *    - Implemented in: TimeTyrantEntity.handleTimeStopAbility()
 * 2. Dimensional Anchor: Prevents Time Tyrant teleportation for 3s after teleport
 *    - Implemented in: TimeTyrantEntity.handleTeleportAbility()
 * 3. Temporal Shield: Reduces Time Tyrant's AoE damage by 50%
 *    - Implemented in: TimeTyrantEntity.handleAoEAbility()
 * 4. Time Reversal Disruption: Reduces Time Tyrant's HP recovery (10% → 5%)
 *    - Implemented in: TimeTyrantEntity.handleTimeReversalAbility()
 * 5. Clarity: Auto-cleanses Slowness/Weakness/Mining Fatigue every 2 seconds
 *    - Implemented in: EntityEventHandler.handleChronoAegisClarity()
 *
 * Duration: 10 minutes (12000 ticks)
 *
 * Note: This effect class does not use applyEffectTick() to avoid
 * ConcurrentModificationException during entity NBT save. All functionality
 * is implemented in other classes that check for this effect's presence.
 *
 * Task: T238 [US3] Implement Chrono Aegis buff effect
 * Task: T240 [US3] Fix Clarity auto-cleanse feature (using event system)
 */
public class ChronoAegisEffect extends MobEffect {
    public ChronoAegisEffect() {
        super(
            MobEffectCategory.BENEFICIAL, // Beneficial effect
            0x4169E1 // Royal Blue color (time/chrono theme)
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // This method is not used - all functionality is implemented in:
        // - TimeTyrantEntity (effects 1-4)
        // - EntityEventHandler.handleChronoAegisClarity() (effect 5: Clarity)
        //
        // This design avoids ConcurrentModificationException that occurs when
        // calling removeEffect() during applyEffectTick() while the game is
        // saving entity NBT data.
        // 1.20.1 version: void return type
    }
}
