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
 *
 * <p>Each block can specify optional attributes using a fluent builder pattern:
 * <ul>
 *   <li>{@code cutout()} - for blocks with transparency (saplings, doors, crops)</li>
 *   <li>{@code cutoutMipped()} - for leaves with mipmapping</li>
 *   <li>{@code translucent()} - for glass-like or ice-like blocks</li>
 *   <li>{@code minVersion()} - for version-specific blocks</li>
 * </ul>
 */
public enum ModBlockId {
    // Ores
    CLOCKSTONE_ORE(def("clockstone_ore")),
    TIME_CRYSTAL_ORE(def("time_crystal_ore")),

    // Clockstone variants
    CLOCKSTONE_BLOCK(def("clockstone_block")),
    CLOCKSTONE_STAIRS(def("clockstone_stairs")),
    CLOCKSTONE_SLAB(def("clockstone_slab")),
    CLOCKSTONE_WALL(def("clockstone_wall")),

    // Temporal Bricks variants
    TEMPORAL_BRICKS(def("temporal_bricks")),
    TEMPORAL_BRICKS_STAIRS(def("temporal_bricks_stairs")),
    TEMPORAL_BRICKS_SLAB(def("temporal_bricks_slab")),
    TEMPORAL_BRICKS_WALL(def("temporal_bricks_wall")),

    // Special blocks
    TIME_CRYSTAL_BLOCK(def("time_crystal_block").translucent()),
    REVERSING_TIME_SANDSTONE(def("reversing_time_sandstone")),
    FROZEN_TIME_ICE(def("frozen_time_ice").translucent()),
    CLOCKWORK_BLOCK(def("clockwork_block")),
    TEMPORAL_MOSS(def("temporal_moss")),
    TEMPORAL_PARTICLE_EMITTER(def("temporal_particle_emitter")),
    CHRONO_DAWN_PORTAL(def("chrono_dawn_portal").translucent()),
    DECORATIVE_WATER(def("decorative_water")),

    // Time Wood (regular)
    TIME_WOOD_LOG(def("time_wood_log")),
    TIME_WOOD(def("time_wood")),
    STRIPPED_TIME_WOOD_LOG(def("stripped_time_wood_log")),
    STRIPPED_TIME_WOOD(def("stripped_time_wood")),
    TIME_WOOD_PLANKS(def("time_wood_planks")),
    TIME_WOOD_STAIRS(def("time_wood_stairs")),
    TIME_WOOD_SLAB(def("time_wood_slab")),
    TIME_WOOD_FENCE(def("time_wood_fence")),
    TIME_WOOD_FENCE_GATE(def("time_wood_fence_gate")),
    TIME_WOOD_DOOR(def("time_wood_door").cutout()),
    TIME_WOOD_TRAPDOOR(def("time_wood_trapdoor").cutout()),
    TIME_WOOD_BUTTON(def("time_wood_button")),
    TIME_WOOD_PRESSURE_PLATE(def("time_wood_pressure_plate")),
    TIME_WOOD_LEAVES(def("time_wood_leaves").cutoutMipped()),
    TIME_WOOD_SAPLING(def("time_wood_sapling").cutout()),

    // Dark Time Wood
    DARK_TIME_WOOD_LOG(def("dark_time_wood_log")),
    DARK_TIME_WOOD(def("dark_time_wood")),
    STRIPPED_DARK_TIME_WOOD_LOG(def("stripped_dark_time_wood_log")),
    STRIPPED_DARK_TIME_WOOD(def("stripped_dark_time_wood")),
    DARK_TIME_WOOD_PLANKS(def("dark_time_wood_planks")),
    DARK_TIME_WOOD_STAIRS(def("dark_time_wood_stairs")),
    DARK_TIME_WOOD_SLAB(def("dark_time_wood_slab")),
    DARK_TIME_WOOD_FENCE(def("dark_time_wood_fence")),
    DARK_TIME_WOOD_FENCE_GATE(def("dark_time_wood_fence_gate")),
    DARK_TIME_WOOD_DOOR(def("dark_time_wood_door").cutout()),
    DARK_TIME_WOOD_TRAPDOOR(def("dark_time_wood_trapdoor").cutout()),
    DARK_TIME_WOOD_BUTTON(def("dark_time_wood_button")),
    DARK_TIME_WOOD_PRESSURE_PLATE(def("dark_time_wood_pressure_plate")),
    DARK_TIME_WOOD_LEAVES(def("dark_time_wood_leaves").cutoutMipped()),
    DARK_TIME_WOOD_SAPLING(def("dark_time_wood_sapling").cutout()),

    // Ancient Time Wood
    ANCIENT_TIME_WOOD_LOG(def("ancient_time_wood_log")),
    ANCIENT_TIME_WOOD(def("ancient_time_wood")),
    STRIPPED_ANCIENT_TIME_WOOD_LOG(def("stripped_ancient_time_wood_log")),
    STRIPPED_ANCIENT_TIME_WOOD(def("stripped_ancient_time_wood")),
    ANCIENT_TIME_WOOD_PLANKS(def("ancient_time_wood_planks")),
    ANCIENT_TIME_WOOD_STAIRS(def("ancient_time_wood_stairs")),
    ANCIENT_TIME_WOOD_SLAB(def("ancient_time_wood_slab")),
    ANCIENT_TIME_WOOD_FENCE(def("ancient_time_wood_fence")),
    ANCIENT_TIME_WOOD_FENCE_GATE(def("ancient_time_wood_fence_gate")),
    ANCIENT_TIME_WOOD_DOOR(def("ancient_time_wood_door").cutout()),
    ANCIENT_TIME_WOOD_TRAPDOOR(def("ancient_time_wood_trapdoor").cutout()),
    ANCIENT_TIME_WOOD_BUTTON(def("ancient_time_wood_button")),
    ANCIENT_TIME_WOOD_PRESSURE_PLATE(def("ancient_time_wood_pressure_plate")),
    ANCIENT_TIME_WOOD_LEAVES(def("ancient_time_wood_leaves").cutoutMipped()),
    ANCIENT_TIME_WOOD_SAPLING(def("ancient_time_wood_sapling").cutout()),

