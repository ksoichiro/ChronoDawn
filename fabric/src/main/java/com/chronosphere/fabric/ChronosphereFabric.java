package com.chronosphere.fabric;

import com.chronosphere.Chronosphere;
import net.fabricmc.api.ModInitializer;

public class ChronosphereFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Chronosphere.init();
        Chronosphere.LOGGER.info("Chronosphere Mod (Fabric) initialized");
    }
}
