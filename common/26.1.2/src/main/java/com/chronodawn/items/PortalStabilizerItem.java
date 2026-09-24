package com.chronodawn.items;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/**
 * Portal Stabilizer - Portal utility item.
 *
 * Right-clicking a deactivated Chrono Dawn portal's Clockstone frame with this
 * item (or any other item in the {@code chronodawn:portal_stabilizers} tag)
 * stabilizes the portal, enabling bidirectional travel; see
 * {@link PortalStabilizationHandler} and {@link com.chronodawn.events.BlockEventHandler}
 * for the actual stabilization logic, which is tag-driven rather than tied to
 * this class.
 *
 * Properties:
 * - Max Stack Size: 1
 * - Durability: 1 (consumed after use)
 *
 * Crafting:
 * - Recipe blueprint found in Forgotten Library (ChronoDawn dimension)
 * - Requires Clockstone and other materials (recipe defined in data/chronodawn/recipes/)
 *
 * Reference: data-model.md (Items → Tools & Utilities → Portal Stabilizer)
 * Task: T063 [US1] Create Portal Stabilizer item
 */
public class PortalStabilizerItem extends Item {
    public PortalStabilizerItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Portal Stabilizer.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1) // Single-use item
                .setId(ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "portal_stabilizer")));
    }
}
