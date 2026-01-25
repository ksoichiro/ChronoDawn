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
 * Master list of all block IDs in the mod.
 * This enum serves as the single source of truth for block IDs.
 * GameTests use this to verify all defined blocks are properly registered.
 */
public enum ModBlockId {
    // Ores
    CLOCKSTONE_ORE("clockstone_ore"),
    TIME_CRYSTAL_ORE("time_crystal_ore"),

    // Clockstone variants
    CLOCKSTONE_BLOCK("clockstone_block"),
    CLOCKSTONE_STAIRS("clockstone_stairs"),
    CLOCKSTONE_SLAB("clockstone_slab"),
    CLOCKSTONE_WALL("clockstone_wall"),

    // Temporal Bricks variants
    TEMPORAL_BRICKS("temporal_bricks"),
    TEMPORAL_BRICKS_STAIRS("temporal_bricks_stairs"),
    TEMPORAL_BRICKS_SLAB("temporal_bricks_slab"),
    TEMPORAL_BRICKS_WALL("temporal_bricks_wall"),

    // Special blocks
    TIME_CRYSTAL_BLOCK("time_crystal_block"),
    REVERSING_TIME_SANDSTONE("reversing_time_sandstone"),
    FROZEN_TIME_ICE("frozen_time_ice"),
    CLOCKWORK_BLOCK("clockwork_block"),
    TEMPORAL_MOSS("temporal_moss"),
    TEMPORAL_PARTICLE_EMITTER("temporal_particle_emitter"),
    CHRONO_DAWN_PORTAL("chrono_dawn_portal"),
    DECORATIVE_WATER("decorative_water"),

    // Time Wood (regular)
    TIME_WOOD_LOG("time_wood_log"),
    TIME_WOOD("time_wood"),
    STRIPPED_TIME_WOOD_LOG("stripped_time_wood_log"),
    STRIPPED_TIME_WOOD("stripped_time_wood"),
    TIME_WOOD_PLANKS("time_wood_planks"),
    TIME_WOOD_STAIRS("time_wood_stairs"),
    TIME_WOOD_SLAB("time_wood_slab"),
    TIME_WOOD_FENCE("time_wood_fence"),
    TIME_WOOD_FENCE_GATE("time_wood_fence_gate"),
    TIME_WOOD_DOOR("time_wood_door"),
    TIME_WOOD_TRAPDOOR("time_wood_trapdoor"),
    TIME_WOOD_BUTTON("time_wood_button"),
    TIME_WOOD_PRESSURE_PLATE("time_wood_pressure_plate"),
    TIME_WOOD_LEAVES("time_wood_leaves"),
    TIME_WOOD_SAPLING("time_wood_sapling"),

    // Dark Time Wood
    DARK_TIME_WOOD_LOG("dark_time_wood_log"),
    DARK_TIME_WOOD("dark_time_wood"),
    STRIPPED_DARK_TIME_WOOD_LOG("stripped_dark_time_wood_log"),
    STRIPPED_DARK_TIME_WOOD("stripped_dark_time_wood"),
    DARK_TIME_WOOD_PLANKS("dark_time_wood_planks"),
    DARK_TIME_WOOD_STAIRS("dark_time_wood_stairs"),
    DARK_TIME_WOOD_SLAB("dark_time_wood_slab"),
    DARK_TIME_WOOD_FENCE("dark_time_wood_fence"),
    DARK_TIME_WOOD_FENCE_GATE("dark_time_wood_fence_gate"),
    DARK_TIME_WOOD_DOOR("dark_time_wood_door"),
    DARK_TIME_WOOD_TRAPDOOR("dark_time_wood_trapdoor"),
    DARK_TIME_WOOD_BUTTON("dark_time_wood_button"),
    DARK_TIME_WOOD_PRESSURE_PLATE("dark_time_wood_pressure_plate"),
    DARK_TIME_WOOD_LEAVES("dark_time_wood_leaves"),
    DARK_TIME_WOOD_SAPLING("dark_time_wood_sapling"),

