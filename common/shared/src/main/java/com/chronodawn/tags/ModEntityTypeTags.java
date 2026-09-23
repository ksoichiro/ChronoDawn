package com.chronodawn.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;

/** Chrono Dawn's own entity type tags, used to keep custom boss/mob rules modpack-extensible. */
public final class ModEntityTypeTags {
    public static final TagKey<EntityType<?>> TIME_DISTORTION_IMMUNE = entityTypeTag("time_distortion_immune");

    private ModEntityTypeTags() {
    }

    private static TagKey<EntityType<?>> entityTypeTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, path));
    }
}
