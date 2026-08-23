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

    // Defaults mirror common/shared/src/main/resources/data/chronodawn/worldgen/structure_set/*.json
    // Asserted by RuntimeStructureOverlayTest (tree equality against the bundled JSONs).
    public static final StructureSettings ANCIENT_RUINS_DEFAULTS = new StructureSettings(true, 56, 20, 20005897L);
    public static final StructureSettings FORGOTTEN_LIBRARY_DEFAULTS = new StructureSettings(true, 30, 15, 8735421890L);
    public static final StructureSettings DESERT_CLOCK_TOWER_DEFAULTS = new StructureSettings(true, 30, 10, 1663542342L);
    public static final StructureSettings GUARDIAN_VAULT_DEFAULTS = new StructureSettings(true, 48, 24, 928374651L);
    public static final StructureSettings CLOCKWORK_DEPTHS_DEFAULTS = new StructureSettings(true, 56, 28, 837465129L);
    public static final StructureSettings PHANTOM_CATACOMBS_DEFAULTS = new StructureSettings(true, 20, 8, 745182936L);
    public static final StructureSettings ENTROPY_CRYPT_DEFAULTS = new StructureSettings(true, 50, 25, 738291456L);
    public static final StructureSettings MASTER_CLOCK_DEFAULTS = new StructureSettings(true, 60, 20, 1234567890L);

    // Defaults mirror common/shared/src/main/resources/data/chronodawn/worldgen/placed_feature/ore_*.json
    // Asserted by RuntimePlacedFeatureOverlayTest (round-trip parsed-tree equality against the bundled JSONs).
    public static final OreSettings TIME_CRYSTAL_DEFAULTS = new OreSettings(true, 3, 0, 48);
    public static final OreSettings ENTROPY_CRYSTAL_DEFAULTS = new OreSettings(true, 4, 40, 100);
    public static final OreSettings TEMPORAL_AMBER_DEFAULTS = new OreSettings(true, 4, -30, 20);
    public static final OreSettings CLOCKSTONE_DEFAULTS = new OreSettings(true, 8, -16, 80);

    // All six bosses default to unmodified statistics. A single shared constant
    // keeps "the default is a no-op" impossible to break for one boss only.
    public static final BossSettings BOSS_DEFAULTS = new BossSettings(1.0, 1.0);

    // Defaults mirror the existing ambient Time Distortion behavior exactly.
    public static final TimeDistortionSettings TIME_DISTORTION_DEFAULTS = new TimeDistortionSettings(
        true,
        4,
        5,
        TimeDistortionSettings.Scope.HOSTILE_MOBS
    );

    // Defaults preserve the shipped one-way portal progression and its
    // Portal Stabilizer requirement for re-ignition in Chrono Dawn.
    public static final PortalSettings PORTAL_DEFAULTS = new PortalSettings(true, false);

    /** Every biome generates by default. */
    public static final ChronoDawnConfig.Biomes BIOME_DEFAULTS = new ChronoDawnConfig.Biomes(
        new BiomeSettings(true), new BiomeSettings(true), new BiomeSettings(true),
        new BiomeSettings(true), new BiomeSettings(true), new BiomeSettings(true),
        new BiomeSettings(true), new BiomeSettings(true), new BiomeSettings(true)
    );

    public static ChronoDawnConfig defaults() {
        return new ChronoDawnConfig(
            ChronoDawnConfig.CURRENT_SCHEMA_VERSION,
            new ChronoDawnConfig.World(
                new ChronoDawnConfig.Structures(
                    ANCIENT_RUINS_DEFAULTS,
                    FORGOTTEN_LIBRARY_DEFAULTS,
                    DESERT_CLOCK_TOWER_DEFAULTS,
                    GUARDIAN_VAULT_DEFAULTS,
                    CLOCKWORK_DEPTHS_DEFAULTS,
                    PHANTOM_CATACOMBS_DEFAULTS,
                    ENTROPY_CRYPT_DEFAULTS,
                    MASTER_CLOCK_DEFAULTS
                ),
                new OresConfig(
                    TIME_CRYSTAL_DEFAULTS,
                    ENTROPY_CRYSTAL_DEFAULTS,
                    TEMPORAL_AMBER_DEFAULTS,
                    CLOCKSTONE_DEFAULTS
                ),
                BIOME_DEFAULTS
            ),
            new ChronoDawnConfig.Gameplay(
                TIME_DISTORTION_DEFAULTS,
                PORTAL_DEFAULTS,
                new BossesConfig(
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS,
                    BOSS_DEFAULTS
                )
            )
        );
    }
}
