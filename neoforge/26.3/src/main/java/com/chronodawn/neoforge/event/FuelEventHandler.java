package com.chronodawn.neoforge.event;

import com.chronodawn.ChronoDawn;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * 26.3 override: {@code net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent}
 * is gone (the whole {@code event.furnace} package is now empty) - no
 * replacement event provides tag-based fuel burn time overrides. Vanilla now
 * exposes fuel burn time as a per-item
 * {@code Item.Properties.cookingFuel(ResourceKey<ContextIntProvider>)}
 * component instead, same shape as the composting change (see
 * ModComposting.java) and the Fabric side of this same feature
 * (see ChronoDawnFuelRegistry.java in fabric/26.3).
 *
 * Porting this properly means adding {@code .cookingFuel(...)} to each
 * ChronoDawn wooden item's own {@code createProperties()} in ModItems.java -
 * tracked as follow-up work, not done here. ChronoDawn's own wooden items
 * won't burn as furnace fuel on NeoForge 26.3 until that follow-up lands.
 */
@EventBusSubscriber(modid = ChronoDawn.MOD_ID)
public class FuelEventHandler {
    private FuelEventHandler() {
        throw new UnsupportedOperationException("Utility class");
    }
}
