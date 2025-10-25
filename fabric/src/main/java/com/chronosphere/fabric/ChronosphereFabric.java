package com.chronosphere.fabric;

import com.chronosphere.Chronosphere;
import com.chronosphere.fabric.compat.CustomPortalFabric;
import net.fabricmc.api.ModInitializer;

public class ChronosphereFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Chronosphere.init();

        // Initialize Custom Portal API integration
        CustomPortalFabric.init();

        Chronosphere.LOGGER.info("Chronosphere Mod (Fabric) initialized");
    }
}
