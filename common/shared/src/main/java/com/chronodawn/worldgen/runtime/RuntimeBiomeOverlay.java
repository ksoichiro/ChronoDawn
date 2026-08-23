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
package com.chronodawn.worldgen.runtime;

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ManagedBiome;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Rewrites the Chrono dimension's biome distribution from {@link ChronoDawnConfig}.
 *
 * <p>Unlike the other overlay generators, which build their JSON from nothing, this
 * one <em>transforms</em> the bundled {@code dimension/chronodawn.json}: it replaces
 * the {@code biome} field of every entry whose biome a pack disabled and leaves the
 * {@code parameters} block alone. Keeping the parameters in the resource means the
 * twelve entries are defined in exactly one place and cannot drift.
 *
 * <p>Remapping rather than deleting entries also matters for existing worlds. The
 * biome definitions themselves are never touched, so a disabled biome stays
 * registered and chunks generated before the change still load.
 */
public final class RuntimeBiomeOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeBiomeOverlay.class);
    private static final Gson GSON = new Gson();

    private RuntimeBiomeOverlay() {}

    /**
     * Build the dimension overlay for the given config.
     *
     * @param config the active configuration
     * @return a single-entry map, or an empty map if the bundled resource could not be
     *         read or parsed — in which case the bundled JSON stays in effect
     */
    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        JsonObject dimension;
        try (InputStream in = RuntimeBiomeOverlay.class.getClassLoader()
            .getResourceAsStream(ManagedBiome.DIMENSION_PACK_PATH)) {
            if (in == null) {
                LOGGER.error("Bundled dimension JSON not found at {}; biome toggles will be inactive",
                    ManagedBiome.DIMENSION_PACK_PATH);
                return Map.of();
            }
            dimension = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                .getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Failed to read bundled dimension JSON; biome toggles will be inactive", e);
            return Map.of();
        }

        try {
            remapDisabledBiomes(dimension, config.world().biomes());
        } catch (Exception e) {
            LOGGER.error("Bundled dimension JSON has an unexpected shape; biome toggles will be inactive", e);
            return Map.of();
        }

        Map<String, byte[]> out = new LinkedHashMap<>();
        out.put(ManagedBiome.DIMENSION_PACK_PATH, GSON.toJson(dimension).getBytes(StandardCharsets.UTF_8));
        return out;
    }

    /**
     * Walks the multi-noise entries in place, pointing each disabled biome's entry at
     * the first enabled biome down its fallback chain.
     *
     * <p>Iterating entries rather than biomes is what makes the one biome that owns two
     * entries ({@code chronodawn_snowy}) work without a special case.
     */
    private static void remapDisabledBiomes(JsonObject dimension, ChronoDawnConfig.Biomes biomes) {
        JsonArray entries = dimension.getAsJsonObject("generator")
            .getAsJsonObject("biome_source")
            .getAsJsonArray("biomes");

        for (JsonElement element : entries) {
            JsonObject entry = element.getAsJsonObject();
            Optional<ManagedBiome> managed = ManagedBiome.byBiomeId(entry.get("biome").getAsString());
            if (managed.isEmpty()) {
                continue;
            }
            ManagedBiome resolved = managed.get()
                .resolveFallback(b -> b.settingsOf(biomes).enabled());
            if (resolved != managed.get()) {
                entry.addProperty("biome", resolved.biomeId());
            }
        }
    }
}
