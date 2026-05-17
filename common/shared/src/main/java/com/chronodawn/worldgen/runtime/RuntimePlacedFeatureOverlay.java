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
import com.chronodawn.config.OreSettings;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates ChronoDawn ore placed_feature JSON files at runtime from
 * {@link ChronoDawnConfig}'s {@code OresConfig}.
 *
 * <p>Output paths follow the data-pack convention
 * {@code data/<namespace>/worldgen/placed_feature/<id>.json} so the
 * generated entries shadow the bundled mod resources.
 *
 * <p>{@code enabled = false} emits the same JSON shape with {@code count = 0}.
 * Keeping the resource registered means biome references to the placed_feature
 * still resolve; runtime placement evaluates to zero attempts so nothing
 * actually generates. The user's {@code count}, {@code yMin}, {@code yMax}
 * values are preserved verbatim (only the leading {@code minecraft:count}
 * step's {@code count} is overridden to 0), so re-enabling the ore restores
 * the user's last numbers rather than reverting to mod defaults.
 */
public final class RuntimePlacedFeatureOverlay {
    public static final String TIME_CRYSTAL_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_time_crystal.json";
    public static final String ENTROPY_CRYSTAL_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_entropy_crystal.json";
    public static final String TEMPORAL_AMBER_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_temporal_amber.json";
    public static final String CLOCKSTONE_PATH =
        "data/" + ChronoDawn.MOD_ID + "/worldgen/placed_feature/ore_clockstone.json";

    private RuntimePlacedFeatureOverlay() {}

    public static Map<String, byte[]> generate(ChronoDawnConfig config) {
        Map<String, byte[]> out = new LinkedHashMap<>();
        out.put(TIME_CRYSTAL_PATH, generateOre(
            "ore_time_crystal", "minecraft:trapezoid", config.world().ores().timeCrystal()));
        out.put(ENTROPY_CRYSTAL_PATH, generateOre(
            "ore_entropy_crystal", "minecraft:trapezoid", config.world().ores().entropyCrystal()));
        out.put(TEMPORAL_AMBER_PATH, generateOre(
            "ore_temporal_amber", "minecraft:uniform", config.world().ores().temporalAmber()));
        out.put(CLOCKSTONE_PATH, generateOre(
            "ore_clockstone", "minecraft:trapezoid", config.world().ores().clockstone()));
        return out;
    }

    static byte[] generateOre(String featureId, String heightDistributionType, OreSettings settings) {
        int emittedCount = settings.enabled() ? settings.count() : 0;
        String json =
            "{\n" +
            "  \"feature\": \"" + ChronoDawn.MOD_ID + ":" + featureId + "\",\n" +
            "  \"placement\": [\n" +
            "    {\n" +
            "      \"type\": \"minecraft:count\",\n" +
            "      \"count\": " + emittedCount + "\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"minecraft:in_square\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"minecraft:height_range\",\n" +
            "      \"height\": {\n" +
            "        \"type\": \"" + heightDistributionType + "\",\n" +
            "        \"min_inclusive\": {\n" +
            "          \"absolute\": " + settings.yMin() + "\n" +
            "        },\n" +
            "        \"max_inclusive\": {\n" +
            "          \"absolute\": " + settings.yMax() + "\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": \"minecraft:biome\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n";
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
