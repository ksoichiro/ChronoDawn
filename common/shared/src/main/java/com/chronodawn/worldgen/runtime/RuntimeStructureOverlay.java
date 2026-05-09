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

import com.chronodawn.ChronoDawn;
import com.chronodawn.config.ChronoDawnConfig;

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
    public static final String ANCIENT_RUINS_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/structure_set/ancient_ruins.json";

    private RuntimeStructureOverlay() {}

    /**
     * Build the full overlay content for the given config.
     *
     * @param config the active configuration
     * @return ordered map of pack-relative path to file bytes
     */
    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        out.put(ANCIENT_RUINS_PATH, generateAncientRuins(config.world().structures().ancientRuins()));
        return out;
    }

    static byte[] generateAncientRuins(ChronoDawnConfig.AncientRuins ar) {
        // Disabled state: keep placement registered (so other systems referencing
        // the ID still find it) but emit no structure variants, so nothing generates.
        String structuresArray = ar.enabled()
            ? "{\n      \"structure\": \"" + ChronoDawn.MOD_ID + ":ancient_ruins\",\n      \"weight\": 1\n    }"
            : "";
        String json =
            "{\n" +
            "  \"structures\": [" + (structuresArray.isEmpty() ? "" : "\n    " + structuresArray + "\n  ") + "],\n" +
            "  \"placement\": {\n" +
            "    \"type\": \"minecraft:random_spread\",\n" +
            "    \"salt\": " + ar.salt() + ",\n" +
            "    \"spacing\": " + ar.spacing() + ",\n" +
            "    \"separation\": " + ar.separation() + "\n" +
            "  }\n" +
            "}\n";
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
