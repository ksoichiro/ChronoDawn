package com.chronodawn.forge.registry;

import com.chronodawn.ChronoDawn;
import com.chronodawn.forge.loot.GrassTimeWheatSeedModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Registry for Global Loot Modifiers in Forge.
 */
public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ChronoDawn.MOD_ID);

    public static final Supplier<Codec<GrassTimeWheatSeedModifier>> GRASS_TIME_WHEAT_SEED =
        LOOT_MODIFIERS.register("grass_time_wheat_seed", () -> GrassTimeWheatSeedModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIERS.register(modEventBus);
        ChronoDawn.LOGGER.debug("Registered ModLootModifiers for Forge");
    }
}
