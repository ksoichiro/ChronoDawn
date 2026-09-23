package com.chronodawn.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;

/** Chrono Dawn's own item tags, used to keep custom mob behavior modpack-extensible. */
public final class ModItemTags {
    public static final TagKey<Item> PULSE_HOG_FOOD = itemTag("pulse_hog_food");
    public static final TagKey<Item> SECONDWING_FOWL_FOOD = itemTag("secondwing_fowl_food");
    public static final TagKey<Item> TIMEBOUND_RABBIT_FOOD = itemTag("timebound_rabbit_food");
    public static final TagKey<Item> TEMPORAL_CAPRID_FOOD = itemTag("temporal_caprid_food");
    public static final TagKey<Item> TICKING_SHEEP_SHEARS = itemTag("ticking_sheep_shears");
    public static final TagKey<Item> CHRONO_BOVINE_MILKING_BUCKETS = itemTag("chrono_bovine_milking_buckets");

    private ModItemTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, CompatResourceLocation.create(ChronoDawn.MOD_ID, path));
    }
}
