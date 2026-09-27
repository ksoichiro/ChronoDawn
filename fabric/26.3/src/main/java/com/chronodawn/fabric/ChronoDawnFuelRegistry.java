package com.chronodawn.fabric;

/**
 * 26.3 override: Fabric API's {@code FuelValueEvents} (and the whole
 * {@code fabric-content-registries-v0} tag-based fuel registration API it
 * lived in) is gone from the fabric-api release resolved for 26.3
 * (0.160.6+26.3 -> fabric-content-registries-v0:15.0.4) - no replacement
 * module provides it. Vanilla now exposes fuel burn time as a per-item
 * {@code Item.Properties.cookingFuel(ResourceKey<ContextIntProvider>)}
 * component instead of a bulk tag-based registry, same shape as the
 * composting change (see ModComposting.java).
 *
 * Porting this properly means adding {@code .cookingFuel(...)} to each
 * ChronoDawn wooden item's own {@code createProperties()} in ModItems.java -
 * tracked as follow-up work, not done here. ChronoDawn's own wooden items
 * won't burn as furnace fuel on Fabric 26.3 until that follow-up lands
 * (NeoForge 26.3 is unaffected - its {@code FuelEventHandler} uses NeoForge's
 * own {@code FurnaceFuelBurnTimeEvent}, which still works).
 */
public class ChronoDawnFuelRegistry {
    public static void register() {
        // Intentionally empty on 26.3 - see class javadoc.
    }

    private ChronoDawnFuelRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }
}
