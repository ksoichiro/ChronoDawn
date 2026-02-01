package com.chronodawn.items.equipment;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ShovelItem;

/**
 * Enhanced Clockstone Shovel - Tier 2 time-themed digging tool.
 *
 * Advanced tier tool crafted from Enhanced Clockstone and Time Crystal.
 * Provides diamond-equivalent digging performance with faster mining speed.
 *
 * Properties:
 * - Mining Speed: 7.5f (EnhancedClockstoneTier)
 * - Attack Damage: 1.5 (base) + 3.0 (tier bonus) = 4.5
 * - Attack Speed: -3.0
 * - Durability: 1200 uses (EnhancedClockstoneTier)
 * - Enchantability: 16
 *
 * Crafting Recipe:
 * - Enhanced Clockstone x1
 * - Time Crystal x1
 * - Stick x2
 *
 * Reference: T251 - Create Enhanced Clockstone Shovel (Tier 2 tool)
 */
public class EnhancedClockstoneShovelItem extends ShovelItem {
    public EnhancedClockstoneShovelItem(Properties properties) {
        super(EnhancedClockstoneTier.INSTANCE, 1.5f, -3.0f, properties);
    }

    /**
     * Create default properties for Enhanced Clockstone Shovel item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .durability(1200)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "enhanced_clockstone_shovel")));
    }
}
