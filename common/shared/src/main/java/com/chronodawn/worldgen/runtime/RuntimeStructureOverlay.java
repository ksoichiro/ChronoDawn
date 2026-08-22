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
import com.chronodawn.config.ManagedStructure;
import com.chronodawn.config.StructureSettings;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates Minecraft worldgen JSON files at runtime from {@link ChronoDawnConfig}.
 *
 * <p>The generated files are returned as a {@code Map<path, bytes>} where path
 * is the data-pack-relative path (e.g.
 * {@code "data/chronodawn/worldgen/structure_set/ancient_ruins.json"}).
 * Loader-specific code wraps this map in an in-memory pack and registers it
 * with the server's pack repository.
 *
 * <p>JSON is built as a String literal (no JSON library) because the schema
 * is fixed and all field values are primitives.
 */
public final class RuntimeStructureOverlay {
    public static final String ANCIENT_RUINS_PATH = ManagedStructure.ANCIENT_RUINS.packPath();

    private RuntimeStructureOverlay() {}

    /**
     * Build the full overlay content for the given config.
     *
     * @param config the active configuration
     * @return ordered map of pack-relative path to file bytes
     */
    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        for (ManagedStructure structure : ManagedStructure.values()) {
            out.put(
                structure.packPath(),
                generateStructureSet(structure, structure.settingsOf(config.world().structures()))
            );
        }
        return out;
    }

    static byte[] generateStructureSet(ManagedStructure structure, StructureSettings settings) {
        // Disabled state: keep placement registered (so other systems referencing
        // the ID still find it) but emit no structure variants, so nothing generates.
        String structuresArray = settings.enabled()
            ? "{\n      \"structure\": \"" + structure.structureId() + "\",\n      \"weight\": 1\n    }"
            : "";
        String exclusionZone = exclusionZoneJson(structure);
        String json =
            "{\n" +
            "  \"structures\": [" + (structuresArray.isEmpty() ? "" : "\n    " + structuresArray + "\n  ") + "],\n" +
            "  \"placement\": {\n" +
            "    \"type\": \"minecraft:random_spread\",\n" +
            "    \"salt\": " + settings.salt() + ",\n" +
            "    \"spacing\": " + settings.spacing() + ",\n" +
            "    \"separation\": " + settings.separation() + (exclusionZone.isEmpty() ? "\n" : ",\n" + exclusionZone) +
            "  }\n" +
            "}\n";
        return json.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Some structure sets keep a fixed distance from another structure set (e.g. so
     * Phantom Catacombs doesn't overlap the other deep structures). This spacing is
     * not user-configurable, so it is hardcoded here rather than modeled in
     * {@link StructureSettings}.
     */
    private static String exclusionZoneJson(ManagedStructure structure) {
        return switch (structure) {
            case CLOCKWORK_DEPTHS, GUARDIAN_VAULT, ENTROPY_CRYPT ->
                "    \"exclusion_zone\": {\n" +
                "      \"other_set\": \"" + ManagedStructure.PHANTOM_CATACOMBS.structureId() + "\",\n" +
                "      \"chunk_count\": 10\n" +
                "    }\n";
            default -> "";
        };
    }
}
