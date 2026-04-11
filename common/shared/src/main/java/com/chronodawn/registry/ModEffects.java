package com.chronodawn.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.effects.ChronoAegisEffect;
import com.chronodawn.effects.EntropyEffect;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

/**
 * Architectury Registry wrapper for custom mob effects.
 *
 * This class provides a centralized registry for all custom status effects in the ChronoDawn mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ChronoDawn.MOD_ID, Registries.MOB_EFFECT);

    /**
     * Chrono Aegis Buff - Provides protection against Time Tyrant's abilities.
     *
     * Effects:
     * - Time Stop Resistance: Reduces Time Tyrant's Time Stop (Slowness V → Slowness II)
     * - Dimensional Anchor: Prevents Time Tyrant teleportation for 3s after teleport
     * - Temporal Shield: Reduces Time Tyrant's AoE damage by 50%
     * - Time Reversal Disruption: Reduces Time Tyrant's HP recovery (10% → 5%)
     * - Clarity: Auto-cleanses Slowness/Weakness/Mining Fatigue periodically
     *
     * Duration: 10 minutes (12000 ticks)
     *
     * Task: T238 [US3] Implement Chrono Aegis system
     */
    public static final RegistrySupplier<MobEffect> CHRONO_AEGIS_BUFF = EFFECTS.register(
        "chrono_aegis_buff",
        ChronoAegisEffect::new
    );

    /**
     * Entropy - Damage over time effect applied on hit by the Entropy Crystal Sword.
     *
     * Ticks once per second and deals 1 magic damage. Default duration is 5 seconds
     * (100 ticks). Used by the Tier 2 Entropy Crystal Sword specialization.
     */
    public static final RegistrySupplier<MobEffect> ENTROPY = EFFECTS.register(
        "entropy",
        EntropyEffect::new
    );

    /**
     * Initialize effect registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        EFFECTS.register();
        ChronoDawn.LOGGER.debug("Registered ModEffects");
    }
}
