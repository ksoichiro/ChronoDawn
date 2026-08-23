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
import com.chronodawn.config.ManagedStructure;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Every {@link ManagedBiome} must have a biome definition on the classpath of the
     * version module the test runs under. Biome definitions live per version (e.g.
     * {@code common/1.20.1}, {@code common/1.21.1}), not only in the shared dimension
     * JSON that {@link #managedBiomes_matchTheBundledDimensionJson()} checks — so a
     * future version whose biome set diverges could otherwise ship a runtime overlay
     * that references a biome absent on that one version, failing world creation only
     * there. Since unit tests already run once per version module, this guards all
     * eleven versions automatically.
     */
    @Test
    void everyManagedBiome_hasABiomeDefinitionOnTheClasspath() {
        for (ManagedBiome biome : ManagedBiome.values()) {
            String path = "data/chronodawn/worldgen/biome/" + biome.biomeId().substring("chronodawn:".length()) + ".json";
            try (java.io.InputStream in = ManagedBiomeTest.class.getClassLoader().getResourceAsStream(path)) {
                assertNotNull(in, biome.name() + ": no biome definition found at " + path);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * No combination of biome toggles may leave a Chrono structure with no biome to
     * generate in. This holds today because every {@code has_*} tag (and the inline
     * {@code biomes} list on structures without one) lists chronodawn_plains, which is
     * core — but that is a property of the data files, not something the toggle code
     * enforces, so it is measured rather than assumed.
     *
     * <p>Exhausts all 2^9 combinations of the configurable biomes.
     *
     * <p>The backstop is deliberately "a biome set was found for every {@code CHRONO_DAWN}
     * structure", not merely "at least one was found": a future structure that uses
     * neither a {@code has_*} tag nor an inline {@code biomes} list must fail this test
     * loudly rather than silently drop out of the 512-combination sweep.
     */
    @Test
    void noCombinationOfBiomeTogglesCanOrphanAStructure() {
        List<ManagedBiome> configurable = ManagedBiome.configurable();
        int combinations = 1 << configurable.size();

        Map<ManagedStructure, Set<String>> tags = new LinkedHashMap<>();
        for (ManagedStructure structure : ManagedStructure.values()) {
            if (structure.dimension() != ManagedStructure.Dimension.CHRONO_DAWN) {
                continue; // Ancient Ruins is placed in Overworld biomes, out of scope here.
            }
            Set<String> biomeSet = readBiomeSet(structure.configKey());
            assertFalse(biomeSet.isEmpty(),
                structure.name() + ": found neither a has_* tag nor an inline biomes list");
            tags.put(structure, biomeSet);
        }

        for (int mask = 0; mask < combinations; mask++) {
            Set<String> enabledIds = new HashSet<>();
            for (ManagedBiome biome : ManagedBiome.values()) {
                if (biome.isCore()) {
                    enabledIds.add(biome.biomeId());
                }
            }
            for (int i = 0; i < configurable.size(); i++) {
                if ((mask & (1 << i)) == 0) {
                    enabledIds.add(configurable.get(i).biomeId());
                }
            }
            for (Map.Entry<ManagedStructure, Set<String>> entry : tags.entrySet()) {
                boolean anyEnabled = entry.getValue().stream().anyMatch(enabledIds::contains);
                assertTrue(anyEnabled,
                    entry.getKey().name() + " has no enabled biome for combination mask " + mask);
            }
        }
    }

    /**
     * Resolves the set of chronodawn: biome IDs a structure can place in, whichever
     * mechanism it uses: a {@code has_<key>} biome tag, or (for structures like Forgotten
     * Library that declare biomes inline) the structure JSON's own {@code biomes} field.
     *
     * @return the biome IDs, or an empty set if neither mechanism yielded one
     */
    private static Set<String> readBiomeSet(String configKey) {
        Optional<Set<String>> fromTag = readBiomeTag(configKey);
        if (fromTag.isPresent()) {
            return fromTag.get();
        }
        return readInlineStructureBiomes(configKey);
    }

    /** Reads the chronodawn: biome IDs out of a has_&lt;key&gt; biome tag, if the tag exists. */
    private static Optional<Set<String>> readBiomeTag(String configKey) {
        String path = "data/chronodawn/tags/worldgen/biome/has_" + configKey + ".json";
        try (java.io.InputStream in = ManagedBiomeTest.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return Optional.empty(); // e.g. forgotten_library, which is placed without a tag.
            }
            com.google.gson.JsonObject tag = com.google.gson.JsonParser
                .parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
            Set<String> values = new HashSet<>();
            for (com.google.gson.JsonElement value : tag.getAsJsonArray("values")) {
                // The tag entry schema allows either a plain string ID or an object form
                // ({"id": ..., "required": false}); getAsString() throws on the latter.
                String id = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString()
                    : value.getAsString();
                if (id.startsWith("chronodawn:")) {
                    values.add(id);
                }
            }
            return values.isEmpty() ? Optional.empty() : Optional.of(values);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the chronodawn: biome IDs out of a structure JSON's inline {@code biomes}
     * field, for structures (e.g. Forgotten Library) that declare biomes directly rather
     * than through a {@code has_*} tag.
     *
     * <p>The vanilla schema allows that field to be a single string, a list of strings, or
     * a {@code #tag} reference (in either form). Only the plain-string and plain-string-list
     * forms are in use today; a {@code #tag} reference or any other shape fails loudly
     * rather than being silently skipped, so a future structure that adopts one is caught
     * here instead of vanishing from the sweep.
     */
    private static Set<String> readInlineStructureBiomes(String configKey) {
        String path = "data/chronodawn/worldgen/structure/" + configKey + ".json";
        try (java.io.InputStream in = ManagedBiomeTest.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return Set.of();
            }
            com.google.gson.JsonObject structure = com.google.gson.JsonParser
                .parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))
                .getAsJsonObject();
            com.google.gson.JsonElement biomes = structure.get("biomes");
            assertNotNull(biomes, configKey + ": structure JSON has no biomes field and no has_* tag");

            Set<String> values = new HashSet<>();
            if (biomes.isJsonPrimitive()) {
                values.add(requirePlainBiomeId(biomes.getAsString(), configKey));
            } else if (biomes.isJsonArray()) {
                for (com.google.gson.JsonElement entry : biomes.getAsJsonArray()) {
                    values.add(requirePlainBiomeId(entry.getAsString(), configKey));
                }
            } else {
                throw new AssertionError(
                    configKey + ": unhandled biomes field shape " + biomes + "; extend readInlineStructureBiomes");
            }
            return values;
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String requirePlainBiomeId(String id, String configKey) {
        if (id.startsWith("#")) {
            throw new AssertionError(
                configKey + ": inline biomes field uses a #tag reference (" + id
                    + "), which readInlineStructureBiomes does not resolve yet");
        }
        return id;
    }
}
