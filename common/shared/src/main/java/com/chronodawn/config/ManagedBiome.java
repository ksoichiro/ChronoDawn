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
package com.chronodawn.config;

import com.chronodawn.ChronoDawn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Every biome of the Chrono dimension whose generation Chrono Dawn exposes to
 * configuration.
 *
 * <p>This enum is the single source of truth for that set: the config parser, the
 * runtime overlay and the guard tests all iterate it. A biome that is not
 * registered here appears in none of them, and one that is registered appears in
 * all of them — so a biome cannot be half-wired.
 *
 * <p>Deliberately absent: the multi-noise parameters. Those stay in the bundled
 * dimension JSON, which {@code RuntimeBiomeOverlay} transforms rather than
 * regenerates, so they are never duplicated into Java.
 *
 * <p>Disabling a biome remaps its region of the parameter space to
 * {@link #fallback()} rather than leaving a hole, so the replacement is
 * predictable. {@link #PLAINS} and {@link #OCEAN} are core: they terminate every
 * fallback chain, so they cannot be disabled and get no config table at all.
 */
public enum ManagedBiome {
    // Fallbacks are declared by name, not by constant reference: the JLS forbids an
    // enum constant's initializer from referencing a later-declared sibling, even
    // through a lambda, and these chains point forward. A String literal is not a
    // reference to the constant, so valueOf() in fallback() sidesteps the restriction.
    PLAINS("plains", "chronodawn_plains", null, "", null),
    OCEAN("ocean", "chronodawn_ocean", null, "", null),
    FOREST("forest", "chronodawn_forest", "PLAINS", "", ChronoDawnConfig.Biomes::forest),
    DARK_FOREST("dark_forest", "chronodawn_dark_forest", "FOREST", "Dark Time Wood trees",
        ChronoDawnConfig.Biomes::darkForest),
    ANCIENT_FOREST("ancient_forest", "chronodawn_ancient_forest", "DARK_FOREST", "",
        ChronoDawnConfig.Biomes::ancientForest),
    SWAMP("swamp", "chronodawn_swamp", "FOREST", "", ChronoDawnConfig.Biomes::swamp),
    FADED_PLAINS("faded_plains", "chronodawn_faded_plains", "PLAINS",
        "the Parched Temporal Dirt, Faded Grass, and Temporal Dead Bush blocks",
        ChronoDawnConfig.Biomes::fadedPlains),
    DESERT("desert", "chronodawn_desert", "FADED_PLAINS", "the Hourglass Monolith landmark",
        ChronoDawnConfig.Biomes::desert),
    PRAIRIES("prairies", "chronodawn_prairies", "PLAINS",
        "the Coarse Temporal Dirt and Tall Grass blocks", ChronoDawnConfig.Biomes::prairies),
    SNOWY("snowy", "chronodawn_snowy", "PLAINS", "the Chrono Ursid and Frozen Time Ice",
        ChronoDawnConfig.Biomes::snowy),
    MOUNTAIN("mountain", "chronodawn_mountain", "PLAINS", "the Temporal Caprid",
        ChronoDawnConfig.Biomes::mountain);

    /** The data-pack-relative path of the dimension JSON the overlay replaces. */
    public static final String DIMENSION_PACK_PATH =
        "data/" + ChronoDawn.MOD_ID + "/dimension/" + ChronoDawn.MOD_ID + ".json";

    private final String configKey;
    private final String biomePath;
    private final String fallbackName;
    private final String contentNote;
    private final Function<ChronoDawnConfig.Biomes, BiomeSettings> accessor;

    ManagedBiome(
        String configKey, String biomePath, String fallbackName, String contentNote,
        Function<ChronoDawnConfig.Biomes, BiomeSettings> accessor
    ) {
        this.configKey = configKey;
        this.biomePath = biomePath;
        this.fallbackName = fallbackName;
        this.contentNote = contentNote;
        this.accessor = accessor;
    }

    /**
     * The TOML table name under {@code [world.biomes]}.
     *
     * <p>Core biomes have one too — it names them in logs and guard tests — but no
     * table is written for them, which is how the config states that they cannot be
     * disabled.
     */
    public String configKey() {
        return configKey;
    }

    /** The registry ID of this biome, as it appears in the dimension JSON. */
    public String biomeId() {
        return ChronoDawn.MOD_ID + ":" + biomePath;
    }

    /** What a pack loses by disabling this biome; empty when nothing becomes unobtainable. */
    public String contentNote() {
        return contentNote;
    }

    /** Whether this biome terminates fallback chains and therefore cannot be disabled. */
    public boolean isCore() {
        return fallbackName == null;
    }

    /** The biome this one's parameter-space region is remapped to when disabled. */
    public Optional<ManagedBiome> fallback() {
        return fallbackName == null ? Optional.empty() : Optional.of(ManagedBiome.valueOf(fallbackName));
    }

    /**
     * Walks the fallback chain until it reaches a biome the given predicate accepts.
     *
     * <p>Terminates because every chain ends at a core biome and core biomes are
     * always enabled — a property {@code ManagedBiomeTest} asserts directly. The
     * visited set is a backstop against a malformed chain hanging the server.
     *
     * @param enabled tells whether a biome is enabled in the active config
     * @return this biome if it is enabled, otherwise the first enabled biome down the chain
     */
    public ManagedBiome resolveFallback(Predicate<ManagedBiome> enabled) {
        Set<ManagedBiome> visited = new HashSet<>();
        ManagedBiome current = this;
        while (!enabled.test(current)) {
            if (!visited.add(current)) {
                throw new IllegalStateException("Fallback chain loops at " + current);
            }
            ManagedBiome disabled = current;
            current = current.fallback()
                .orElseThrow(() -> new IllegalStateException(
                    "Core biome reported as disabled: " + disabled));
        }
        return current;
    }

    /** Reads this biome's settings out of a parsed config. Core biomes are always enabled. */
    public BiomeSettings settingsOf(ChronoDawnConfig.Biomes biomes) {
        return accessor == null ? new BiomeSettings(true) : accessor.apply(biomes);
    }

    /** The biomes a pack can toggle — every biome except the core ones, in enum order. */
    public static List<ManagedBiome> configurable() {
        return Arrays.stream(values()).filter(b -> !b.isCore()).toList();
    }

    /** Looks up a biome by its {@link #biomeId()}. */
    public static Optional<ManagedBiome> byBiomeId(String biomeId) {
        return Arrays.stream(values()).filter(b -> b.biomeId().equals(biomeId)).findFirst();
    }

    /** Looks up a biome by its {@link #configKey()}. */
    public static Optional<ManagedBiome> byConfigKey(String configKey) {
        return Arrays.stream(values()).filter(b -> b.configKey.equals(configKey)).findFirst();
    }
}
