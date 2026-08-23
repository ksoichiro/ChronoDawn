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

import com.chronodawn.config.BiomeSettings;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.ManagedBiome;
import com.chronodawn.worldgen.runtime.RuntimeBiomeOverlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeBiomeOverlayTest {

    @Test
    void packPath_matchesTheBundledResourceLocation() {
        assertEquals("data/chronodawn/dimension/chronodawn.json", ManagedBiome.DIMENSION_PACK_PATH);
    }

    @Test
    void defaultConfig_reproducesTheBundledDimensionJson() {
        Map<String, byte[]> overlay = RuntimeBiomeOverlay.generate(ConfigDefaults.defaults());

        byte[] bytes = overlay.get(ManagedBiome.DIMENSION_PACK_PATH);
        assertNotNull(bytes, "Overlay must contain the dimension JSON");
        assertEquals(loadBundled(), parse(bytes),
            "With every biome enabled the output must be tree-equal to the bundled JSON");
    }

    @Test
    void disablingOneBiome_rewritesOnlyItsBiomeFieldAndKeepsParameters() {
        JsonArray bundled = biomeEntries(loadBundled().getAsJsonObject());
        JsonArray actual = biomeEntries(parse(
            RuntimeBiomeOverlay.generate(configWith(Set.of(ManagedBiome.DARK_FOREST)))
                .get(ManagedBiome.DIMENSION_PACK_PATH)).getAsJsonObject());

        assertEquals(bundled.size(), actual.size(), "Entry count must not change");
        for (int i = 0; i < bundled.size(); i++) {
            JsonObject before = bundled.get(i).getAsJsonObject();
            JsonObject after = actual.get(i).getAsJsonObject();
            assertEquals(before.get("parameters"), after.get("parameters"),
                "Entry " + i + ": parameters must never be touched");
            String expected = ManagedBiome.DARK_FOREST.biomeId().equals(before.get("biome").getAsString())
                ? ManagedBiome.FOREST.biomeId()
                : before.get("biome").getAsString();
            assertEquals(expected, after.get("biome").getAsString(), "Entry " + i + ": biome field");
        }
    }

    @Test
    void disablingSnowy_rewritesBothOfItsEntries() {
        JsonArray entries = biomeEntries(parse(
            RuntimeBiomeOverlay.generate(configWith(Set.of(ManagedBiome.SNOWY)))
                .get(ManagedBiome.DIMENSION_PACK_PATH)).getAsJsonObject());

        long remaining = count(entries, ManagedBiome.SNOWY.biomeId());
        assertEquals(0, remaining, "chronodawn_snowy occupies two entries; both must be rewritten");
    }

    @Test
    void disablingAChain_resolvesToTheFirstEnabledBiome() {
        JsonArray entries = biomeEntries(parse(
            RuntimeBiomeOverlay.generate(configWith(
                    Set.of(ManagedBiome.ANCIENT_FOREST, ManagedBiome.DARK_FOREST, ManagedBiome.FOREST)))
                .get(ManagedBiome.DIMENSION_PACK_PATH)).getAsJsonObject());

        assertEquals(0, count(entries, ManagedBiome.ANCIENT_FOREST.biomeId()));
        assertEquals(0, count(entries, ManagedBiome.DARK_FOREST.biomeId()));
        assertEquals(0, count(entries, ManagedBiome.FOREST.biomeId()));
        assertTrue(count(entries, ManagedBiome.PLAINS.biomeId()) >= 4,
            "plains absorbs its own entry plus the three disabled ones");
    }

    private static long count(JsonArray entries, String biomeId) {
        long n = 0;
        for (JsonElement e : entries) {
            if (biomeId.equals(e.getAsJsonObject().get("biome").getAsString())) n++;
        }
        return n;
    }

    private static JsonArray biomeEntries(JsonObject dimension) {
        return dimension.getAsJsonObject("generator")
            .getAsJsonObject("biome_source")
            .getAsJsonArray("biomes");
    }

    /** A config with exactly the given biomes disabled. */
    private static ChronoDawnConfig configWith(Set<ManagedBiome> disabled) {
        java.util.function.Function<ManagedBiome, BiomeSettings> s =
            b -> new BiomeSettings(!disabled.contains(b));
        ChronoDawnConfig defaults = ConfigDefaults.defaults();
        return new ChronoDawnConfig(
            defaults.schemaVersion(),
            new ChronoDawnConfig.World(
                defaults.world().structures(),
                defaults.world().ores(),
                new ChronoDawnConfig.Biomes(
                    s.apply(ManagedBiome.DESERT),
                    s.apply(ManagedBiome.PRAIRIES),
                    s.apply(ManagedBiome.FOREST),
                    s.apply(ManagedBiome.DARK_FOREST),
                    s.apply(ManagedBiome.ANCIENT_FOREST),
                    s.apply(ManagedBiome.SNOWY),
                    s.apply(ManagedBiome.MOUNTAIN),
                    s.apply(ManagedBiome.SWAMP),
                    s.apply(ManagedBiome.FADED_PLAINS)
                )
            ),
            defaults.gameplay()
        );
    }

    private static JsonElement parse(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
    }

    private static JsonElement loadBundled() {
        try (InputStream in = RuntimeBiomeOverlayTest.class.getClassLoader()
            .getResourceAsStream(ManagedBiome.DIMENSION_PACK_PATH)) {
            assertNotNull(in, "Bundled dimension JSON not found on the test classpath");
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
