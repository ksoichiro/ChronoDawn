package com.chronodawn.registry;

/**
 * 26.3 override: {@code ComposterBlock.COMPOSTABLES} (the post-registration
 * chance map every other supported version uses) was removed entirely.
 * Compostability is now a {@code DataComponents.COMPOSTABLE} component set on
 * an item at construction time via {@code Item.Properties.compostable(...)}
 * (backed by named tiers in {@code ContextIntProviders}: COMPOSTABLE_LOW /
 * LOW_MEDIUM / MEDIUM / MEDIUM_HIGH / ALWAYS_ADD_ONE, matching the old
 * 0.3/0.5/0.65/0.85/1.0 chances), so it can no longer be registered centrally
 * after the fact here.
 *
 * Porting this properly means adding {@code .compostable(...)} to each of the
 * ~30 compostable items' own {@code createProperties()} in ModItems.java -
 * tracked as follow-up work, not done here. Composting is a no-op on 26.3
 * until that follow-up lands.
 */
public class ModComposting {
    public static void register() {
        // Intentionally empty on 26.3 - see class javadoc.
    }

    private ModComposting() {
        throw new UnsupportedOperationException("Utility class");
    }
}
