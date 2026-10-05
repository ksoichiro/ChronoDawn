package com.chronodawn.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import com.chronodawn.ChronoDawn;
import com.chronodawn.compat.CompatResourceLocation;

/** Chrono Dawn's own entity type tags, used to keep custom boss/mob rules modpack-extensible. */
public final class ModEntityTypeTags {
    /**
     * Mobs that ignore ambient Time Distortion, both Slowness and Speed. For bosses this
     * covers only the ambient effect. Clockwork Sentinel also rejects Slowness from every
     * other source while it is in this tag (see {@code ClockworkSentinelEntity#canBeAffected}).
     */
    public static final TagKey<EntityType<?>> TIME_DISTORTION_IMMUNE = entityTypeTag("time_distortion_immune");
    /** Mobs that take ambient Slowness two levels weaker. Speed applies unchanged. */
    public static final TagKey<EntityType<?>> TIME_DISTORTION_RESISTANT = entityTypeTag("time_distortion_resistant");

    private ModEntityTypeTags() {
    }

    private static TagKey<EntityType<?>> entityTypeTag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, CompatResourceLocation.create(ChronoDawn.MOD_ID, path));
    }
}
