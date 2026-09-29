package com.chronodawn.neoforge.event;

import com.chronodawn.ChronoDawn;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 26.3 override: {@code net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent}
 * is gone (the whole {@code event.furnace} package is now empty). Vanilla now
 * exposes fuel burn time as a per-item
 * {@code Item.Properties.cookingFuel(ResourceKey<ContextIntProvider>)}
 * component instead, and ChronoDawn's wooden items declare it in their own
 * registration in ModItems.java (see ChronoDawnFuelRegistry.java in
 * fabric/26.3 for the shared rationale), so there is nothing left to handle
 * here.
 */
@EventBusSubscriber(modid = ChronoDawn.MOD_ID)
public class FuelEventHandler {
    private FuelEventHandler() {
        throw new UnsupportedOperationException("Utility class");
    }
}
