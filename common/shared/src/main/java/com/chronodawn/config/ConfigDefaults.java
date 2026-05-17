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
package com.chronodawn.config;

/**
 * Single source of truth for default config values.
 *
 * <p>Defaults must match the bundled JSON behavior so adding a config option
 * is a no-op for users who never touch the file.
 */
public final class ConfigDefaults {
    private ConfigDefaults() {}

    // Defaults mirror common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/ancient_ruins.json
    public static final boolean ANCIENT_RUINS_ENABLED = true;
    public static final int ANCIENT_RUINS_SPACING = 56;
    public static final int ANCIENT_RUINS_SEPARATION = 20;
    public static final int ANCIENT_RUINS_SALT = 20005897;

    // Defaults mirror common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_*.json
    // Asserted by RuntimePlacedFeatureOverlayTest (round-trip parsed-tree equality against the bundled JSONs).
    public static final OreSettings TIME_CRYSTAL_DEFAULTS = new OreSettings(true, 3, 0, 48);
    public static final OreSettings ENTROPY_CRYSTAL_DEFAULTS = new OreSettings(true, 4, 40, 100);
    public static final OreSettings TEMPORAL_AMBER_DEFAULTS = new OreSettings(true, 4, -30, 20);
    public static final OreSettings CLOCKSTONE_DEFAULTS = new OreSettings(true, 8, -16, 80);

    public static ChronoDawnConfig defaults() {
        return new ChronoDawnConfig(
            ChronoDawnConfig.CURRENT_SCHEMA_VERSION,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(
                    new ChronoDawnConfig.AncientRuins(
                        ANCIENT_RUINS_ENABLED,
                        ANCIENT_RUINS_SPACING,
                        ANCIENT_RUINS_SEPARATION,
                        ANCIENT_RUINS_SALT
                    )
                ),
                new OresConfig(
                    TIME_CRYSTAL_DEFAULTS,
                    ENTROPY_CRYSTAL_DEFAULTS,
                    TEMPORAL_AMBER_DEFAULTS,
                    CLOCKSTONE_DEFAULTS
                )
            )
        );
    }
}
