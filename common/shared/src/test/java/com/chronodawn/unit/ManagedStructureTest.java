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

import com.chronodawn.ChronoDawn;
import com.chronodawn.config.ChronoDawnConfig;
import com.chronodawn.config.ConfigDefaults;
import com.chronodawn.config.ManagedStructure;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards {@link ManagedStructure} as the single source of truth for configurable structures.
 *
 * <p>A structure that ships a structure_set but is missing from the enum would be
 * silently unconfigurable, and an enum constant with no structure_set would emit an
 * overlay file that overrides nothing. Both directions are checked here.
 */
class ManagedStructureTest {

    private static final Path STRUCTURE_SET_DIR = Paths.get(
        TestUtils.getProjectRoot(),
        "common", "shared", "src", "main", "resources",
        "data", "chronodawn", "worldgen", "structure_set"
    );

    @Test
    void enumCoversEveryShippedStructureSet() {
        File[] files = STRUCTURE_SET_DIR.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        assertTrue(files != null && files.length > 0,
            "No structure_set JSON found under " + STRUCTURE_SET_DIR);

        Set<String> shipped = Arrays.stream(files)
            .map(f -> f.getName().replace(".json", ""))
            .collect(Collectors.toCollection(TreeSet::new));
        Set<String> registered = Arrays.stream(ManagedStructure.values())
            .map(ManagedStructure::configKey)
            .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(shipped, registered,
            "Every structure_set must be registered in ManagedStructure and vice versa");
    }

    @Test
    void packPathAndStructureIdFollowTheConfigKey() {
        for (ManagedStructure structure : ManagedStructure.values()) {
            assertEquals(ChronoDawn.MOD_ID + ":" + structure.configKey(), structure.structureId(),
                structure.name());
            assertEquals(
                "data/" + ChronoDawn.MOD_ID + "/worldgen/structure_set/" + structure.configKey() + ".json",
                structure.packPath(),
                structure.name());
        }
    }

    @Test
    void settingsOfReturnsTheMatchingField() {
        ChronoDawnConfig.Structures defaults = ConfigDefaults.defaults().world().structures();
        for (ManagedStructure structure : ManagedStructure.values()) {
            assertEquals(structure.defaults(), structure.settingsOf(defaults),
                structure.name() + ": settingsOf must read the field its defaults describe");
        }
    }

    @Test
    void onlyAncientRuinsHasNoProgressionNote() {
        for (ManagedStructure structure : ManagedStructure.values()) {
            if (structure == ManagedStructure.ANCIENT_RUINS) {
                assertTrue(structure.progressionNote().isEmpty(),
                    "Ancient Ruins is Overworld flavour and must carry no progression note");
            } else {
                assertFalse(structure.progressionNote().isEmpty(),
                    structure.name() + " gates progression and must explain what disabling it costs");
            }
        }
    }

    @Test
    void exclusionZonesTargetPhantomCatacombsWithExpectedChunkCount() {
        Set<ManagedStructure> expectedWithZone =
            EnumSet.of(ManagedStructure.CLOCKWORK_DEPTHS, ManagedStructure.GUARDIAN_VAULT, ManagedStructure.ENTROPY_CRYPT);

        for (ManagedStructure structure : ManagedStructure.values()) {
            if (expectedWithZone.contains(structure)) {
                ManagedStructure.ExclusionZone zone = structure.exclusionZone().orElseThrow(
                    () -> new AssertionError(structure.name() + " must declare an exclusion zone"));
                assertEquals(ManagedStructure.PHANTOM_CATACOMBS, zone.target(),
                    structure.name() + ": exclusion zone must target Phantom Catacombs");
                assertEquals(10, zone.chunkCount(),
                    structure.name() + ": exclusion zone chunk count");
            } else {
                assertTrue(structure.exclusionZone().isEmpty(),
                    structure.name() + " must not declare an exclusion zone");
            }
        }
    }

    @Test
    void compassTargetKeysHaveTranslations() {
        Map<String, String> lang = TestUtils.loadLangFile();
        for (ManagedStructure structure : ManagedStructure.values()) {
            assertTrue(lang.containsKey(structure.compassTargetKey()),
                structure.name() + ": the Time Compass would show a raw key for "
                    + structure.compassTargetKey());
        }
    }

    @Test
    void onlyAncientRuinsGeneratesInTheOverworld() {
        for (ManagedStructure structure : ManagedStructure.values()) {
            ManagedStructure.Dimension expected = structure == ManagedStructure.ANCIENT_RUINS
                ? ManagedStructure.Dimension.OVERWORLD
                : ManagedStructure.Dimension.CHRONO_DAWN;
            assertEquals(expected, structure.dimension(), structure.name());
        }
    }

    @Test
    void byConfigKeyRoundTripsEveryConstantAndRejectsUnknownKeys() {
        for (ManagedStructure structure : ManagedStructure.values()) {
            assertEquals(structure, ManagedStructure.byConfigKey(structure.configKey()).orElse(null),
                structure.name());
        }
        assertTrue(ManagedStructure.byConfigKey("not_a_structure").isEmpty());
    }
}
