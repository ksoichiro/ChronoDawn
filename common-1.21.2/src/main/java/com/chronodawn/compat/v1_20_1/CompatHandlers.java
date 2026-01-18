package com.chronodawn.compat.v1_20_1;

import com.chronodawn.compat.v1_20_1.ItemDataHandler120;

/**
 * Factory for version-specific compatibility handlers (Minecraft 1.20.1).
 *
 * This class provides singleton instances of compatibility handlers
 * for the current Minecraft version.
 *
 * Usage:
 * <pre>{@code
 * // Access singleton handlers
 * CompatHandlers.ITEM_DATA.setString(stack, "key", "value");
 * }</pre>
 */
public class CompatHandlers {
    /**
     * Singleton ItemDataHandler instance for Minecraft 1.20.1.
     */
    public static final ItemDataHandler ITEM_DATA = new ItemDataHandler120();

    // Private constructor - static factory only
    private CompatHandlers() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }
}
