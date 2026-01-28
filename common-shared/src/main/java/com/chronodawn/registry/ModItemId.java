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
 * Master list of all item IDs in the mod.
 * This enum serves as the single source of truth for item IDs.
 * GameTests use this to verify all defined items are properly registered.
 */
public enum ModItemId {
    // === Block Items ===

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
    DECORATIVE_WATER_BUCKET("decorative_water_bucket"),

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
    TIME_WOOD_BOAT("time_wood_boat"),
    TIME_WOOD_CHEST_BOAT("time_wood_chest_boat"),

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
    DARK_TIME_WOOD_BOAT("dark_time_wood_boat"),
    DARK_TIME_WOOD_CHEST_BOAT("dark_time_wood_chest_boat"),

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
    ANCIENT_TIME_WOOD_BOAT("ancient_time_wood_boat"),
    ANCIENT_TIME_WOOD_CHEST_BOAT("ancient_time_wood_chest_boat"),

    // Crops and plants (block items)
    TIME_WHEAT_BALE("time_wheat_bale"),
    CHRONO_MELON("chrono_melon"),
    UNSTABLE_FUNGUS("unstable_fungus"),
    FRUIT_OF_TIME("fruit_of_time"),

    // Flowers
    ORANGE_TIME_BLOSSOM("orange_time_blossom"),
    PINK_TIME_BLOSSOM("pink_time_blossom"),
    PURPLE_TIME_BLOSSOM("purple_time_blossom"),
    DAWN_BELL("dawn_bell"),
    DUSK_BELL("dusk_bell"),

    // Structure blocks
    BOSS_ROOM_BOUNDARY_MARKER("boss_room_boundary_marker"),
    BOSS_ROOM_DOOR("boss_room_door"),
    CLOCK_TOWER_TELEPORTER("clock_tower_teleporter"),
    ENTROPY_CRYPT_TRAPDOOR("entropy_crypt_trapdoor"),

    // === Materials ===
    CLOCKSTONE("clockstone"),
    ENHANCED_CLOCKSTONE("enhanced_clockstone"),
    TIME_CRYSTAL("time_crystal"),
    COLOSSUS_GEAR("colossus_gear"),
    PHANTOM_ESSENCE("phantom_essence"),
    ENTROPY_CORE("entropy_core"),
    GUARDIAN_STONE("guardian_stone"),
    FRAGMENT_OF_STASIS_CORE("fragment_of_stasis_core"),
    EYE_OF_CHRONOS("eye_of_chronos"),

    // === Food ===
    TIME_WHEAT_SEEDS("time_wheat_seeds"),
    TIME_WHEAT("time_wheat"),
    TIME_BREAD("time_bread"),
    ENHANCED_TIME_BREAD("enhanced_time_bread"),
    TIME_FRUIT_PIE("time_fruit_pie"),
    TIME_JAM("time_jam"),
    TIME_WHEAT_COOKIE("time_wheat_cookie"),
    CLOCKWORK_COOKIE("clockwork_cookie"),
    GOLDEN_TIME_WHEAT("golden_time_wheat"),
    TEMPORAL_ROOT("temporal_root"),
    BAKED_TEMPORAL_ROOT("baked_temporal_root"),
    TEMPORAL_ROOT_STEW("temporal_root_stew"),
    CHRONO_MELON_SEEDS("chrono_melon_seeds"),
    CHRONO_MELON_SLICE("chrono_melon_slice"),
    GLISTENING_CHRONO_MELON("glistening_chrono_melon"),
    CHRONO_MELON_JUICE("chrono_melon_juice"),
    TIMELESS_MUSHROOM("timeless_mushroom"),
    TIMELESS_MUSHROOM_SOUP("timeless_mushroom_soup"),
    GLIDE_FISH("glide_fish"),
    COOKED_GLIDE_FISH("cooked_glide_fish"),

    // === Tools ===
    CLOCKSTONE_SWORD("clockstone_sword"),
    CLOCKSTONE_PICKAXE("clockstone_pickaxe"),
    CLOCKSTONE_AXE("clockstone_axe"),
    CLOCKSTONE_SHOVEL("clockstone_shovel"),
    CLOCKSTONE_HOE("clockstone_hoe"),
    ENHANCED_CLOCKSTONE_SWORD("enhanced_clockstone_sword"),
    ENHANCED_CLOCKSTONE_PICKAXE("enhanced_clockstone_pickaxe"),
    ENHANCED_CLOCKSTONE_AXE("enhanced_clockstone_axe"),
    ENHANCED_CLOCKSTONE_SHOVEL("enhanced_clockstone_shovel"),
    ENHANCED_CLOCKSTONE_HOE("enhanced_clockstone_hoe"),
    SPATIALLY_LINKED_PICKAXE("spatially_linked_pickaxe"),