    // Ancient Time Wood
    ANCIENT_TIME_WOOD_LOG("ancient_time_wood_log"),
    ANCIENT_TIME_WOOD("ancient_time_wood"),
    STRIPPED_ANCIENT_TIME_WOOD_LOG("stripped_ancient_time_wood_log"),
    STRIPPED_ANCIENT_TIME_WOOD("stripped_ancient_time_wood"),
    ANCIENT_TIME_WOOD_PLANKS("ancient_time_wood_planks"),
    ANCIENT_TIME_WOOD_STAIRS("ancient_time_wood_stairs"),
    ANCIENT_TIME_WOOD_SLAB("ancient_time_wood_slab"),
    ANCIENT_TIME_WOOD_FENCE("ancient_time_wood_fence"),
    ANCIENT_TIME_WOOD_FENCE_GATE("ancient_time_wood_fence_gate"),
    ANCIENT_TIME_WOOD_DOOR("ancient_time_wood_door"),
    ANCIENT_TIME_WOOD_TRAPDOOR("ancient_time_wood_trapdoor"),
    ANCIENT_TIME_WOOD_BUTTON("ancient_time_wood_button"),
    ANCIENT_TIME_WOOD_PRESSURE_PLATE("ancient_time_wood_pressure_plate"),
    ANCIENT_TIME_WOOD_LEAVES("ancient_time_wood_leaves"),
    ANCIENT_TIME_WOOD_SAPLING("ancient_time_wood_sapling"),

    // Crops and plants
    TIME_WHEAT("time_wheat"),
    TIME_WHEAT_BALE("time_wheat_bale"),
    TEMPORAL_ROOT("temporal_root"),
    CHRONO_MELON("chrono_melon"),
    CHRONO_MELON_STEM("chrono_melon_stem"),
    ATTACHED_CHRONO_MELON_STEM("attached_chrono_melon_stem"),
    TIMELESS_MUSHROOM("timeless_mushroom"),
    UNSTABLE_FUNGUS("unstable_fungus"),
    FRUIT_OF_TIME("fruit_of_time"),

    // Flowers
    ORANGE_TIME_BLOSSOM("orange_time_blossom"),
    PINK_TIME_BLOSSOM("pink_time_blossom"),
    PURPLE_TIME_BLOSSOM("purple_time_blossom"),
    DAWN_BELL("dawn_bell"),
    DUSK_BELL("dusk_bell"),

    // Potted plants
    POTTED_TIME_WOOD_SAPLING("potted_time_wood_sapling"),
    POTTED_DARK_TIME_WOOD_SAPLING("potted_dark_time_wood_sapling"),
    POTTED_ANCIENT_TIME_WOOD_SAPLING("potted_ancient_time_wood_sapling"),
    POTTED_ORANGE_TIME_BLOSSOM("potted_orange_time_blossom"),
    POTTED_PINK_TIME_BLOSSOM("potted_pink_time_blossom"),
    POTTED_PURPLE_TIME_BLOSSOM("potted_purple_time_blossom"),
    POTTED_UNSTABLE_FUNGUS("potted_unstable_fungus"),

    // Structure blocks
    BOSS_ROOM_BOUNDARY_MARKER("boss_room_boundary_marker"),
    BOSS_ROOM_DOOR("boss_room_door"),
    CLOCK_TOWER_TELEPORTER("clock_tower_teleporter"),
    ENTROPY_CRYPT_TRAPDOOR("entropy_crypt_trapdoor"),
    ;

    private final String id;
    private final MinecraftVersion minVersion;

    ModBlockId(String id) {
        this(id, MinecraftVersion.MC_1_20_1);
    }

    ModBlockId(String id, MinecraftVersion minVersion) {
        this.id = id;
        this.minVersion = minVersion;
    }

    /**
     * Returns the block ID string (e.g., "clockstone_ore").
     */
    public String id() {
        return id;
    }

    /**
     * Returns the minimum Minecraft version required for this block.
     */
    public MinecraftVersion minVersion() {
        return minVersion;
    }

    /**
     * Checks if this block is available for the specified Minecraft version.
     */
    public boolean isAvailableFor(MinecraftVersion version) {
        return version.isAtLeast(minVersion);
    }

    /**
     * Returns all block IDs available for the specified Minecraft version.
     */
    public static List<ModBlockId> availableFor(MinecraftVersion version) {
        return Arrays.stream(values())
                .filter(e -> e.isAvailableFor(version))
                .toList();
    }

    /**
     * Returns all block IDs available for the current Minecraft version.
     */
    public static List<ModBlockId> availableForCurrent() {
        return availableFor(CurrentVersion.VERSION);
    }
}
