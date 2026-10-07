package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import net.minecraft.world.item.Item;

/**
 * Clockstone Hoe - Tier 1 time-themed farming tool.
 *
 * Basic tier tool crafted from Clockstone.
 * Provides better farming performance than iron equipment but below diamond tier.
 *
 * Properties:
 * - Mining Speed: 6.5f (ClockstoneTier)
 * - Attack Damage: -2.0 (base) + 2.5 (tier bonus) = 0.5
 * - Attack Speed: -1.0 (fast hoe speed)
 * - Durability: 450 uses (ClockstoneTier)
 * - Enchantability: 14
 *
 * Crafting Recipe:
 * - Clockstone x2
 * - Stick x2
 *
 * Reference: tasks.md (T214)
 */
public class ClockstoneHoeItem extends Item {
    public ClockstoneHoeItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Clockstone Hoe item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(450)
                .hoe(ClockstoneTier.INSTANCE, -2.0f, -1.0f)
                .setId(ResourceKey.create(Registries.ITEM,
                    Identifier.fromNamespaceAndPath(ChronoDawn.MOD_ID, "clockstone_hoe")));
    }
}
