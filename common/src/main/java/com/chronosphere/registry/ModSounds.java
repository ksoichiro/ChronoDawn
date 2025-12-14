package com.chronosphere.registry;

import com.chronosphere.Chronosphere;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Architectury Registry wrapper for custom sounds.
 *
 * This class provides a centralized registry for all custom sound events in the Chronosphere mod.
 * Using Architectury's DeferredRegister ensures cross-loader compatibility between NeoForge and Fabric.
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Chronosphere.MOD_ID, Registries.SOUND_EVENT);

    /**
     * Gear Projectile Impact Sound
     * Used when Clockwork Colossus's gear projectile hits a target.
     * Vanilla sound: minecraft:block/anvil/land
     */
    public static final RegistrySupplier<SoundEvent> GEAR_IMPACT = registerSound("gear_impact");

    /**
     * Clockwork Colossus Stomp Attack Sound
     * Used during Clockwork Colossus's stomp attack.
     * Vanilla sound: minecraft:block/anvil/use
     */
    public static final RegistrySupplier<SoundEvent> COLOSSUS_STOMP = registerSound("colossus_stomp");

    /**
     * Boss Power Up Sound
     * Used when bosses power up or enter new phases.
     * Vanilla sound: minecraft:entity/wither/spawn
     */
    public static final RegistrySupplier<SoundEvent> BOSS_POWER_UP = registerSound("boss_power_up");

    /**
     * Boss Teleport Sound
     * Used when bosses teleport.
     * Vanilla sound: minecraft:entity/enderman/teleport
     */
    public static final RegistrySupplier<SoundEvent> BOSS_TELEPORT = registerSound("boss_teleport");

    /**
     * Boss Cast Spell Sound
     * Used when bosses cast spells.
     * Vanilla sound: minecraft:entity/evoker/cast_spell
     */
    public static final RegistrySupplier<SoundEvent> BOSS_CAST_SPELL = registerSound("boss_cast_spell");

    /**
     * Boss Prepare Summon Sound
     * Used when bosses prepare to summon minions.
     * Vanilla sound: minecraft:entity/evoker/prepare_summon
     */
    public static final RegistrySupplier<SoundEvent> BOSS_PREPARE_SUMMON = registerSound("boss_prepare_summon");

    /**
     * Entropy Keeper Attack Sound
     * Used for Entropy Keeper's special attacks.
     * Vanilla sound: minecraft:block/sculk/place
     */
    public static final RegistrySupplier<SoundEvent> ENTROPY_ATTACK = registerSound("entropy_attack");

    /**
     * Chronos Warden Roar Sound
     * Used when Chronos Warden roars.
     * Vanilla sound: minecraft:entity/warden/roar
     */
    public static final RegistrySupplier<SoundEvent> WARDEN_ROAR = registerSound("warden_roar");

    /**
     * Time Tyrant Bell Sound
     * Used for Time Tyrant's bell attack.
     * Vanilla sound: minecraft:block/bell/use
     */
    public static final RegistrySupplier<SoundEvent> TYRANT_BELL = registerSound("tyrant_bell");

    /**
     * Entropy Keeper Wither Anger Sound
     * Used when Entropy Keeper rages.
     * Vanilla sound: minecraft:entity/wither/ambient
     */
    public static final RegistrySupplier<SoundEvent> ENTROPY_WITHER_ANGER = registerSound("entropy_wither_anger");

    /**
     * Teleporter Warp Sound
     * Used when Clock Tower Teleporter warps players.
     * Vanilla sound: minecraft:entity/enderman/teleport
     */
    public static final RegistrySupplier<SoundEvent> TELEPORTER_WARP = registerSound("teleporter_warp");

    /**
     * Teleporter Charge Start Sound
     * Used when Clock Tower Teleporter charging begins.
     * Vanilla sound: minecraft:block/beacon/activate
     */
    public static final RegistrySupplier<SoundEvent> TELEPORTER_CHARGE_START = registerSound("teleporter_charge_start");

    /**
     * Teleporter Charging Sound
     * Used during Clock Tower Teleporter charging.
     * Vanilla sound: minecraft:block/portal/ambient
     */
    public static final RegistrySupplier<SoundEvent> TELEPORTER_CHARGING = registerSound("teleporter_charging");

    /**
     * Colossus Activate Sound
     * Used when Clockwork Colossus activates special abilities.
     * Vanilla sound: minecraft:block/beacon/activate
     */
    public static final RegistrySupplier<SoundEvent> COLOSSUS_ACTIVATE = registerSound("colossus_activate");

    /**
     * Gear Launch Sound
     * Used when Clockwork Colossus launches gear projectiles.
     * Vanilla sound: minecraft:block/dispenser/launch
     */
    public static final RegistrySupplier<SoundEvent> GEAR_LAUNCH = registerSound("gear_launch");

    /**
     * Gear Ground Impact Sound
     * Used when gear projectile hits the ground.
     * Vanilla sound: minecraft:block/stone/hit
     */
    public static final RegistrySupplier<SoundEvent> GEAR_GROUND_IMPACT = registerSound("gear_ground_impact");

    /**
     * Entropy Burst Sound
     * Used for Entropy Keeper's entropy burst explosion.
     * Vanilla sound: minecraft:entity/generic/explode
     */
    public static final RegistrySupplier<SoundEvent> ENTROPY_BURST = registerSound("entropy_burst");

    /**
     * Phantom Teleport Sound
     * Used when Temporal Phantom teleports.
     * Vanilla sound: minecraft:entity/enderman/teleport
     */
    public static final RegistrySupplier<SoundEvent> PHANTOM_TELEPORT = registerSound("phantom_teleport");

    /**
     * Time Needle Break Sound
     * Used when Chronos Warden's time needle breaks.
     * Vanilla sound: minecraft:block/stone/break
     */
    public static final RegistrySupplier<SoundEvent> TIME_NEEDLE_BREAK = registerSound("time_needle_break");

    /**
     * Time Needle Place Sound
     * Used when Chronos Warden places time needle.
     * Vanilla sound: minecraft:block/stone/place
     */
    public static final RegistrySupplier<SoundEvent> TIME_NEEDLE_PLACE = registerSound("time_needle_place");

    /**
     * Tyrant Phase Change Sound
     * Used when Time Tyrant changes phases.
     * Vanilla sound: minecraft:entity/player/levelup
     */
    public static final RegistrySupplier<SoundEvent> TYRANT_PHASE_CHANGE = registerSound("tyrant_phase_change");

    /**
     * Chrono Aegis Activate Sound
     * Used when Chrono Aegis shield activates.
     * Vanilla sound: minecraft:block/end_portal_frame/fill
     */
    public static final RegistrySupplier<SoundEvent> CHRONO_AEGIS_ACTIVATE = registerSound("chrono_aegis_activate");

    /**
     * Portal Stabilize Spawn Sound
     * Used when Portal Stabilizer creates/stabilizes portal.
     * Vanilla sound: minecraft:block/end_portal/spawn
     */
    public static final RegistrySupplier<SoundEvent> PORTAL_STABILIZE_SPAWN = registerSound("portal_stabilize_spawn");

    /**
     * Portal Stabilize Activate Sound
     * Used when Portal Stabilizer activates.
     * Vanilla sound: minecraft:block/beacon/activate
     */
    public static final RegistrySupplier<SoundEvent> PORTAL_STABILIZE_ACTIVATE = registerSound("portal_stabilize_activate");

    /**
     * Time Compass Chime Sound
     * Used when Time Compass is used.
     * Vanilla sound: minecraft:block/amethyst_block/chime
     */
    public static final RegistrySupplier<SoundEvent> TIME_COMPASS_CHIME = registerSound("time_compass_chime");

    /**
     * Time Compass Update Sound
     * Used when Time Compass updates its target.
     * Vanilla sound: minecraft:ui/cartography_table/take_result
     */
    public static final RegistrySupplier<SoundEvent> TIME_COMPASS_UPDATE = registerSound("time_compass_update");

    /**
     * Time Compass Break Sound
     * Used when Time Compass breaks.
     * Vanilla sound: minecraft:entity/item/break
     */
    public static final RegistrySupplier<SoundEvent> TIME_COMPASS_BREAK = registerSound("time_compass_break");

    /**
     * Helper method to register a sound event.
     *
     * @param name The sound event name (should match the key in sounds.json)
     * @return The registered sound event supplier
     */
    private static RegistrySupplier<SoundEvent> registerSound(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Chronosphere.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    /**
     * Initialize sound registry.
     * This method must be called during mod initialization.
     */
    public static void register() {
        SOUNDS.register();
        Chronosphere.LOGGER.info("Registered ModSounds");
    }
}