    // Crops and plants
    TIME_WHEAT(def("time_wheat").cutout()),
    TIME_WHEAT_BALE(def("time_wheat_bale")),
    TEMPORAL_ROOT(def("temporal_root").cutout()),
    CHRONO_MELON(def("chrono_melon")),
    CHRONO_MELON_STEM(def("chrono_melon_stem").cutout()),
    ATTACHED_CHRONO_MELON_STEM(def("attached_chrono_melon_stem").cutout()),
    TIMELESS_MUSHROOM(def("timeless_mushroom").cutout()),
    UNSTABLE_FUNGUS(def("unstable_fungus").cutout()),
    FRUIT_OF_TIME(def("fruit_of_time")),

    // Flowers
    ORANGE_TIME_BLOSSOM(def("orange_time_blossom").cutout()),
    PINK_TIME_BLOSSOM(def("pink_time_blossom").cutout()),
    PURPLE_TIME_BLOSSOM(def("purple_time_blossom").cutout()),
    DAWN_BELL(def("dawn_bell").cutout()),
    DUSK_BELL(def("dusk_bell").cutout()),

    // Potted plants
    POTTED_TIME_WOOD_SAPLING(def("potted_time_wood_sapling").cutout()),
    POTTED_DARK_TIME_WOOD_SAPLING(def("potted_dark_time_wood_sapling").cutout()),
    POTTED_ANCIENT_TIME_WOOD_SAPLING(def("potted_ancient_time_wood_sapling").cutout()),
    POTTED_ORANGE_TIME_BLOSSOM(def("potted_orange_time_blossom").cutout()),
    POTTED_PINK_TIME_BLOSSOM(def("potted_pink_time_blossom").cutout()),
    POTTED_PURPLE_TIME_BLOSSOM(def("potted_purple_time_blossom").cutout()),
    POTTED_UNSTABLE_FUNGUS(def("potted_unstable_fungus").cutout()),

    // Structure blocks
    BOSS_ROOM_BOUNDARY_MARKER(def("boss_room_boundary_marker")),
    BOSS_ROOM_DOOR(def("boss_room_door").cutout()),
    CLOCK_TOWER_TELEPORTER(def("clock_tower_teleporter")),
    ENTROPY_CRYPT_TRAPDOOR(def("entropy_crypt_trapdoor").cutout()),
    ;

    private final BlockDef def;

    ModBlockId(BlockDef def) {
        this.def = def;
    }

    /**
     * Returns the block ID string (e.g., "clockstone_ore").
     */
    public String id() {
        return def.id;
    }

    /**
     * Returns the minimum Minecraft version required for this block.
     */
    public MinecraftVersion minVersion() {
        return def.minVersion;
    }

    /**
     * Returns the render layer for this block.
     * Used by client-side code to register appropriate render types.
     */
    public RenderLayer renderLayer() {
        return def.renderLayer;
    }

    /**
     * Checks if this block is available for the specified Minecraft version.
     */
    public boolean isAvailableFor(MinecraftVersion version) {
        return version.isAtLeast(def.minVersion);
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

    /**
     * Creates a new BlockDef builder with the specified ID.
     */
    private static BlockDef def(String id) {
        return new BlockDef(id);
    }

    /**
     * Render layer types for blocks.
     * These correspond to Minecraft's RenderType values.
     */
    public enum RenderLayer {
        /** Default solid rendering (opaque blocks) */
        SOLID,
        /** Cutout rendering for blocks with transparency (saplings, doors, crops) */
        CUTOUT,
        /** Cutout with mipmapping for leaves */
        CUTOUT_MIPPED,
        /** Translucent rendering for glass-like or ice-like blocks */
        TRANSLUCENT
    }

    /**
     * Builder class for defining block properties.
     * Allows fluent configuration of optional attributes.
     */
    public static class BlockDef {
        private final String id;
        private MinecraftVersion minVersion = MinecraftVersion.MC_1_20_1;
        private RenderLayer renderLayer = RenderLayer.SOLID;

        BlockDef(String id) {
            this.id = id;
        }

        /**
         * Sets the minimum Minecraft version for this block.
         */
        public BlockDef minVersion(MinecraftVersion version) {
            this.minVersion = version;
            return this;
        }

        /**
         * Sets the render layer to CUTOUT (for transparency without mipmapping).
         */
        public BlockDef cutout() {
            this.renderLayer = RenderLayer.CUTOUT;
            return this;
        }

        /**
         * Sets the render layer to CUTOUT_MIPPED (for leaves with mipmapping).
         */
        public BlockDef cutoutMipped() {
            this.renderLayer = RenderLayer.CUTOUT_MIPPED;
            return this;
        }

        /**
         * Sets the render layer to TRANSLUCENT (for glass-like blocks).
         */
        public BlockDef translucent() {
            this.renderLayer = RenderLayer.TRANSLUCENT;
            return this;
        }
    }
}
