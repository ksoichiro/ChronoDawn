package com.chronodawn.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import com.chronodawn.compat.CompatResourceLocation;

/** Conventional item tags consumed by Chrono Dawn's cross-version gameplay code. */
public final class ConventionalItemTags {
    public static final TagKey<Item> TIME_CRYSTAL = itemTag("gems/time_crystal");
    public static final TagKey<Item> ENTROPY_CRYSTAL = itemTag("gems/entropy_crystal");
    public static final TagKey<Item> TEMPORAL_AMBER_DUST = itemTag("dusts/temporal_amber");

    private ConventionalItemTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, CompatResourceLocation.create("c", path));
    }
}
