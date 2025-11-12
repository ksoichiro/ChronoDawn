package com.chronosphere.fabric;

import com.chronosphere.Chronosphere;
import com.chronosphere.entities.bosses.TimeGuardianEntity;
import com.chronosphere.entities.mobs.ClockworkSentinelEntity;
import com.chronosphere.entities.mobs.TemporalWraithEntity;
import com.chronosphere.entities.mobs.TimeKeeperEntity;
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
        // Boss entities
        FabricDefaultAttributeRegistry.register(
            ModEntities.TIME_GUARDIAN.get(),
            TimeGuardianEntity.createAttributes()
        );

        // Custom mobs
        FabricDefaultAttributeRegistry.register(
            ModEntities.TEMPORAL_WRAITH.get(),
            TemporalWraithEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.CLOCKWORK_SENTINEL.get(),
            ClockworkSentinelEntity.createAttributes()
        );

        FabricDefaultAttributeRegistry.register(
            ModEntities.TIME_KEEPER.get(),
            TimeKeeperEntity.createAttributes()
        );

        Chronosphere.LOGGER.info("Registered entity attributes for Fabric");
    }
}
