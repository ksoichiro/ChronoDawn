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

import java.util.Optional;
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
    ANCIENT_RUINS("ancient_ruins", Dimension.OVERWORLD, ConfigDefaults.ANCIENT_RUINS_DEFAULTS,
        ChronoDawnConfig.Structures::ancientRuins,
        ""),
    FORGOTTEN_LIBRARY("forgotten_library", Dimension.CHRONO_DAWN, ConfigDefaults.FORGOTTEN_LIBRARY_DEFAULTS,
        ChronoDawnConfig.Structures::forgottenLibrary,
        "the Portal Stabilizer recipe"),
    DESERT_CLOCK_TOWER("desert_clock_tower", Dimension.CHRONO_DAWN, ConfigDefaults.DESERT_CLOCK_TOWER_DEFAULTS,
        ChronoDawnConfig.Structures::desertClockTower,
        "Time Guardian, the Master Clock Key and Enhanced Clockstone"),
    // Forward references to PHANTOM_CATACOMBS (declared below) can't be passed
    // directly as constructor arguments — the JLS forbids referencing a later enum
    // constant from an earlier one's initializer, even through a lambda. Passing its
    // name as a String and resolving it lazily via valueOf() in exclusionZone() sidesteps
    // the restriction entirely, since a String literal isn't a reference to the constant.
    GUARDIAN_VAULT("guardian_vault", Dimension.CHRONO_DAWN, ConfigDefaults.GUARDIAN_VAULT_DEFAULTS,
        ChronoDawnConfig.Structures::guardianVault,
        "Chronos Warden and the Guardian Stone",
        "PHANTOM_CATACOMBS", 10),
    CLOCKWORK_DEPTHS("clockwork_depths", Dimension.CHRONO_DAWN, ConfigDefaults.CLOCKWORK_DEPTHS_DEFAULTS,
        ChronoDawnConfig.Structures::clockworkDepths,
        "Clockwork Colossus and the Colossus Gear",
        "PHANTOM_CATACOMBS", 10),
    PHANTOM_CATACOMBS("phantom_catacombs", Dimension.CHRONO_DAWN, ConfigDefaults.PHANTOM_CATACOMBS_DEFAULTS,
        ChronoDawnConfig.Structures::phantomCatacombs,
        "Temporal Phantom and the Phantom Essence"),
    ENTROPY_CRYPT("entropy_crypt", Dimension.CHRONO_DAWN, ConfigDefaults.ENTROPY_CRYPT_DEFAULTS,
        ChronoDawnConfig.Structures::entropyCrypt,
        "Entropy Keeper and the Entropy Core",
        "PHANTOM_CATACOMBS", 10),
    MASTER_CLOCK("master_clock", Dimension.CHRONO_DAWN, ConfigDefaults.MASTER_CLOCK_DEFAULTS,
        ChronoDawnConfig.Structures::masterClock,
        "Time Tyrant, the final boss");

    /**
     * A fixed distance a structure set must keep from another set's placements.
     * Not user-configurable, hence modeled here rather than in {@link StructureSettings}.
     *
     * @param target the other structure set this one must stay clear of
     * @param chunkCount the exclusion radius, in chunks
     */
    public record ExclusionZone(ManagedStructure target, int chunkCount) {
    }

    /** The dimension a structure generates in; the Time Compass searches there. */
    public enum Dimension {
        OVERWORLD("minecraft:overworld"),
        CHRONO_DAWN("chronodawn:chronodawn");

        private final String id;

        Dimension(String id) {
            this.id = id;
        }

        /** The dimension's registry ID, as {@code namespace:path}. */
        public String id() {
            return id;
        }
    }

    private final String configKey;
    private final Dimension dimension;
    private final StructureSettings defaults;
    private final Function<ChronoDawnConfig.Structures, StructureSettings> accessor;
    private final String progressionNote;
    private final String exclusionZoneTargetName;
    private final int exclusionZoneChunkCount;

    ManagedStructure(
        String configKey,
        Dimension dimension,
        StructureSettings defaults,
        Function<ChronoDawnConfig.Structures, StructureSettings> accessor,
        String progressionNote
    ) {
        this(configKey, dimension, defaults, accessor, progressionNote, null, 0);
    }

    ManagedStructure(
        String configKey,
        Dimension dimension,
        StructureSettings defaults,
        Function<ChronoDawnConfig.Structures, StructureSettings> accessor,
        String progressionNote,
        String exclusionZoneTargetName,
        int exclusionZoneChunkCount
    ) {
        this.configKey = configKey;
        this.dimension = dimension;
        this.defaults = defaults;
        this.accessor = accessor;
        this.progressionNote = progressionNote;
        this.exclusionZoneTargetName = exclusionZoneTargetName;
        this.exclusionZoneChunkCount = exclusionZoneChunkCount;
    }

    /** The TOML table name under {@code [world.structures]}. */
    public String configKey() {
        return configKey;
    }

    /** The dimension this structure generates in. */
    public Dimension dimension() {
        return dimension;
    }

    /** The translation key the Time Compass shows for this structure. */
    public String compassTargetKey() {
        return "item.chronodawn.time_compass.target." + configKey;
    }

    /**
     * Looks up a structure by its {@link #configKey()}.
     *
     * @param configKey the TOML table name, which is also the Time Compass target value
     *                  persisted in item NBT
     * @return the matching structure, or empty for an unknown key
     */
    public static java.util.Optional<ManagedStructure> byConfigKey(String configKey) {
        for (ManagedStructure structure : values()) {
            if (structure.configKey.equals(configKey)) {
                return java.util.Optional.of(structure);
            }
        }
        return java.util.Optional.empty();
    }

    /** The registry ID of the structure this set places. */
    public String structureId() {
        return ChronoDawn.MOD_ID + ":" + configKey;
    }

    /**
     * The ID of this structure_set itself, as referenced by other structure sets'
     * exclusion zones. Distinct from {@link #structureId()} in concept — one names a
     * structure, the other names the set that places it — even though the two
     * strings coincide today because every set here places exactly one structure
     * sharing its name.
     */
    public String structureSetId() {
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

    /** The exclusion zone this structure set's placement must respect, if any. */
    public Optional<ExclusionZone> exclusionZone() {
        return exclusionZoneTargetName == null
            ? Optional.empty()
            : Optional.of(new ExclusionZone(ManagedStructure.valueOf(exclusionZoneTargetName), exclusionZoneChunkCount));
    }
}
