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
import com.chronodawn.config.OreSettings;
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

    @Test
    void defaultConfig_reproducesBundledClockstoneJson() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        byte[] bytes = overlay.get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH);
        assertNotNull(bytes, "Overlay must contain ore_clockstone.json under expected path");
        JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        JsonElement bundled = loadBundled("data/chronodawn/worldgen/placed_feature/ore_clockstone.json");
        assertEquals(bundled, generated);
    }

    @Test
    void clockstoneCountOverride_changesCountStepOnly() {
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS,
            ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS,
            ConfigDefaults.TEMPORAL_AMBER_DEFAULTS,
            new OreSettings(true, 20, -16, 80)
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH));
        assertEquals(20, countStep(json).get("count").getAsInt());
    }

    @Test
    void clockstoneDisabled_emitsCountZeroAndPreservesOtherValues() {
        OreSettings disabled = new OreSettings(false, 12, -32, 60);
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS,
            ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS,
            ConfigDefaults.TEMPORAL_AMBER_DEFAULTS,
            disabled
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH));
        assertEquals(0, countStep(json).get("count").getAsInt(),
            "enabled=false must force the count step to 0");
        JsonObject heightRange = json.getAsJsonArray("placement").get(2).getAsJsonObject();
        assertEquals(-32,
            heightRange.getAsJsonObject("height").getAsJsonObject("min_inclusive").get("absolute").getAsInt(),
            "yMin must be preserved verbatim when disabled");
        assertEquals(60,
            heightRange.getAsJsonObject("height").getAsJsonObject("max_inclusive").get("absolute").getAsInt(),
            "yMax must be preserved verbatim when disabled");
    }

    @Test
    void clockstoneCustomYRange_changesHeightBoundsAndKeepsTrapezoid() {
        OreSettings clk = new OreSettings(true, 8, -50, 30);
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS,
            ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS,
            ConfigDefaults.TEMPORAL_AMBER_DEFAULTS,
            clk
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH));
        JsonObject height = json.getAsJsonArray("placement").get(2).getAsJsonObject().getAsJsonObject("height");
        assertEquals(-50, height.getAsJsonObject("min_inclusive").get("absolute").getAsInt());
        assertEquals(30, height.getAsJsonObject("max_inclusive").get("absolute").getAsInt());
        assertEquals("minecraft:trapezoid", height.get("type").getAsString());
    }

    @Test
    void timeCrystalCountOverride_changesCountStepOnly() {
        ChronoDawnConfig custom = withOres(
            new OreSettings(true, 10, 0, 48),
            ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS,
            ConfigDefaults.TEMPORAL_AMBER_DEFAULTS,
            ConfigDefaults.CLOCKSTONE_DEFAULTS
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.TIME_CRYSTAL_PATH));
        assertEquals(10, countStep(json).get("count").getAsInt());
    }

    @Test
    void entropyCrystalDisabled_emitsCountZeroAndPreservesOtherValues() {
        // Disabled state: forced count=0 in the emitted JSON, but user's count / yMin / yMax
        // round-trip verbatim so re-enabling restores their last numbers.
        OreSettings disabled = new OreSettings(false, 7, 30, 90);
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS, disabled, ConfigDefaults.TEMPORAL_AMBER_DEFAULTS,
            ConfigDefaults.CLOCKSTONE_DEFAULTS
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.ENTROPY_CRYSTAL_PATH));
        assertEquals(0, countStep(json).get("count").getAsInt(),
            "enabled=false must force the count step to 0");
        JsonObject heightRange = json.getAsJsonArray("placement").get(2).getAsJsonObject();
        assertEquals(30,
            heightRange.getAsJsonObject("height").getAsJsonObject("min_inclusive").get("absolute").getAsInt(),
            "yMin must be preserved verbatim when disabled");
        assertEquals(90,
            heightRange.getAsJsonObject("height").getAsJsonObject("max_inclusive").get("absolute").getAsInt(),
            "yMax must be preserved verbatim when disabled");
    }

    @Test
    void temporalAmberCustomYRange_changesHeightBoundsAndKeepsUniform() {
        OreSettings amber = new OreSettings(true, 4, -20, 30);
        ChronoDawnConfig custom = withOres(
            ConfigDefaults.TIME_CRYSTAL_DEFAULTS, ConfigDefaults.ENTROPY_CRYSTAL_DEFAULTS, amber,
            ConfigDefaults.CLOCKSTONE_DEFAULTS
        );
        JsonObject json = parseObject(RuntimePlacedFeatureOverlay.generate(custom)
            .get(RuntimePlacedFeatureOverlay.TEMPORAL_AMBER_PATH));
        JsonObject height = json.getAsJsonArray("placement").get(2).getAsJsonObject().getAsJsonObject("height");
        assertEquals(-20, height.getAsJsonObject("min_inclusive").get("absolute").getAsInt());
        assertEquals(30, height.getAsJsonObject("max_inclusive").get("absolute").getAsInt());
        assertEquals("minecraft:uniform", height.get("type").getAsString());
    }

    @Test
    void perOreDistributionType_isFixed() {
        Map<String, byte[]> overlay = RuntimePlacedFeatureOverlay.generate(ConfigDefaults.defaults());
        assertEquals("minecraft:trapezoid",
            heightType(overlay.get(RuntimePlacedFeatureOverlay.TIME_CRYSTAL_PATH)));
        assertEquals("minecraft:trapezoid",
            heightType(overlay.get(RuntimePlacedFeatureOverlay.ENTROPY_CRYSTAL_PATH)));
        assertEquals("minecraft:uniform",
            heightType(overlay.get(RuntimePlacedFeatureOverlay.TEMPORAL_AMBER_PATH)));
        assertEquals("minecraft:trapezoid",
            heightType(overlay.get(RuntimePlacedFeatureOverlay.CLOCKSTONE_PATH)));
    }

    // --- helpers ---

    private static ChronoDawnConfig withOres(OreSettings tc, OreSettings ec, OreSettings ta, OreSettings clk) {
        ChronoDawnConfig defaults = ConfigDefaults.defaults();
        return new ChronoDawnConfig(
            defaults.schemaVersion(),
            new ChronoDawnConfig.World(
                defaults.world().structures(),
                new com.chronodawn.config.OresConfig(tc, ec, ta, clk)
            )
        );
    }

    private static JsonObject parseObject(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static JsonObject countStep(JsonObject placedFeature) {
        return placedFeature.getAsJsonArray("placement").get(0).getAsJsonObject();
    }

    private static String heightType(byte[] bytes) {
        return parseObject(bytes).getAsJsonArray("placement").get(2).getAsJsonObject()
            .getAsJsonObject("height").get("type").getAsString();
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
