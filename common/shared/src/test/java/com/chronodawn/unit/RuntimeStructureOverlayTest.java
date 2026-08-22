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

import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.ManagedStructure;
import com.chronodawn.config.StructureSettings;
import com.chronodawn.worldgen.runtime.RuntimeStructureOverlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link RuntimeStructureOverlay} JSON output.
 *
 * <p>Compares against the parsed JSON shape rather than the raw bytes so that
 * harmless whitespace tweaks don't break tests; the contract is "the JSON
 * Minecraft sees has these values", not "the bytes match exactly".
 */
class RuntimeStructureOverlayTest {

    @Test
    void defaultConfig_producesExpectedAncientRuinsJson() {
        Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(ConfigDefaults.defaults());

        byte[] bytes = overlay.get(RuntimeStructureOverlay.ANCIENT_RUINS_PATH);
        assertNotNull(bytes, "Overlay must contain ancient_ruins.json under expected path");

        JsonObject json = parse(bytes);
        JsonArray structures = json.getAsJsonArray("structures");
        assertEquals(1, structures.size(), "Default config has structures enabled");
        JsonObject entry = structures.get(0).getAsJsonObject();
        assertEquals("chronodawn:ancient_ruins", entry.get("structure").getAsString());
        assertEquals(1, entry.get("weight").getAsInt());

        JsonObject placement = json.getAsJsonObject("placement");
        assertEquals("minecraft:random_spread", placement.get("type").getAsString());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.salt(), placement.get("salt").getAsLong());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.spacing(), placement.get("spacing").getAsInt());
        assertEquals(ConfigDefaults.ANCIENT_RUINS_DEFAULTS.separation(), placement.get("separation").getAsInt());
    }

    @Test
    void customConfig_reflectsCustomValuesInJson() {
        ChronoDawnConfig custom = new ChronoDawnConfig(
            ChronoDawnConfig.CURRENT_SCHEMA_VERSION,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(
                    new StructureSettings(true, 16, 4, 999L)
                ),
                ConfigDefaults.defaults().world().ores()
            ),
            ConfigDefaults.defaults().gameplay()
        );

        JsonObject json = parse(RuntimeStructureOverlay.generate(custom)
            .get(RuntimeStructureOverlay.ANCIENT_RUINS_PATH));

        JsonObject placement = json.getAsJsonObject("placement");
        assertEquals(999, placement.get("salt").getAsInt());
        assertEquals(16, placement.get("spacing").getAsInt());
        assertEquals(4, placement.get("separation").getAsInt());
        assertEquals(1, json.getAsJsonArray("structures").size());
    }

    @Test
    void disabledConfig_producesEmptyStructuresArray() {
        ChronoDawnConfig disabled = new ChronoDawnConfig(
            ChronoDawnConfig.CURRENT_SCHEMA_VERSION,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(
                    new StructureSettings(false, 56, 20, 20005897L)
                ),
                ConfigDefaults.defaults().world().ores()
            ),
            ConfigDefaults.defaults().gameplay()
        );

        JsonObject json = parse(RuntimeStructureOverlay.generate(disabled)
            .get(RuntimeStructureOverlay.ANCIENT_RUINS_PATH));

        // Empty structures array: placement is still registered but no variants chosen
        // → nothing generates. Cleaner than omitting the JSON entirely (which would
        // re-expose the bundled defaults).
        assertTrue(json.has("structures"));
        assertEquals(0, json.getAsJsonArray("structures").size(),
            "enabled=false must produce an empty structures array");
        // Placement values still present so other systems referencing the ID find it
        assertNotNull(json.getAsJsonObject("placement"));
    }

    @Test
    void overlay_pathMatchesDataPackConvention() {
        // Path must follow `data/<namespace>/worldgen/structure_set/<id>.json`
        // so it overrides the bundled mod resource correctly.
        assertEquals(
            "data/chronodawn/worldgen/structure_set/ancient_ruins.json",
            RuntimeStructureOverlay.ANCIENT_RUINS_PATH
        );
    }

    @Test
    void defaultConfig_reproducesEveryBundledStructureSet() {
        Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(ConfigDefaults.defaults());

        for (ManagedStructure structure : ManagedStructure.values()) {
            byte[] bytes = overlay.get(structure.packPath());
            assertNotNull(bytes, "Overlay must contain " + structure.packPath());

            JsonElement generated = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
            JsonElement bundled = loadBundled(structure.packPath());
            assertEquals(bundled, generated,
                structure.name() + ": default overlay output must be tree-equal to the bundled JSON");
        }
    }

    @Test
    void overlay_containsExactlyTheManagedStructures() {
        Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(ConfigDefaults.defaults());

        Set<String> expected = Arrays.stream(ManagedStructure.values())
            .map(ManagedStructure::packPath)
            .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(expected, new TreeSet<>(overlay.keySet()));
    }

    @Test
    void disablingMasterClock_emptiesOnlyItsStructuresArray() {
        ChronoDawnConfig defaults = ConfigDefaults.defaults();
        ChronoDawnConfig.Structures s = defaults.world().structures();
        ChronoDawnConfig config = new ChronoDawnConfig(
            defaults.schemaVersion(),
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(
                    s.ancientRuins(), s.forgottenLibrary(), s.desertClockTower(), s.guardianVault(),
                    s.clockworkDepths(), s.phantomCatacombs(), s.entropyCrypt(),
                    new StructureSettings(false, 60, 20, 1234567890L)
                ),
                defaults.world().ores()
            ),
            defaults.gameplay()
        );

        Map<String, byte[]> overlay = RuntimeStructureOverlay.generate(config);

        JsonObject masterClock = parseObject(overlay.get(ManagedStructure.MASTER_CLOCK.packPath()));
        assertEquals(0, masterClock.getAsJsonArray("structures").size(),
            "Disabled structure must emit an empty structures array");
        assertEquals(60, masterClock.getAsJsonObject("placement").get("spacing").getAsInt(),
            "Disabled structure keeps its placement block");

        JsonObject entropyCrypt = parseObject(overlay.get(ManagedStructure.ENTROPY_CRYPT.packPath()));
        assertEquals(1, entropyCrypt.getAsJsonArray("structures").size(),
            "Disabling one structure must not affect its siblings");
    }

    private static JsonObject parse(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static JsonObject parseObject(byte[] bytes) {
        return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static JsonElement loadBundled(String classpathRelative) {
        try (InputStream in = RuntimeStructureOverlayTest.class.getClassLoader()
            .getResourceAsStream(classpathRelative)) {
            assertNotNull(in, "Bundled resource not found on test classpath: " + classpathRelative);
            return JsonParser.parseReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
