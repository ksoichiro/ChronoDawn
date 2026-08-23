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
 * Immutable record holding the parsed Chrono Dawn configuration.
 *
 * <p>Loaded once at mod startup by {@link ConfigLoader}. Consumers should
 * read values from {@link #INSTANCE} after {@code ConfigLoader.load(...)}
 * has been called from {@code ChronoDawn.init()}.
 *
 * <p>To extend this record with a new tunable, follow the recipe in the
 * design spec ({@code docs/superpowers/specs/2026-05-09-config-foundation-design.md},
 * "Extension pattern" section).
 */
public record ChronoDawnConfig(
    int schemaVersion,
    World world,
    Gameplay gameplay
) {
    /** Latest known schema version this build understands. */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    /**
     * Holds the active configuration once {@code ConfigLoader.load(...)} has
     * been called. Defaults to {@link ConfigDefaults#defaults()} so consumers
     * never see {@code null} even if the loader was skipped (e.g. in tests).
     */
    private static volatile ChronoDawnConfig INSTANCE = ConfigDefaults.defaults();

    public static ChronoDawnConfig get() {
        return INSTANCE;
    }

    /** Replace the active configuration. Called by {@link ConfigLoader}. */
    public static void set(ChronoDawnConfig config) {
        INSTANCE = config;
    }

    public record World(Structures structures, OresConfig ores, Biomes biomes) {
        /** Preserve the existing construction pattern for callers that don't configure biomes. */
        public World(Structures structures, OresConfig ores) {
            this(structures, ores, ConfigDefaults.BIOME_DEFAULTS);
        }
    }

    public record Biomes(
        BiomeSettings desert,
        BiomeSettings prairies,
        BiomeSettings forest,
        BiomeSettings darkForest,
        BiomeSettings ancientForest,
        BiomeSettings snowy,
        BiomeSettings mountain,
        BiomeSettings swamp,
        BiomeSettings fadedPlains
    ) {}

    public record Structures(
        StructureSettings ancientRuins,
        StructureSettings forgottenLibrary,
        StructureSettings desertClockTower,
        StructureSettings guardianVault,
        StructureSettings clockworkDepths,
        StructureSettings phantomCatacombs,
        StructureSettings entropyCrypt,
        StructureSettings masterClock
    ) {
        /** Preserve the existing construction pattern for callers that only configure Ancient Ruins. */
        public Structures(StructureSettings ancientRuins) {
            this(
                ancientRuins,
                ConfigDefaults.FORGOTTEN_LIBRARY_DEFAULTS,
                ConfigDefaults.DESERT_CLOCK_TOWER_DEFAULTS,
                ConfigDefaults.GUARDIAN_VAULT_DEFAULTS,
                ConfigDefaults.CLOCKWORK_DEPTHS_DEFAULTS,
                ConfigDefaults.PHANTOM_CATACOMBS_DEFAULTS,
                ConfigDefaults.ENTROPY_CRYPT_DEFAULTS,
                ConfigDefaults.MASTER_CLOCK_DEFAULTS
            );
        }
    }

    public record Gameplay(TimeDistortionSettings timeDistortion, PortalSettings portals, BossesConfig bosses) {
        /** Preserve the existing construction pattern for callers that only configure bosses. */
        public Gameplay(BossesConfig bosses) {
            this(ConfigDefaults.TIME_DISTORTION_DEFAULTS, ConfigDefaults.PORTAL_DEFAULTS, bosses);
        }

        /** Preserve the existing construction pattern for callers that configure Time Distortion. */
        public Gameplay(TimeDistortionSettings timeDistortion, BossesConfig bosses) {
            this(timeDistortion, ConfigDefaults.PORTAL_DEFAULTS, bosses);
        }
    }
}
