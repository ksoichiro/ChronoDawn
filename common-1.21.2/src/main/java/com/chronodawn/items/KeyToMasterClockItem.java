package com.chronodawn.items;

import com.chronodawn.ChronoDawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Key to Master Clock item - Key item for accessing Master Clock depths.
 *
 * Obtained from defeating the Time Guardian (mini-boss).
 * Used to open the door at the Master Clock entrance, granting access to the deepest area
 * where the Time Tyrant awaits.
 *
 * Properties:
 * - Max Stack Size: 1
 * - No special use behavior (door opening is handled by block interaction)
 *
 * Reference: data-model.md (Items → Key Items → Key to Master Clock)
 */
public class KeyToMasterClockItem extends Item {
    public KeyToMasterClockItem(Properties properties) {
        super(properties);
    }

    /**
     * Create default properties for Key to Master Clock item.
     *
     * @return Item properties with appropriate settings
     */
    public static Properties createProperties() {
        return new Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(ChronoDawn.MOD_ID, "key_to_master_clock")));
    }
}
