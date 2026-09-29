package com.chronodawn.fabric;

/**
 * 26.3 override: Fabric API's {@code FuelValueEvents} (and the whole
 * {@code fabric-content-registries-v0} tag-based fuel registration API it
 * lived in) is gone, and so is NeoForge's {@code FurnaceFuelBurnTimeEvent}.
 * Vanilla now exposes fuel burn time as a per-item
 * {@code Item.Properties.cookingFuel(ResourceKey<ContextIntProvider>)}
 * component, using the named tiers in {@code ContextIntProviders}.
 *
 * ChronoDawn's wooden items (logs, planks, stairs, slabs, fences, gates,
 * buttons, pressure plates, doors, trapdoors, boats, chest boats, saplings)
 * therefore declare their burn time in their own registration in
 * ModItems.java, and there is nothing left to register here. Items that a
 * modpack adds to the vanilla tags no longer inherit a burn time
 * automatically on 26.3.
 */
public class ChronoDawnFuelRegistry {
    public static void register() {
        // Intentionally empty on 26.3 - see class javadoc.
    }

    private ChronoDawnFuelRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }
}
