package com.chronosphere.neoforge;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.registry.ModEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(Chronosphere.MOD_ID)
public class ChronosphereNeoForge {
    public ChronosphereNeoForge(IEventBus modEventBus) {
        Chronosphere.init();

        // Register entity attributes (NeoForge-specific)
        modEventBus.addListener(this::registerEntityAttributes);

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

        Chronosphere.LOGGER.info("Registered entity attributes for NeoForge");
    }
}
