package com.chronosphere.neoforge;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.entities.bosses.TimeTyrantEntity;
import com.chronosphere.registry.ModEntities;
import com.chronosphere.worldgen.biomes.ChronosphereOverworldRegion;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import terrablender.api.Regions;

@Mod(Chronosphere.MOD_ID)
public class ChronosphereNeoForge {
    public ChronosphereNeoForge(IEventBus modEventBus) {
        Chronosphere.init();

        // Register entity attributes (NeoForge-specific)
        modEventBus.addListener(this::registerEntityAttributes);

        // Register TerraBlender regions (NeoForge-specific)
        modEventBus.addListener(this::commonSetup);

        Chronosphere.LOGGER.info("Chronosphere Mod (NeoForge) initialized");
    }

    /**
     * Register entity attributes for NeoForge.
     * This is required for all custom living entities to have proper attributes.
     */
    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(
            ModEntities.TIME_GUARDIAN.get(),
            TimeGuardianEntity.createAttributes().build()
        );

        event.put(
            ModEntities.TIME_TYRANT.get(),
            TimeTyrantEntity.createAttributes().build()
        );

        Chronosphere.LOGGER.info("Registered entity attributes for NeoForge");
    }

    /**
     * Common setup event for NeoForge.
     * Register TerraBlender regions for biome injection.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register Strange Forest biome region with weight 2 (rare but discoverable)
            Regions.register(new ChronosphereOverworldRegion(
                ResourceLocation.fromNamespaceAndPath("chronosphere", "overworld"),
                2
            ));

            Chronosphere.LOGGER.info("Registered TerraBlender regions for Strange Forest biome");
        });
    }
}
