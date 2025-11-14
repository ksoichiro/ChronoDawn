package com.chronosphere.fabric;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.entities.bosses.TimeTyrantEntity;
import com.chronosphere.fabric.compat.CustomPortalFabric;
import com.chronosphere.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ChronosphereFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Chronosphere.init();

        // Register entity attributes (Fabric-specific)
        registerEntityAttributes();

        // Initialize Custom Portal API integration
        CustomPortalFabric.init();

        Chronosphere.LOGGER.info("Chronosphere Mod (Fabric) initialized");
    }

    /**
     * Register entity attributes for Fabric.
     * This is required for all custom living entities to have proper attributes.
     */
    private void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(
            ModEntities.TIME_GUARDIAN.get(),
            TimeGuardianEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.TIME_TYRANT.get(),
            TimeTyrantEntity.createAttributes()
        );

        Chronosphere.LOGGER.info("Registered entity attributes for Fabric");
    }
}
