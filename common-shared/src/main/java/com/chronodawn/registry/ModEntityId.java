/*
 * Copyright (C) 2025 ksoichiro
 *
 * This file is part of Chrono Dawn.
 *
 * Chrono Dawn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Chrono Dawn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chrono Dawn. If not, see <https://www.gnu.org/licenses/>.
 */
package com.chronodawn.registry;

import com.chronodawn.version.CurrentVersion;
import com.chronodawn.version.MinecraftVersion;

import java.util.Arrays;
import java.util.List;

/**
 * Master list of all entity type IDs in the mod.
 * This enum serves as the single source of truth for entity IDs.
 * GameTests use this to verify all defined entities are properly registered.
 */
public enum ModEntityId {
    // === Bosses ===
    TIME_GUARDIAN("time_guardian"),
    CHRONOS_WARDEN("chronos_warden"),
    CLOCKWORK_COLOSSUS("clockwork_colossus"),
    TIME_TYRANT("time_tyrant"),
    TEMPORAL_PHANTOM("temporal_phantom"),
    ENTROPY_KEEPER("entropy_keeper"),

    // === Hostile Mobs ===
    TEMPORAL_WRAITH("temporal_wraith"),
    CLOCKWORK_SENTINEL("clockwork_sentinel"),
    EPOCH_HUSK("epoch_husk"),
    FORGOTTEN_MINUTE("forgotten_minute"),
    CHRONAL_LEECH("chronal_leech"),
    MOMENT_CREEPER("moment_creeper"),
    PARADOX_CRAWLER("paradox_crawler"),
    SECONDHAND_ARCHER("secondhand_archer"),
    TIMELINE_STRIDER("timeline_strider"),

    // === Friendly Mobs ===
    TIME_KEEPER("time_keeper"),
    FLOQ("floq"),
    GLIDE_FISH("glide_fish"),
    CHRONO_TURTLE("chrono_turtle"),
    TIMEBOUND_RABBIT("timebound_rabbit"),
    PULSE_HOG("pulse_hog"),
    SECONDWING_FOWL("secondwing_fowl"),

    // === Projectiles ===
    GEAR_PROJECTILE("gear_projectile"),
    TIME_ARROW("time_arrow"),
    TIME_BLAST("time_blast"),

    // === Vehicles ===
    CHRONO_DAWN_BOAT("chronodawn_boat"),
    CHRONO_DAWN_CHEST_BOAT("chronodawn_chest_boat"),
    ;

    private final String id;
    private final MinecraftVersion minVersion;

    ModEntityId(String id) {
        this(id, MinecraftVersion.MC_1_20_1);
    }

    ModEntityId(String id, MinecraftVersion minVersion) {
        this.id = id;
        this.minVersion = minVersion;
    }

    /**
     * Returns the entity ID string (e.g., "time_guardian").
     */
    public String id() {
        return id;
    }

    /**
     * Returns the minimum Minecraft version required for this entity.
     */
    public MinecraftVersion minVersion() {
        return minVersion;
    }

    /**
     * Checks if this entity is available for the specified Minecraft version.
     */
    public boolean isAvailableFor(MinecraftVersion version) {
        return version.isAtLeast(minVersion);
    }

    /**
     * Returns all entity IDs available for the specified Minecraft version.
     */
    public static List<ModEntityId> availableFor(MinecraftVersion version) {
        return Arrays.stream(values())
                .filter(e -> e.isAvailableFor(version))
                .toList();
    }

    /**
     * Returns all entity IDs available for the current Minecraft version.
     */
    public static List<ModEntityId> availableForCurrent() {
        return availableFor(CurrentVersion.VERSION);
    }
}
