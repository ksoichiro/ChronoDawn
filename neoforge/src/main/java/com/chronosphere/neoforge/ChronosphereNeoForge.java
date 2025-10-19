package com.chronosphere.neoforge;

import com.chronosphere.Chronosphere;
import net.neoforged.fml.common.Mod;

@Mod(Chronosphere.MOD_ID)
public class ChronosphereNeoForge {
    public ChronosphereNeoForge() {
        Chronosphere.init();
        Chronosphere.LOGGER.info("Chronosphere Mod (NeoForge) initialized");
    }
}
