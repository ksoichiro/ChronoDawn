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

import com.chronodawn.ChronoDawn;

import java.util.function.Function;

/**
 * Every structure whose placement Chrono Dawn exposes to configuration.
 *
 * <p>This enum is the single source of truth for that set: the config parser, the
 * runtime overlay and the guard tests all iterate it. A structure that is not
 * registered here appears in none of them, and one that is registered appears in
 * all of them — so a structure cannot be half-wired.
 */
public enum ManagedStructure {
    ANCIENT_RUINS("ancient_ruins", ConfigDefaults.ANCIENT_RUINS_DEFAULTS,
        ChronoDawnConfig.Structures::ancientRuins,
        ""),
    FORGOTTEN_LIBRARY("forgotten_library", ConfigDefaults.FORGOTTEN_LIBRARY_DEFAULTS,
        ChronoDawnConfig.Structures::forgottenLibrary,
        "the Portal Stabilizer recipe"),
    DESERT_CLOCK_TOWER("desert_clock_tower", ConfigDefaults.DESERT_CLOCK_TOWER_DEFAULTS,
        ChronoDawnConfig.Structures::desertClockTower,
        "Time Guardian, the Master Clock Key and Enhanced Clockstone"),
    GUARDIAN_VAULT("guardian_vault", ConfigDefaults.GUARDIAN_VAULT_DEFAULTS,
        ChronoDawnConfig.Structures::guardianVault,
        "Chronos Warden and the Guardian Stone"),
    CLOCKWORK_DEPTHS("clockwork_depths", ConfigDefaults.CLOCKWORK_DEPTHS_DEFAULTS,
        ChronoDawnConfig.Structures::clockworkDepths,
        "Clockwork Colossus and the Colossus Gear"),
    PHANTOM_CATACOMBS("phantom_catacombs", ConfigDefaults.PHANTOM_CATACOMBS_DEFAULTS,
        ChronoDawnConfig.Structures::phantomCatacombs,
        "Temporal Phantom and the Phantom Essence"),
    ENTROPY_CRYPT("entropy_crypt", ConfigDefaults.ENTROPY_CRYPT_DEFAULTS,
        ChronoDawnConfig.Structures::entropyCrypt,
        "Entropy Keeper and the Entropy Core"),
    MASTER_CLOCK("master_clock", ConfigDefaults.MASTER_CLOCK_DEFAULTS,
        ChronoDawnConfig.Structures::masterClock,
        "Time Tyrant, the final boss");

    private final String configKey;
    private final StructureSettings defaults;
    private final Function<ChronoDawnConfig.Structures, StructureSettings> accessor;
    private final String progressionNote;

    ManagedStructure(
        String configKey,
        StructureSettings defaults,
        Function<ChronoDawnConfig.Structures, StructureSettings> accessor,
        String progressionNote
    ) {
        this.configKey = configKey;
        this.defaults = defaults;
        this.accessor = accessor;
        this.progressionNote = progressionNote;
    }

    /** The TOML table name under {@code [world.structures]}. */
    public String configKey() {
        return configKey;
    }

    /** The registry ID of the structure this set places. */
    public String structureId() {
        return ChronoDawn.MOD_ID + ":" + configKey;
    }

    /** The data-pack-relative path of the structure_set JSON the overlay replaces. */
    public String packPath() {
        return "data/" + ChronoDawn.MOD_ID + "/worldgen/structure_set/" + configKey + ".json";
    }

    /** The shipped placement values, matching the bundled JSON. */
    public StructureSettings defaults() {
        return defaults;
    }

    /** What becomes unobtainable if this structure is disabled; empty when nothing does. */
    public String progressionNote() {
        return progressionNote;
    }

    /** Reads this structure's settings out of a parsed config. */
    public StructureSettings settingsOf(ChronoDawnConfig.Structures structures) {
        return accessor.apply(structures);
    }
}
