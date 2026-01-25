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
 * Master list of all block entity type IDs in the mod.
 * This enum serves as the single source of truth for block entity IDs.
 * GameTests use this to verify all defined block entities are properly registered.
 */
public enum ModBlockEntityId {
    CLOCK_TOWER_TELEPORTER("clock_tower_teleporter"),
    BOSS_ROOM_DOOR("boss_room_door"),
    BOSS_ROOM_BOUNDARY_MARKER("boss_room_boundary_marker"),
    ;

    private final String id;
    private final MinecraftVersion minVersion;

    ModBlockEntityId(String id) {
        this(id, MinecraftVersion.MC_1_20_1);
    }

    ModBlockEntityId(String id, MinecraftVersion minVersion) {
        this.id = id;
        this.minVersion = minVersion;
    }

    /**
     * Returns the block entity ID string (e.g., "clock_tower_teleporter").
     */
    public String id() {
        return id;
    }

    /**
     * Returns the minimum Minecraft version required for this block entity.
     */
    public MinecraftVersion minVersion() {
        return minVersion;
    }

    /**
     * Checks if this block entity is available for the specified Minecraft version.
     */
    public boolean isAvailableFor(MinecraftVersion version) {
        return version.isAtLeast(minVersion);
    }

    /**
     * Returns all block entity IDs available for the specified Minecraft version.
     */
    public static List<ModBlockEntityId> availableFor(MinecraftVersion version) {
        return Arrays.stream(values())
                .filter(e -> e.isAvailableFor(version))
                .toList();
    }

    /**
     * Returns all block entity IDs available for the current Minecraft version.
     */
    public static List<ModBlockEntityId> availableForCurrent() {
        return availableFor(CurrentVersion.VERSION);
    }
}
