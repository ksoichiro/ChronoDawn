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
package com.chronodawn.unit;

import com.chronodawn.config.ManagedBiome;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagedBiomeTest {

    @Test
    void exactlyTwoCoreBiomes_plainsAndOcean() {
        List<String> core = java.util.Arrays.stream(ManagedBiome.values())
            .filter(ManagedBiome::isCore)
            .map(ManagedBiome::configKey)
            .sorted()
            .toList();
        assertEquals(List.of("ocean", "plains"), core, "plains and ocean are the only core biomes");
    }

    @Test
    void coreBiomesHaveNoFallback_nonCoreBiomesDo() {
        for (ManagedBiome biome : ManagedBiome.values()) {
            if (biome.isCore()) {
                assertTrue(biome.fallback().isEmpty(), biome + " is core so it must have no fallback");
            } else {
                assertTrue(biome.fallback().isPresent(), biome + " is not core so it must have a fallback");
            }
        }
    }

    @Test
    void everyFallbackChainTerminatesAtACoreBiome() {
        for (ManagedBiome start : ManagedBiome.values()) {
            Set<ManagedBiome> visited = new HashSet<>();
            ManagedBiome current = start;
            while (!current.isCore()) {
                assertTrue(visited.add(current),
                    start + ": fallback chain loops at " + current);
                Optional<ManagedBiome> next = current.fallback();
                assertTrue(next.isPresent(), current + " is not core so it must have a fallback");
                current = next.get();
            }
        }
    }

    @Test
    void resolveFallback_skipsDisabledBiomesUntilAnEnabledOne() {
        ManagedBiome ancientForest = ManagedBiome.ANCIENT_FOREST;

        // Nothing disabled: a biome resolves to itself.
        assertEquals(ancientForest, ancientForest.resolveFallback(b -> true));

        // ancient_forest disabled only: falls back one step to dark_forest.
        assertEquals(
            ManagedBiome.DARK_FOREST,
            ancientForest.resolveFallback(b -> b != ManagedBiome.ANCIENT_FOREST)
        );

        // ancient_forest, dark_forest and forest all disabled: resolves to plains.
        Set<ManagedBiome> off = Set.of(
            ManagedBiome.ANCIENT_FOREST, ManagedBiome.DARK_FOREST, ManagedBiome.FOREST);
        assertEquals(ManagedBiome.PLAINS, ancientForest.resolveFallback(b -> !off.contains(b)));
    }

    @Test
    void configKeysAndBiomeIdsAreUnique() {
        Set<String> keys = new HashSet<>();
        Set<String> ids = new HashSet<>();
        for (ManagedBiome biome : ManagedBiome.values()) {
            assertTrue(keys.add(biome.configKey()), "Duplicate configKey: " + biome.configKey());
            assertTrue(ids.add(biome.biomeId()), "Duplicate biomeId: " + biome.biomeId());
        }
    }

    @Test
    void configurable_excludesCoreBiomes() {
        List<ManagedBiome> configurable = ManagedBiome.configurable();
        assertEquals(9, configurable.size(), "Nine of the eleven biomes are configurable");
        for (ManagedBiome biome : configurable) {
            assertFalse(biome.isCore(), biome + " is core and must not be configurable");
        }
    }

    @Test
    void byBiomeId_findsEveryBiome_andRejectsUnknown() {
        for (ManagedBiome biome : ManagedBiome.values()) {
            assertEquals(Optional.of(biome), ManagedBiome.byBiomeId(biome.biomeId()));
        }
        assertTrue(ManagedBiome.byBiomeId("minecraft:plains").isEmpty());
    }

    /**
     * Guards against the enum drifting from the bundled dimension JSON. Adding a biome to
     * the dimension without registering it here would leave it silently untoggleable;
     * removing one from the dimension while it stays here would produce a config table
     * that does nothing.
     *
     * <p>Compared as sets, not lists: chronodawn_snowy occupies two entries.
     */
    @Test
    void managedBiomes_matchTheBundledDimensionJson() {
        Set<String> declared = java.util.Arrays.stream(ManagedBiome.values())
            .map(ManagedBiome::biomeId)
            .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new));

        Set<String> inJson = new java.util.TreeSet<>();
        try (java.io.InputStream in = ManagedBiomeTest.class.getClassLoader()
            .getResourceAsStream(ManagedBiome.DIMENSION_PACK_PATH)) {
            assertNotNull(in, "Bundled dimension JSON not found on the test classpath");
            com.google.gson.JsonObject dimension = com.google.gson.JsonParser
                .parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
            for (com.google.gson.JsonElement entry : dimension.getAsJsonObject("generator")
                .getAsJsonObject("biome_source").getAsJsonArray("biomes")) {
                inJson.add(entry.getAsJsonObject().get("biome").getAsString());
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(inJson, declared,
            "ManagedBiome must declare exactly the biomes the bundled dimension JSON references");
    }
}
