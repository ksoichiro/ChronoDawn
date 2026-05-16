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
import com.chronodawn.config.ConfigDefaults;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link RuntimePlacedFeatureOverlay}.
 *
 * <p>The default-equivalence assertions compare parsed JSON trees (not raw bytes)
 * so the overlay generator can format whitespace however it likes. If a future
 * change drifts the runtime emitter away from a bundled JSON, these tests fail
 * with a tree diff pointing at the differing key.
 */
class RuntimePlacedFeatureOverlayTest {

    @Test
    void defaultConfig_reproducesBundledTimeCrystalJson() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        byte[] bytes = overlay.get(RuntimePlacedFeatureOverlay.TIME_CRYSTAL_PATH);
        assertNotNull(bytes, "Overlay must contain ore_time_crystal.json under expected path");

        JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        JsonElement bundled = loadBundled("data/chronodawn/worldgen/placed_feature/ore_time_crystal.json");
        assertEquals(bundled, generated,
            "Runtime overlay output for default config must be tree-equal to the bundled placed_feature JSON");
    }

    @Test
    void defaultConfig_reproducesBundledEntropyCrystalJson() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        byte[] bytes = overlay.get(RuntimePlacedFeatureOverlay.ENTROPY_CRYSTAL_PATH);
        assertNotNull(bytes, "Overlay must contain ore_entropy_crystal.json under expected path");
        JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        JsonElement bundled = loadBundled("data/chronodawn/worldgen/placed_feature/ore_entropy_crystal.json");
        assertEquals(bundled, generated);
    }

    @Test
    void defaultConfig_reproducesBundledTemporalAmberJson() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        byte[] bytes = overlay.get(RuntimePlacedFeatureOverlay.TEMPORAL_AMBER_PATH);
        assertNotNull(bytes, "Overlay must contain ore_temporal_amber.json under expected path");
        JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        JsonElement bundled = loadBundled("data/chronodawn/worldgen/placed_feature/ore_temporal_amber.json");
        assertEquals(bundled, generated);
    }

    private static JsonElement loadBundled(String classpathRelative) {
        try (InputStream in = RuntimePlacedFeatureOverlayTest.class.getClassLoader()
            .getResourceAsStream(classpathRelative)) {
            assertNotNull(in, "Bundled resource not found on test classpath: " + classpathRelative);
            return JsonParser.parseReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