    // === Armor ===
    CLOCKSTONE_HELMET("clockstone_helmet"),
    CLOCKSTONE_CHESTPLATE("clockstone_chestplate"),
    CLOCKSTONE_LEGGINGS("clockstone_leggings"),
    CLOCKSTONE_BOOTS("clockstone_boots"),
    ENHANCED_CLOCKSTONE_HELMET("enhanced_clockstone_helmet"),
    ENHANCED_CLOCKSTONE_CHESTPLATE("enhanced_clockstone_chestplate"),
    ENHANCED_CLOCKSTONE_LEGGINGS("enhanced_clockstone_leggings"),
    ENHANCED_CLOCKSTONE_BOOTS("enhanced_clockstone_boots"),

    // === Artifacts ===
    CHRONOBLADE("chronoblade"),
    TIME_TYRANT_MAIL("time_tyrant_mail"),
    ECHOING_TIME_BOOTS("echoing_time_boots"),
    UNSTABLE_POCKET_WATCH("unstable_pocket_watch"),
    CHRONO_AEGIS("chrono_aegis"),

    // === Special Items ===
    TIME_HOURGLASS("time_hourglass"),
    UNSTABLE_HOURGLASS("unstable_hourglass"),
    PORTAL_STABILIZER("portal_stabilizer"),
    CHRONICLE_BOOK("chronicle_book"),
    TIME_CLOCK("time_clock"),
    TIME_COMPASS("time_compass"),
    TIME_ARROW("time_arrow"),
    KEY_TO_MASTER_CLOCK("key_to_master_clock"),
    ANCIENT_GEAR("ancient_gear"),

    // === Spawn Eggs ===
    TIME_GUARDIAN_SPAWN_EGG("time_guardian_spawn_egg"),
    TIME_KEEPER_SPAWN_EGG("time_keeper_spawn_egg"),
    CLOCKWORK_SENTINEL_SPAWN_EGG("clockwork_sentinel_spawn_egg"),
    FLOQ_SPAWN_EGG("floq_spawn_egg"),
    CHRONO_TURTLE_SPAWN_EGG("chrono_turtle_spawn_egg"),
    TIMEBOUND_RABBIT_SPAWN_EGG("timebound_rabbit_spawn_egg"),
    PULSE_HOG_SPAWN_EGG("pulse_hog_spawn_egg"),
    SECONDWING_FOWL_SPAWN_EGG("secondwing_fowl_spawn_egg"),
    GLIDE_FISH_SPAWN_EGG("glide_fish_spawn_egg"),
    TIMELINE_STRIDER_SPAWN_EGG("timeline_strider_spawn_egg"),
    EPOCH_HUSK_SPAWN_EGG("epoch_husk_spawn_egg"),
    MOMENT_CREEPER_SPAWN_EGG("moment_creeper_spawn_egg"),
    SECONDHAND_ARCHER_SPAWN_EGG("secondhand_archer_spawn_egg"),
    CHRONAL_LEECH_SPAWN_EGG("chronal_leech_spawn_egg"),
    FORGOTTEN_MINUTE_SPAWN_EGG("forgotten_minute_spawn_egg"),
    PARADOX_CRAWLER_SPAWN_EGG("paradox_crawler_spawn_egg"),
    TEMPORAL_WRAITH_SPAWN_EGG("temporal_wraith_spawn_egg"),
    TIME_TYRANT_SPAWN_EGG("time_tyrant_spawn_egg"),
    CHRONOS_WARDEN_SPAWN_EGG("chronos_warden_spawn_egg"),
    CLOCKWORK_COLOSSUS_SPAWN_EGG("clockwork_colossus_spawn_egg"),
    TEMPORAL_PHANTOM_SPAWN_EGG("temporal_phantom_spawn_egg"),
    ENTROPY_KEEPER_SPAWN_EGG("entropy_keeper_spawn_egg"),
    ;

    private final String id;
    private final MinecraftVersion minVersion;

    ModItemId(String id) {
        this(id, MinecraftVersion.MC_1_20_1);
    }

    ModItemId(String id, MinecraftVersion minVersion) {
        this.id = id;
        this.minVersion = minVersion;
    }

    /**
     * Returns the item ID string (e.g., "clockstone").
     */
    public String id() {
        return id;
    }

    /**
     * Returns the minimum Minecraft version required for this item.
     */
    public MinecraftVersion minVersion() {
        return minVersion;
    }

    /**
     * Checks if this item is available for the specified Minecraft version.
     */
    public boolean isAvailableFor(MinecraftVersion version) {
        return version.isAtLeast(minVersion);
    }

    /**
     * Returns all item IDs available for the specified Minecraft version.
     */
    public static List<ModItemId> availableFor(MinecraftVersion version) {
        return Arrays.stream(values())
                .filter(e -> e.isAvailableFor(version))
                .toList();
    }

    /**
     * Returns all item IDs available for the current Minecraft version.
     */
    public static List<ModItemId> availableForCurrent() {
        return availableFor(CurrentVersion.VERSION);
    }
}
