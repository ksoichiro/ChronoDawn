package com.chronodawn.items.base;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Time Crystal item - Advanced material for equipment crafting.
 *
 * Obtained by mining Time Crystal Ore in the ChronoDawn dimension (Y: 0-48).
 * Used as crafting material for:
 * - Clockstone equipment (swords, tools, armor)
 * - Enhanced time-related items
 *
 * Properties:
 * - Max Stack Size: 64
 * - Rarer than Clockstone (smaller vein size: 3-5)
 *
 * Reference: tasks.md (T211)
 */
public class TimeCrystalItem extends Item {
    public TimeCrystalItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Time Crystal item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(64)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "time_crystal")));
    }
}
