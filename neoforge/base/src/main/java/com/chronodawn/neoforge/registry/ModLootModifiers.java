package com.chronodawn.neoforge.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.neoforge.loot.GrassTimeWheatSeedModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Registry for Global Loot Modifiers in NeoForge.
 */
public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
        DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ChronoDawn.MOD_ID);

    public static final Supplier<MapCodec<GrassTimeWheatSeedModifier>> GRASS_TIME_WHEAT_SEED =
        LOOT_MODIFIERS.register("grass_time_wheat_seed", () -> GrassTimeWheatSeedModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIERS.register(modEventBus);
        ChronoDawn.LOGGER.debug("Registered ModLootModifiers for NeoForge");
    }
}
