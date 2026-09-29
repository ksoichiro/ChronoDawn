package com.chronodawn.registry;

/**
 * 26.3 override: {@code ComposterBlock.COMPOSTABLES} (the post-registration
 * chance map every other supported version uses) was removed entirely.
 * Compostability is now a {@code DataComponents.COMPOSTABLE} component set on
 * an item at construction time via {@code Item.Properties.compostable(...)},
 * using the named tiers in {@code ContextIntProviders} (COMPOSTABLE_LOW /
 * LOW_MEDIUM / MEDIUM / MEDIUM_HIGH / ALWAYS_ADD_ONE, matching the old
 * 0.3/0.5/0.65/0.85/1.0 chances).
 *
 * The compostable items therefore declare their tier in their own
 * registration in ModItems.java, and there is nothing left to register here.
 */
public class ModComposting {
    public static void register() {
        // Intentionally empty on 26.3 - see class javadoc.
    }

    private ModComposting() {
        throw new UnsupportedOperationException("Utility class");
    }
}
